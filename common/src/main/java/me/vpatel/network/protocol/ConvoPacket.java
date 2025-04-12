package me.vpatel.network.protocol;

import io.netty.buffer.ByteBuf;

public abstract class ConvoPacket {

    private PacketDirection direction;
    private int id;

    public ConvoPacket() {
    }

    public PacketDirection getDirection() {
        return direction;
    }

    public int getId() {
        return id;
    }

    public void setDirection(PacketDirection direction) {
        this.direction = direction;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract void toWire(ByteBuf buf);

    public abstract void fromWire(ByteBuf buf);

    public abstract String toString();
}
