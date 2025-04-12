package me.vpatel.client;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.server.ServerPongPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoClientHandler extends ConvoHandler {

    private static final Logger log = LogManager.getLogger(ConvoClientHandler.class);

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
        if (msg instanceof ServerPongPacket serverPongPacket)
        {
            log.info("PONG! {}", serverPongPacket.getPayload());
        }
    }

    public ConvoConnection getConnection() {
        return connection;
    }
}
