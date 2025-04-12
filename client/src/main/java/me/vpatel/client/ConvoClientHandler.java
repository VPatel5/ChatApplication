package me.vpatel.client;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;

public class ConvoClientHandler extends ConvoHandler {

    private final ConvoClient client;
    private ConvoConnection connection;

    public ConvoClientHandler(ConvoClient client)
    {
        this.client = client;
    }

    @Override
    public void join(ConvoConnection connection) {
        this.connection = connection;
    }

    @Override
    public void leave(ConvoConnection connection) {
        this.connection = null;
        client.getConsole().shutdown();
    }

    @Override
    public void handle(ConvoConnection connection, ConvoPacket msg) {
        
    }
}
