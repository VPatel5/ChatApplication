package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

public class ClientGroupMessagesRequestPacket extends ConvoPacket {

    private String groupName;

    public ClientGroupMessagesRequestPacket() {

    }

    public ClientGroupMessagesRequestPacket(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(groupName, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        groupName = DataTypes.readString(buf);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("groupName", this.groupName)
                .toString();
    }
}