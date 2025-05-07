package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

import java.util.UUID;

public class ServerLoginFailPacket extends ConvoPacket {

    private String message;

    public ServerLoginFailPacket() {

    }

    public ServerLoginFailPacket(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(message, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        message = DataTypes.readString(buf);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", message)
                .toString();
    }
}