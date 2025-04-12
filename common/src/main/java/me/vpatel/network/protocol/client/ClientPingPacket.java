package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.DataTypes;
import me.vpatel.network.protocol.ConvoPacket;

public class ClientPingPacket extends ConvoPacket {

    private String payload;

    public ClientPingPacket() {

    }

    public ClientPingPacket(String payload) {
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