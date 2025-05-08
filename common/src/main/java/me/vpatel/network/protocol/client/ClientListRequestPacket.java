package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

public class ClientListRequestPacket extends ConvoPacket {

    private ListType type;
    private String groupName;

    public ClientListRequestPacket() {

    }

    public ClientListRequestPacket(ListType type) {
        this.type = type;
    }

    public ClientListRequestPacket(String groupName, ListType type) {
        this.type = type;
        this.groupName = groupName;
    }

    public ListType getType() {
        return type;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public void toWire(ByteBuf buf) {
        buf.writeInt(type.ordinal());
        if (type == ListType.OUTGOING_GROUP_INVITES || type == ListType.MESSAGES) {
            DataTypes.writeString(groupName, buf);
        }
    }

    @Override
    public void fromWire(ByteBuf buf) {
        type = ListType.values()[buf.readInt()];
        if (type == ListType.OUTGOING_GROUP_INVITES || type == ListType.MESSAGES) {
            groupName = DataTypes.readString(buf);
        }
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper string = MoreObjects.toStringHelper(this)
                .add("type", this.type);
        if (type == ListType.OUTGOING_GROUP_INVITES || type == ListType.MESSAGES) {
            string.add("groupName", groupName);
        }
        return string.toString();
    }

    public enum ListType {
        INCOMING_FRIEND_INVITES, OUTGOING_FRIEND_INVITES, FRIENDS, INCOMING_GROUP_INVITES, OUTGOING_GROUP_INVITES, GROUPS, MESSAGES, FALCUN_USERS
    }
}