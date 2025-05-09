package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.api.MessageType;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

import java.util.UUID;

public class ClientDirectMessagePacket extends ConvoPacket {

    private String message;
    private MessageType messageType;
    private UUID user;
    private String name;

    public ClientDirectMessagePacket() {

    }

    public ClientDirectMessagePacket(String message, UUID user) {
        this.message = message;
        this.messageType = MessageType.USER;
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
        DataTypes.writeString(user.toString(), buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        message = DataTypes.readString(buf);
        messageType = MessageType.USER;
        user = UUID.fromString(DataTypes.readString(buf));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", message)
                .add("messageType", messageType)
                .add("user", user)
                .toString();
    }
}