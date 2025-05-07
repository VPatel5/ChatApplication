package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.DataTypes;

public class ServerRegisterResponsePacket extends ConvoPacket {
    private boolean success;
    private String message;

    public ServerRegisterResponsePacket() {}

    public ServerRegisterResponsePacket(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeBoolean(success, buf);
        DataTypes.writeString(message, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        success = DataTypes.readBoolean(buf);
        message = DataTypes.readString(buf);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("success", success)
                .add("message", message)
                .toString();
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}