package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

public class ServerResponsePacket extends ConvoPacket {

    private String message;
    private ResponseType type;

    public ServerResponsePacket() {

    }

    public ServerResponsePacket(String message, ResponseType type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public ResponseType getType() {
        return type;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(message, buf);
        buf.writeInt(type.ordinal());
    }

    @Override
    public void fromWire(ByteBuf buf) {
        message = DataTypes.readString(buf);
        type = ResponseType.values()[buf.readInt()];
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", message)
                .add("type", type)
                .toString();
    }

    public enum ResponseType {
        OK, WARNING, ERROR
    }
}