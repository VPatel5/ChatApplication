package me.vpatel.client;

import me.vpatel.client.ui.ChatUI;
import me.vpatel.client.ui.LoginUI;
import me.vpatel.client.ui.RegisterUI;
import me.vpatel.client.ui.UIScreenManager;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.client.ClientLoginStartPacket;
import me.vpatel.network.protocol.client.ClientPingPacket;
import me.vpatel.network.protocol.server.*;
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
    }

    @Override
    public void leave(ConvoConnection connection) {
        this.connection = null;
        client.getConsole().shutdown();
    }

    @Override
    public void handle(ConvoConnection connection, ConvoPacket msg) {
        if (msg instanceof ServerPongPacket packet)
        {
            log.info("PONG! {}", packet.getPayload());
        }
        else if (msg instanceof ServerLoginFailPacket packet)
        {
            log.info("Logged in as failed");
            if (UIScreenManager.getCurrentFrame() instanceof LoginUI ui)
            {
                ui.statusLabel.setText(packet.getMessage());
            }
        }
        else if (msg instanceof ServerLoginSuccessPacket packet)
        {
            client.setUser(new ConvoUser(packet.getUuid(), packet.getUsername()));
            log.info("Logged in as {}", client.getUser());
        }
        else if (msg instanceof ServerRegisterResponsePacket packet)
        {
            if (!packet.isSuccess())
            {
                if (UIScreenManager.getCurrentFrame() instanceof RegisterUI ui)
                {
                    ui.statusLabel.setText("Error occurred registering");
                }
            }
        }
        else if (msg instanceof ServerEncryptionRequestPacket packet)
        {
            client.getAuthHandler().auth(packet, connection);
        }
        else if (msg instanceof ServerAuthFinishedPacket packet)
        {
            connection.setAuthFinished(true);
            UIScreenManager.showScreen(new ChatUI());
        }
    }

    public ConvoConnection getConnection() {
        return connection;
    }
}
