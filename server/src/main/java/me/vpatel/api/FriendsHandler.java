package me.vpatel.api;

import me.vpatel.db.FriendDao;
import me.vpatel.db.InviteDao;
import me.vpatel.db.MessageDao;
import me.vpatel.db.UserDao;
import me.vpatel.db.tables.InviteTable;
import me.vpatel.db.tables.MessageTable;
import me.vpatel.db.tables.UsersTable;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.InviteType;
import me.vpatel.network.protocol.server.ServerDirectMessagePacket;
import me.vpatel.server.ConvoServer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FriendsHandler {
    private final ConvoServer server;

    public FriendsHandler(ConvoServer server) {
        this.server = server;
    }

    public Invite sendInvite(ConvoUser sender, String receiverName) {
        // cant invite yourself
        if (sender.getName().equals(receiverName)) {
            return null;
        }

        // check if already on friendlist
        for (ConvoUser friend : getFriends(sender)) {
            if (friend.getName().equals(receiverName)) {
                return null;
            }
        }

        // check existing invite
        for (Invite outgoingInvite : getOutgoingInvites(sender)) {
            if (outgoingInvite.getUser().getName().equals(receiverName)) {
                return null;
            }
        }

        // get user
        UsersTable receiver = server.getDbHandler().jdbi().withExtension(UserDao.class, handle -> handle.getByName(receiverName));
        if (receiver == null) {
            return null;
        }

        // check existing invite the other way around
        for (Invite incomingInvite : getIncomingInvites(receiver.convert())) {
            if (incomingInvite.getUser().getName().equals(receiverName)) {
                return null;
            }
        }

        return server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> {
            long id = handle.createInvite(new InviteTable(receiver.getId(), sender.getInternalId()));
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
        return server.getDbHandler().jdbi().withExtension(FriendDao.class, handle -> {
            handle.addFriend(UsersTable.of(user), UsersTable.of(invite.getInviter()));
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

    public List<Invite> getIncomingInvites(ConvoUser user) {
        List<Invite> list = new ArrayList<>();
        for (InviteTable table : server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> handle.getIncomingInvites(user, InviteType.FRIEND))) {
            Invite convert = table.convert(server.getUsersHandler(), server.getGroupsHandler());
            list.add(convert);
        }
        return list;
    }

    public List<Invite> getOutgoingInvites(ConvoUser user) {
        List<Invite> list = new ArrayList<>();
        for (InviteTable table : server.getDbHandler().jdbi().withExtension(InviteDao.class, handle -> handle.getOutgoingInvites(user, InviteType.FRIEND))) {
            Invite convert = table.convert(server.getUsersHandler(), server.getGroupsHandler());
            list.add(convert);
        }
        return list;
    }

    public List<ConvoUser> getFriends(ConvoUser user) {
        return server.getDbHandler().jdbi().withExtension(FriendDao.class,
                        handle -> handle.findFriends(UsersTable.of(user))).
                stream().map(UsersTable::convert)
                .collect(Collectors.toList());
    }

    public String removeUser(ConvoUser sender, ConvoUser removed) {
        Integer affectedRows = server.getDbHandler().jdbi().withExtension(FriendDao.class,
                handle -> handle.removeFriend(UsersTable.of(sender), UsersTable.of(removed)));

        return affectedRows != 0 ? "OK" : "User was not in friendlist!";
    }

//    public String chat(ConvoUser sender, ConvoUser receiver, String message) {
//        ConvoConnection connection = server.getHandler().getConnection(receiver);
//        if (connection == null) {
//            return "User is not online or not in friend list!";
//        }
//
//        if (getFriends(sender).stream().noneMatch(p -> p.getInternalId() == receiver.getInternalId())) {
//            return "User is not online or not in friend list!";
//        } else {
//            connection.sendPacket(new ServerChatPacket(message, sender.getId()));
//            return "Send";
//        }
//    }

    public String chat(ConvoUser sender, ConvoUser receiver, String message) {
        // Verify they are friends
        boolean areFriends = getFriends(sender).stream()
                .anyMatch(f -> f.getInternalId() == receiver.getInternalId());
        if (!areFriends) {
            return "User is not in friend list!";
        }

        ConvoConnection connection = server.getHandler().getConnection(receiver);
        if (connection != null) {
            connection.sendPacket(new ServerDirectMessagePacket(message, sender.getId()));
        }

        MessageTable messageTable = new MessageTable();
        messageTable.setSenderId(sender.getInternalId());
        messageTable.setRecipientId(receiver.getInternalId());
        messageTable.setTimestamp(OffsetDateTime.now());
        messageTable.setGroupId(-1);
        messageTable.setMessage(message);

        return server.getDbHandler().jdbi().withExtension(MessageDao.class, handle -> {
            handle.saveMessage(messageTable);
            return "Send";
        });
    }
}
