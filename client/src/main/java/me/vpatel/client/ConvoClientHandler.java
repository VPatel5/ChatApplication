package me.vpatel.client;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.client.ClientLoginStartPacket;
import me.vpatel.network.protocol.client.ClientPingPacket;
import me.vpatel.network.protocol.server.ServerLoginSuccessPacket;
import me.vpatel.network.protocol.server.ServerPongPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoClientHandler extends ConvoHandler {

    private static final Logger log = LogManager.getLogger(ConvoClientHandler.class);

    private static final ConvoClient client = AppContext.getClient();
    private ConvoConnection connection;

    public ConvoClientHandler()
    {

    }

    @Override
    public void join(ConvoConnection connection) {
        this.connection = connection;
        connection.sendPacket(new ClientLoginStartPacket(client.getUser().getName()));
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
        if (msg instanceof ServerLoginSuccessPacket packet)
        {
            client.setUser(new ConvoUser(packet.getUuid(), packet.getUsername()));
            //connection.setAuthFinished(true);
            log.info("Logged in as {}", client.getUser());
            connection.sendPacket(new ClientPingPacket("This is a test ping"));
        }
    }

    public ConvoConnection getConnection() {
        return connection;
    }
}
