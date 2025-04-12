package me.vpatel.client;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;

public class ConvoClientHandler extends ConvoHandler {

    private final ConvoClient client;

    public ConvoClientHandler(ConvoClient client)
    {
        this.client = client;
    }

    @Override
    public void join(ConvoConnection connection) {

    }

    @Override
    public void leave(ConvoConnection connection) {

    }

    @Override
    public void handle(ConvoConnection connection, ConvoPacket msg) {

    }
}
