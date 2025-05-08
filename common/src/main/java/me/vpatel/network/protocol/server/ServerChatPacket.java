package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;
import me.vpatel.network.protocol.client.ClientChatPacket.MessageType;

import java.util.UUID;

public class ServerChatPacket extends ConvoPacket {

    private String message;
    private MessageType messageType;
    private UUID user;
    private String name;

    public ServerChatPacket() {

    }

    public ServerChatPacket(String message) {
        this.message = message;
        this.messageType = MessageType.SYSTEM;
    }

    public ServerChatPacket(String message, UUID user) {
        this.message = message;
        this.messageType = MessageType.USER;
        this.user = user;
    }

    public ServerChatPacket(String message, String groupName, UUID user) {
        this.message = message;
        this.messageType = MessageType.GROUP;
        this.name = groupName;
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public UUID getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(message, buf);
        buf.writeInt(messageType.ordinal());
        if (messageType == MessageType.USER) {
            DataTypes.writeString(user.toString(), buf);
        } else if (messageType == MessageType.GROUP) {
            DataTypes.writeString(name, buf);
            DataTypes.writeString(user.toString(), buf);
        }
    }

    @Override
    public void fromWire(ByteBuf buf) {
        message = DataTypes.readString(buf);
        messageType = MessageType.values()[buf.readInt()];
        if (messageType == MessageType.USER) {
            user = UUID.fromString(DataTypes.readString(buf));
        } else if (messageType == MessageType.GROUP) {
            name = DataTypes.readString(buf);
            user = UUID.fromString(DataTypes.readString(buf));
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", message)
                .add("messageType", messageType)
                .add("user", user)
                .add("name", name)
                .toString();
    }
}