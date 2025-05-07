package me.vpatel.server;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.client.ClientLoginStartPacket;
import me.vpatel.network.protocol.client.ClientPingPacket;
import me.vpatel.network.protocol.server.ServerLoginSuccessPacket;
import me.vpatel.network.protocol.server.ServerPongPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ConvoServerHandler extends ConvoHandler {

    private static final Logger log = LogManager.getLogger(ConvoServerHandler.class);

    private final Queue<ConvoConnection> connections = new ConcurrentLinkedQueue<>();

    private final ConvoServer server;

    public ConvoServerHandler(ConvoServer server) {
        this.server = server;
    }

    public Queue<ConvoConnection> getConnections() {
        return connections;
    }

    @Override
    public void join(ConvoConnection connection) {
        connections.add(connection);
        log.info("Now handling {} connections", connections.size());
    }

    @Override
    public void leave(ConvoConnection connection) {
        connections.remove(connection);
        log.info("Now handling {} connections", connections.size());
    }

    @Override
    public void handle(ConvoConnection connection, ConvoPacket msg) {
        if (msg instanceof ClientPingPacket clientPingPacket)
        {
            log.info("Received ping packet from {} with payload {}", connection.getRemoteAddress(), clientPingPacket.getPayload());
            connection.sendPacket(new ServerPongPacket(clientPingPacket.getPayload()));
        }
        else if (msg instanceof ClientLoginStartPacket packet)
        {
            String username = packet.getUsername();
            UUID uuid = UUID.nameUUIDFromBytes(username.getBytes());

            log.info("Received login packet from {} with username {}", connection.getRemoteAddress(), username);
            connection.sendPacket(new ServerLoginSuccessPacket(username, uuid));

            boolean foundMatch = connections.stream()
                    .filter(c -> c.getUser() != null && c.getUser().getName() != null)
                    .anyMatch(c -> c.getUser().getName().equalsIgnoreCase(packet.getUsername()));

            if (foundMatch)
            {
                connection.close("Already connected from a different connection!");
                return;
            }
            //connection.initUser(packet.getUsername());
            //connection.sendPacket(new ServerEncryptionRequestPacket(server.getAuthHandler().getPublicKey(), server.getAuthHandler().genVerificationToken(packet.getUsername())));
        }
    }
}