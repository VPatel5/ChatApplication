package me.vpatel.network.protocol.client;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import me.vpatel.network.protocol.ConvoPacket;

public class ClientDirectMessagesRequestPacket extends ConvoPacket {

    public ClientDirectMessagesRequestPacket() {

    }

    @Override
    public void toWire(ByteBuf buf) {
    }

    @Override
    public void fromWire(ByteBuf buf) {
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper string = MoreObjects.toStringHelper(this);
        return string.toString();
    }
}