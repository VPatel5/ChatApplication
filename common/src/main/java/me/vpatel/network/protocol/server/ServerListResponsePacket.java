package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.api.ConvoGroup;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;
import me.vpatel.network.protocol.client.ClientListRequestPacket.ListType;

import java.util.ArrayList;
import java.util.List;

public class ServerListResponsePacket extends ConvoPacket {

    private ListType type;
    private String groupName;
    private List<Invite> invites;
    private List<ConvoUser> users;
    private List<ConvoUser> friends;
    private List<ConvoGroup> groups;
    private List<Message> messages;

    public ServerListResponsePacket() {

    }

    public ServerListResponsePacket(ListType type, List<Invite> invites) {
        this.type = type;
        this.invites = invites;
    }

    public ServerListResponsePacket(String groupName, List<Invite> invites) {
        this.type = ListType.OUTGOING_GROUP_INVITES;
        this.groupName = groupName;
        this.invites = invites;
    }

    public ServerListResponsePacket(List<Message> messages, String groupName) {
        this.type = ListType.MESSAGES;
        this.groupName = groupName;
        this.messages = messages;
    }

    public ServerListResponsePacket(List<ConvoUser> users, boolean listUsers) {
        this.type = ListType.FALCUN_USERS;
        this.users = users;
    }

    public ServerListResponsePacket(List<ConvoUser> friends) {
        this.type = ListType.FRIENDS;
        this.friends = friends;
    }

    public ServerListResponsePacket(List<ConvoGroup> groups, int dummyParam) {
        this.type = ListType.GROUPS;
        this.groups = groups;
    }

    public ListType getType() {
        return type;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<Invite> getInvites() {
        return invites;
    }

    public List<ConvoUser> getFriends() {
        return friends;
    }

    public List<ConvoGroup> getGroups() {
        return groups;
    }

    public List<ConvoUser> getUsers() {
        return users;
    }

    public List<Message> getMessages() {
        return messages;
    }

    @Override
    public void toWire(ByteBuf buf) {
        buf.writeInt(type.ordinal());
        if (type == ListType.OUTGOING_GROUP_INVITES) {
            DataTypes.writeString(groupName, buf);
            buf.writeInt(invites.size());
            for (Invite invite : invites) {
                DataTypes.writeInvite(invite, buf);
            }
        } else if (type == ListType.MESSAGES) {
            DataTypes.writeString(groupName, buf);
            buf.writeInt(messages.size());
            for (Message message : messages) {
                DataTypes.writeMessage(message, buf);
            }
        } else if (type == ListType.FALCUN_USERS) {
            buf.writeInt(users.size());
            for (ConvoUser user : users) {
                DataTypes.writeUser(user, buf);
            }
        } else if (type == ListType.FRIENDS) {
            buf.writeInt(friends.size());
            for (ConvoUser friend : friends) {
                DataTypes.writeUser(friend, buf);
            }
        } else if (type == ListType.GROUPS) {
            buf.writeInt(groups.size());
            for (ConvoGroup group : groups) {
                DataTypes.writeGroup(group, buf);
            }
        } else {
            buf.writeInt(invites.size());
            for (Invite invite : invites) {
                DataTypes.writeInvite(invite, buf);
            }
        }
    }

    @Override
    public void fromWire(ByteBuf buf) {
        type = ListType.values()[buf.readInt()];
        if (type == ListType.OUTGOING_GROUP_INVITES) {
            groupName = DataTypes.readString(buf);
            invites = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                invites.add(DataTypes.readInvite(buf));
            }
        } else if (type == ListType.MESSAGES) {
            groupName = DataTypes.readString(buf);
            messages = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                messages.add(DataTypes.readMessage(buf));
            }
        }else if (type == ListType.FRIENDS) {
            friends = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                friends.add(DataTypes.readUser(buf));
            }
        } else if (type == ListType.GROUPS) {
            groups = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                groups.add(DataTypes.readGroup(buf));
            }
        } else if (type == ListType.FALCUN_USERS) {
            users = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                users.add(DataTypes.readUser(buf));
            }
        } else {
            invites = new ArrayList<>();
            int size = buf.readInt();
            for (int i = 0; i < size; i++) {
                invites.add(DataTypes.readInvite(buf));
            }
        }
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper string = MoreObjects.toStringHelper(this)
                .add("type", this.type);
        if (type == ListType.OUTGOING_GROUP_INVITES) {
            string.add("groupName", groupName);
            string.add("invites", invites);
        } else if (type == ListType.MESSAGES) {
            string.add("groupName", groupName);
            string.add("messages", messages);
        } else if (type == ListType.FRIENDS) {
            string.add("friends", friends);
        } else if (type == ListType.FALCUN_USERS) {
            string.add("users", users);
        }else if (type == ListType.GROUPS) {
            string.add("groups", groups);
        } else {
            string.add("invites", invites);
        }
        return string.toString();
    }
}