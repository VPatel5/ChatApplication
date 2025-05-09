package me.vpatel.client.api;

import me.vpatel.client.ConvoClient;
import me.vpatel.network.api.ConvoGroup;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.client.ClientActionPacket;
import me.vpatel.network.protocol.client.ClientChatPacket;
import me.vpatel.network.protocol.client.ClientListRequestPacket;
import me.vpatel.network.protocol.client.ClientListRequestPacket.ListType;
import me.vpatel.network.protocol.client.ClientActionPacket.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientApi {
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
    private Map<String, List<Message>> messages = new HashMap<>();

    public void chat(String friendName, String message) {
        for (ConvoUser friend : this.getFriends()) {
            if (friend.getName().equals(friendName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientChatPacket(message, friend.getId()));
                return;
            }
        }
    }

    public void chat(ConvoUser user, String message) {
        client.getHandler().getConnection()
                .sendPacket(new ClientChatPacket(message, user.getId()));
    }

    public void groupChat(String groupName, String message) {
        client.getHandler().getConnection()
                .sendPacket(new ClientChatPacket(message, groupName));
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

    public Map<String, List<Message>> getMessages() {
        return messages;
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

    public void setMessages(Map<String, List<Message>> messages) {
        this.messages = messages;
    }

    public void setOutgoingFriendInvites(List<Invite> outgoingFriendInvites) {
        this.outgoingFriendInvites = outgoingFriendInvites;
    }

    public void setOutgoingGroupInvites(Map<String, List<Invite>> outgoingGroupInvites) {
        this.outgoingGroupInvites = outgoingGroupInvites;
    }

    public void setUsers(List<ConvoUser> users) {
        this.users = users;
    }
}
