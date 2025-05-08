package me.vpatel.api;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.vpatel.db.GroupDao;
import me.vpatel.db.InviteDao;
import me.vpatel.db.MessageDao;
import me.vpatel.db.UserDao;
import me.vpatel.db.tables.GroupTable;
import me.vpatel.db.tables.InviteTable;
import me.vpatel.db.tables.MessageTable;
import me.vpatel.db.tables.UsersTable;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.*;
import me.vpatel.network.protocol.server.ServerChatPacket;
import me.vpatel.server.ConvoServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GroupsHandler {

    private final ConvoServer server;

    private final LoadingCache<Long, ConvoGroup> cache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(new CacheLoader<>() {
                @Override
                public ConvoGroup load(Long id) {
                    GroupTable table = server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> handle.getById(id));

                    if (table == null) {
                        return null;
                    }

                    return table.convert(server.getUsersHandler());
                }
            });

    public GroupsHandler(ConvoServer server) {
        this.server = server;
    }

    public String createGroup(String name, ConvoUser owner) {
        if (name == null || name.length() < 4) {
            return "Name must be at least 4 chars long!";
        }

        return server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> {
            GroupTable group = handle.getGroup(name, owner);
            if (group != null) {
                return "Group already exists!";
            }
            long id = handle.createGroup(name, owner);
            handle.addUser(id, owner);
            return "OK";
        });
    }

    public String deleteGroup(String name, ConvoUser owner) {
        return server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> {
            GroupTable group = handle.getGroup(name, owner);
            if (group == null) {
                return "Group not found!";
            }
            if (group.getOwner() == owner.getInternalId()) {
                return "Only the owner can delete the group!";
            }
            if (handle.deleteGroup(group.getId())) {
                return "OK";
            } else {
                return "Failed to delete group!";
            }
        });
    }

    public String kick(ConvoGroup group, ConvoUser owner, ConvoUser kicked) {
        if (owner.getName().equals(kicked.getName()) || owner.getInternalId() == kicked.getInternalId()) {
            return "Can't kick yourself!";
        }
        return server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> {
            GroupTable realGroup = handle.getGroup(group.getName(), owner);

            if (realGroup.getOwner() != owner.getInternalId()) {
                return "Only owner can kick users!";
            }

            handle.removeUser(realGroup.getId(), kicked);
            return "OK";
        });
    }

    public Invite sendInvite(ConvoGroup group, ConvoUser sender, String receiverName) {
        // cant invite yourself
        if (sender.getName().equals(receiverName)) {
            return null;
        }

        // check if user exist
        UsersTable receiver = server.getDbHandler().jdbi().withExtension(UserDao.class, handle -> handle.getByName(receiverName));
        if (receiver == null) {
            return null;
        }

        GroupTable groupTable = server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> handle.getGroup(group.getName(), sender));
        ConvoGroup realGroup = groupTable.convert(server.getUsersHandler());
        // TODO validate group

        // check if owner
        if (groupTable.getOwner() != sender.getInternalId()) {
            return null;
        }

        // check if already member
        if (getGroupMembers(groupTable.getId()).stream().anyMatch(p -> p.getInternalId() == receiver.getId())) {
            return null;
        }

        // check existing invite
        for (Invite outgoingInvite : getOutgoingInvites(groupTable.getName(), sender)) {
            if (outgoingInvite.getUser().getName().equals(receiverName)) {
                return null;
            }
        }

        return server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> {
            long id = handle.createInvite(new InviteTable(receiver.getId(), sender.getInternalId(), groupTable.getId()));
            return handle.getInvite(id).convert(server.getUsersHandler(), server.getGroupsHandler());
        });
    }

    public String acceptInvite(ConvoUser user, Invite invite) {
        String error = server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> {
            if (!checkInviteAction(handle, user, invite, true)) {
                return "Invalid";
            }
            handle.deactivateInviteById(invite.getInternalId());
            return "OK";
        });
        if (!"OK".equals(error)) {
            return error;
        }
        return server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> {
            handle.addUser(invite.getGroup().getInternalId(), invite.getUser());
            return "OK";
        });
    }

    public String declineInvite(ConvoUser user, Invite invite) {
        return server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> {
            if (!checkInviteAction(handle, user, invite, true)) {
                return "Invalid";
            }
            handle.deactivateInviteById(invite.getInternalId());
            return "OK";
        });
    }

    public String revokeInvite(ConvoUser user, Invite invite) {
        return server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> {
            if (!checkInviteAction(handle, user, invite, false)) {
                return "Invalid";
            }
            handle.deactivateInviteById(invite.getInternalId());
            return "OK";
        });
    }

    private boolean checkInviteAction(InviteDao dao, ConvoUser user, Invite invite, boolean checkForUserAction) {
        InviteTable table = dao.getInvite(invite.getInternalId());
        boolean valid = table.convert(server.getUsersHandler(), server.getGroupsHandler()).equals(invite);
        if (checkForUserAction) {
            valid = valid && invite.getUser().equals(user);
        } else {
            valid = valid && invite.getInviter().equals(user);
        }
        return valid;
    }

    public List<ConvoGroup> getGroups(ConvoUser user, boolean includeMembers) {
        List<ConvoGroup> groups = server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> handle.getGroups(user))
                .stream().map(g -> g.convert(server.getUsersHandler())).collect(Collectors.toList());
        if (includeMembers) {
            server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> {
                groups.forEach(g -> g.setUsers(handle.getGroupMembers(g.getInternalId()).stream().map(UsersTable::convert).collect(Collectors.toSet())));
                return null;
            });
        }

        return groups;
    }

    public List<Invite> getIncomingInvites(ConvoUser user) {
        List<Invite> list = new ArrayList<>();
        for (InviteTable table : server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> handle.getIncomingInvites(user, InviteType.GROUP))) {
            Invite convert = table.convert(server.getUsersHandler(), server.getGroupsHandler());
            list.add(convert);
        }
        return list;
    }

    public List<Invite> getOutgoingInvites(String name, ConvoUser user) {
        ConvoGroup group = getGroup(name, user);
        if (group == null) {
            return new ArrayList<>();
        }
        long groupId = group.getInternalId();
        List<Invite> list = new ArrayList<>();
        for (InviteTable table : server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> handle.getOutgoingInvites(user, InviteType.GROUP, groupId))) {
            Invite convert = table.convert(server.getUsersHandler(), server.getGroupsHandler());
            list.add(convert);
        }
        return list;
    }

    public List<ConvoUser> getGroupMembers(long groupId) {
        return server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> handle.getGroupMembers(groupId))
                .stream().map(UsersTable::convert).collect(Collectors.toList());
    }

    public ConvoGroup getGroup(String name, ConvoUser user) {
        GroupTable groupTable = server.getDbHandler().jdbi().withExtension(GroupDao.class, handle -> handle.getGroup(name, user));
        if (groupTable == null) {
            return null;
        }
        return groupTable.convert(server.getUsersHandler());
    }

    public String chat(String name, ConvoUser user, String message) {
        ConvoGroup group = getGroup(name, user);
        if (group == null) {
            return "You are not in a group named like that!";
        }
        getGroupMembers(group.getInternalId()).forEach(receiver -> {
            ConvoConnection connection = server.getHandler().getConnection(receiver);
            if (connection != null) {
                connection.sendPacket(new ServerChatPacket(message, name, user.getId()));
            }
        });

        MessageTable messageTable = new MessageTable();
        messageTable.setGroupId(group.getInternalId());
        messageTable.setMessage(message);
        messageTable.setUser(user.getInternalId());
        return server.getDbHandler().jdbi().withExtension(MessageDao.class, handle -> {
            handle.saveMessage(messageTable);
            return "Send";
        });
    }

    public List<Message> getMessages(String name, ConvoUser user) {
        ConvoGroup group = getGroup(name, user);
        if (group == null) {
            return new ArrayList<>();
        }

        return server.getDbHandler().jdbi().withExtension(MessageDao.class, handle -> handle.retrieveMessages(group.getInternalId(), 100))
                .stream().map((MessageTable table) -> MessageTable.convert(table, server.getUsersHandler(), server.getGroupsHandler())).collect(Collectors.toList());
    }

    public ConvoGroup getOrCacheGroup(long groupId) {
        try {
            return cache.get(groupId);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }
}