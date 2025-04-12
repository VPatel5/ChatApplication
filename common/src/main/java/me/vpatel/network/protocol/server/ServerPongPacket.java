package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.DataTypes;
import me.vpatel.network.protocol.ConvoPacket;

public class ServerPongPacket extends ConvoPacket {

    private String payload;

    public ServerPongPacket() {

    }

    public ServerPongPacket(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public void toWire(ByteBuf buf) {
        DataTypes.writeString(payload, buf);
    }

    @Override
    public void fromWire(ByteBuf buf) {
        payload = DataTypes.readString(buf);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("payload", payload)
                .toString();
    }
}