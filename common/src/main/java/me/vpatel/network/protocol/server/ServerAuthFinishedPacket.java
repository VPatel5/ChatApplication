package me.vpatel.network.protocol.server;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;

public class ServerAuthFinishedPacket extends ConvoPacket {

    public ServerAuthFinishedPacket() {

    }

    @Override
    public void toWire(ByteBuf buf) {
    }

    @Override
    public void fromWire(ByteBuf buf) {
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }
}