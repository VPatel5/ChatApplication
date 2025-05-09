package me.vpatel.client.api;

import me.vpatel.client.ConvoClient;
import me.vpatel.network.api.ConvoGroup;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.client.*;
import me.vpatel.network.protocol.client.ClientListRequestPacket.ListType;
import me.vpatel.network.protocol.client.ClientActionPacket.Action;
import me.vpatel.network.protocol.server.ServerGroupMessagePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientApi {
    private static final Logger log = LogManager.getLogger(ClientApi.class);

    private final ConvoClient client;

    public ClientApi(ConvoClient client) {
        this.client = client;
    }

    private List<ConvoUser> users = new ArrayList<>();
    private List<ConvoUser> friends = new ArrayList<>();
    private List<ConvoGroup> groups = new ArrayList<>();
    private List<Invite> incomingFriendInvites = new ArrayList<>();
    private List<Invite> outgoingFriendInvites = new ArrayList<>();
    private Map<String, List<Invite>> incomingGroupInvites = new HashMap<>();
    private Map<String, List<Invite>> outgoingGroupInvites = new HashMap<>();
    private Map<String, List<Message>> groupMessages = new HashMap<>();
    private List<Message> directMessages = new ArrayList<>();

    public void chat(String friendName, String message) {
        log.info("Attempting to send {} message: {}", friendName, message);
        log.info("Friends: {}", friends);
        for (ConvoUser friend : this.getFriends()) {
            if (friend.getName().equals(friendName)) {
                log.info("Sending {} message: {}", friend.getId(), message);
                client.getHandler().getConnection()
                        .sendPacket(new ClientDirectMessagePacket(message, friend.getId()));
                return;
            }
        }
    }

    public void chat(ConvoUser user, String message) {
        log.info("Sending {} message: {}", user, message);
        client.getHandler().getConnection()
                .sendPacket(new ClientDirectMessagePacket(message, user.getId()));
    }

    public void groupChat(String groupName, String message) {
        client.getHandler().getConnection()
                .sendPacket(new ClientGroupMessagePacket(message, groupName));
    }

    public void list(ListType type) {
        client.getHandler().getConnection()
                .sendPacket(new ClientListRequestPacket(type));
    }

    public void list(ListType type, String groupName) {
        client.getHandler().getConnection()
                .sendPacket(new ClientListRequestPacket(groupName, type));
    }

    public void kickUser(String groupName, String userName) {
        ConvoGroup group = this.getGroup(groupName);
        if (group == null) return;
        for (ConvoUser user : group.getUsers()) {
            if (user.getName().equals(userName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(user, group));
                return;
            }
        }
    }

    public void removeFriend(String friendName) {
        for (ConvoUser friend : this.getFriends()) {
            if (friend.getName().equals(friendName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(friend));
                return;
            }
        }
    }

    public void sendFriendInvite(String username) {
        client.getHandler().getConnection()
                .sendPacket(new ClientActionPacket(username));
    }

    public void sendGroupInvite(String groupName, String targetName) {
        ConvoGroup group = this.getGroup(groupName);
        if (group == null) return;
        client.getHandler().getConnection()
                .sendPacket(new ClientActionPacket(group, targetName, Action.SEND_GROUP_INVITE));
    }

    public void createGroup(String groupName) {
        log.info("Attempting to create group {}", groupName);
        client.getHandler().getConnection()
                .sendPacket(new ClientActionPacket(groupName, Action.CREATE_GROUP));
    }

    public void deleteGroup(String groupName) {
        client.getHandler().getConnection()
                .sendPacket(new ClientActionPacket(groupName, Action.DELETE_GROUP));
    }

    public void acceptFriendInvite(String inviterName) {
        for (Invite invite : this.getIncomingFriendInvites()) {
            if (invite.getInviter().getName().equals(inviterName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.ACCEPT_FRIEND_INVITE, invite));
                return;
            }
        }
    }

    public void declineFriendInvite(String inviterName) {
        for (Invite invite : this.getIncomingFriendInvites()) {
            if (invite.getInviter().getName().equals(inviterName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.DECLINE_FRIEND_INVITE, invite));
                return;
            }
        }
    }

    public void revokeFriendInvite(String targetName) {
        for (Invite invite : this.getOutgoingFriendInvites()) {
            if (invite.getUser().getName().equals(targetName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.REVOKE_FRIEND_INVITE, invite));
                return;
            }
        }
    }

    public void acceptGroupInvite(String groupName) {
        List<Invite> invites = this.getIncomingGroupInvites().get(groupName);
        if (invites == null) return;
        for (Invite invite : invites) {
            if (invite.getUser().getName().equals(client.getUser().getName())) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.ACCEPT_GROUP_INVITE, invite));
                return;
            }
        }
    }

    public void declineGroupInvite(String groupName) {
        List<Invite> invites = this.getIncomingGroupInvites().get(groupName);
        if (invites == null) return;
        for (Invite invite : invites) {
            if (invite.getUser().getName().equals(client.getUser().getName())) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.DECLINE_GROUP_INVITE, invite));
                return;
            }
        }
    }

    public void revokeGroupInvite(String groupName, String targetName) {
        List<Invite> invites = this.getOutgoingGroupInvites().get(groupName);
        if (invites == null) return;
        for (Invite invite : invites) {
            if (invite.getUser().getName().equals(targetName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.REVOKE_GROUP_INVITE, invite));
                return;
            }
        }
    }

    public void requestDirectMessages()
    {
        client.getHandler().getConnection().sendPacket(new ClientDirectMessagesRequestPacket());
    }

    public void requestGroupMessages(String group)
    {
        client.getHandler().getConnection().sendPacket(new ClientGroupMessagesRequestPacket(group));
    }

    public List<Message> getDirectMessages() {
        return directMessages;
    }

    public Map<String, List<Message>> getGroupMessages() {
        return groupMessages;
    }

    public Map<String, List<Invite>> getOutgoingGroupInvites() {
        return outgoingGroupInvites;
    }

    public Map<String, List<Invite>> getIncomingGroupInvites() {
        return incomingGroupInvites;
    }

    public List<Invite> getIncomingFriendInvites() {
        return incomingFriendInvites;
    }

    public List<Invite> getOutgoingFriendInvites() {
        return outgoingFriendInvites;
    }

    public List<ConvoUser> getUsers() {
        return users;
    }

    public List<ConvoUser> getFriends() {
        return friends;
    }

    public List<ConvoGroup> getGroups() {
        return groups;
    }

    public ConvoGroup getGroup(String name) {
        return groups.stream()
                .filter(g -> g.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void setFriends(List<ConvoUser> friends) {
        this.friends = friends;
    }

    public void setGroups(List<ConvoGroup> groups) {
        this.groups = groups;
    }

    public void setIncomingFriendInvites(List<Invite> incomingFriendInvites) {
        this.incomingFriendInvites = incomingFriendInvites;
    }

    public void setIncomingGroupInvites(Map<String, List<Invite>> incomingGroupInvites) {
        this.incomingGroupInvites = incomingGroupInvites;
    }

    public void setOutgoingFriendInvites(List<Invite> outgoingFriendInvites) {
        this.outgoingFriendInvites = outgoingFriendInvites;
    }

    public void setOutgoingGroupInvites(Map<String, List<Invite>> outgoingGroupInvites) {
        this.outgoingGroupInvites = outgoingGroupInvites;
    }

    public void setDirectMessages(List<Message> directMessages) {
        this.directMessages = directMessages;
    }

    public void setGroupMessages(Map<String, List<Message>> groupMessages) {
        this.groupMessages = groupMessages;
    }

    public void setUsers(List<ConvoUser> users) {
        this.users = users;
    }
}
