package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.api.MessageType;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

public class ClientGroupMessagePacket extends ConvoPacket {

    private String message;
    private MessageType messageType;
    private String name;

    public ClientGroupMessagePacket() {

    }

    public ClientGroupMessagePacket(String message, String name) {
        this.message = message;
        this.messageType = MessageType.GROUP;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getName() {
        return name;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(message, buf);
        DataTypes.writeString(name, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        message = DataTypes.readString(buf);
        messageType = MessageType.GROUP;
        name = DataTypes.readString(buf);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", message)
                .add("messageType", messageType)
                .add("groupName", name)
                .toString();
    }
}