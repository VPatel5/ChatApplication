package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

import java.util.UUID;

public class ClientChatPacket extends ConvoPacket {

    private String message;
    private MessageType messageType;
    private UUID user;
    private String name;

    public ClientChatPacket() {

    }

    public ClientChatPacket(String message, UUID user) {
        this.message = message;
        this.messageType = MessageType.USER;
        this.user = user;
    }

    public ClientChatPacket(String message, String groupName) {
        this.message = message;
        this.messageType = MessageType.GROUP;
        this.name = groupName;
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

    public enum MessageType {
        GROUP, USER, SYSTEM
    }
}