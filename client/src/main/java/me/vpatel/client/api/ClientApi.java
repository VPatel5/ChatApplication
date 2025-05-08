package me.vpatel.client.api;

import me.vpatel.client.ConvoClient;
import me.vpatel.network.api.ConvoGroup;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.protocol.client.ClientActionPacket;
import me.vpatel.network.protocol.client.ClientChatPacket;
import me.vpatel.network.protocol.client.ClientListRequestPacket;
import me.vpatel.network.protocol.client.ClientListRequestPacket.ListType;
import me.vpatel.network.protocol.client.ClientActionPacket.Action;

import java.util.List;

public class ClientApi {
    private final ConvoClient client;

    public ClientApi(ConvoClient client) {
        this.client = client;
    }

    public void chat(String friendName, String message) {
        for (ConvoUser friend : client.getHandler().getFriends()) {
            if (friend.getName().equals(friendName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientChatPacket(message, friend.getId()));
                return;
            }
        }
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
        ConvoGroup group = client.getHandler().getGroup(groupName);
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
        for (ConvoUser friend : client.getHandler().getFriends()) {
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
        ConvoGroup group = client.getHandler().getGroup(groupName);
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
        for (Invite invite : client.getHandler().getIncomingFriendInvites()) {
            if (invite.getInviter().getName().equals(inviterName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.ACCEPT_FRIEND_INVITE, invite));
                return;
            }
        }
    }

    public void declineFriendInvite(String inviterName) {
        for (Invite invite : client.getHandler().getIncomingFriendInvites()) {
            if (invite.getInviter().getName().equals(inviterName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.DECLINE_FRIEND_INVITE, invite));
                return;
            }
        }
    }

    public void revokeFriendInvite(String targetName) {
        for (Invite invite : client.getHandler().getOutgoingFriendInvites()) {
            if (invite.getUser().getName().equals(targetName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.REVOKE_FRIEND_INVITE, invite));
                return;
            }
        }
    }

    public void acceptGroupInvite(String groupName) {
        List<Invite> invites = client.getHandler().getIncomingGroupInvites().get(groupName);
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
        List<Invite> invites = client.getHandler().getIncomingGroupInvites().get(groupName);
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
        List<Invite> invites = client.getHandler().getOutgoingGroupInvites().get(groupName);
        if (invites == null) return;
        for (Invite invite : invites) {
            if (invite.getUser().getName().equals(targetName)) {
                client.getHandler().getConnection()
                        .sendPacket(new ClientActionPacket(Action.REVOKE_GROUP_INVITE, invite));
                return;
            }
        }
    }
}
