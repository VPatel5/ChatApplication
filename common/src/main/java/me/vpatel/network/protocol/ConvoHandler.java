package me.vpatel.network.protocol;

import me.vpatel.network.ConvoConnection;

public abstract class ConvoHandler {

    public abstract void join(ConvoConnection connection);

    public abstract void leave(ConvoConnection connection);

    public abstract void handle(ConvoConnection connection, ConvoPacket msg);
}