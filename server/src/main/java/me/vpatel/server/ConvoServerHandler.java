package me.vpatel.server;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.client.ClientEncryptionResponsePacket;
import me.vpatel.network.protocol.client.ClientLoginStartPacket;
import me.vpatel.network.protocol.client.ClientPingPacket;
import me.vpatel.network.protocol.client.ClientRegisterRequestPacket;
import me.vpatel.network.protocol.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;
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
        if (msg instanceof ClientLoginStartPacket packet)
        {
            String username = packet.getUsername();
            String password = packet.getPassword();

            log.info("Received login packet from {} with username {}", connection.getRemoteAddress(), username);

            boolean foundMatch = connections.stream()
                    .filter(c -> c.getUser() != null && c.getUser().getName() != null)
                    .anyMatch(c -> c.getUser().getName().equalsIgnoreCase(packet.getUsername()));

            if (foundMatch)
            {
                connection.close("Already connected from a different connection!");
                return;
            }

            ConvoUser user = server.getAuthHandler().loginUser(username, password);
            if (user != null)
            {
                connection.setUser(user);
                connection.initUser(packet.getUsername());
                connection.sendPacket(new ServerLoginSuccessPacket(user.getName(), user.getId()));

                connection.sendPacket(new ServerEncryptionRequestPacket(server.getAuthHandler().getPublicKey(), server.getAuthHandler().genVerificationToken(packet.getUsername())));
            }
            else
            {
                connection.sendPacket(new ServerLoginFailPacket("Invalid login information"));
            }
        }
        else if (msg instanceof ClientRegisterRequestPacket packet)
        {
            String username = packet.getUsername();
            String password = packet.getPasswordHash();
            String salt = packet.getSalt();

            log.info("Received login packet from {} with username {}", connection.getRemoteAddress(), username);

            boolean foundMatch = connections.stream()
                    .filter(c -> c.getUser() != null && c.getUser().getName() != null)
                    .anyMatch(c -> c.getUser().getName().equalsIgnoreCase(packet.getUsername()));

            if (foundMatch)
            {
                connection.close("Already connected from a different connection!");
                return;
            }

            ConvoUser user = server.getAuthHandler().registerUser(username, password, salt);
            if (user != null)
            {
                connection.setUser(user);
                connection.initUser(packet.getUsername());
                connection.sendPacket(new ServerLoginSuccessPacket(user.getName(), user.getId()));
                connection.sendPacket(new ServerEncryptionRequestPacket(server.getAuthHandler().getPublicKey(), server.getAuthHandler().genVerificationToken(packet.getUsername())));
            }
            else
            {
                connection.sendPacket(new ServerRegisterResponsePacket(false,"Error occurred registering"));
            }
        }
        else if (msg instanceof ClientEncryptionResponsePacket) {
            ClientEncryptionResponsePacket packet = (ClientEncryptionResponsePacket) msg;
            server.getAuthHandler().auth(packet, connection);
            connection.sendPacket(new ServerAuthFinishedPacket());
        }
        else if (!connection.isAuthFinished()) {
            // everything below requires auth, so stop if not authed
            log.warn("Can't accept packet {} from {} without being logged in!", msg, connection);
            connection.close("Sending " + msg.getClass().getName() + " without being logged in");
        }
        else if (msg instanceof ClientPingPacket clientPingPacket)
        {
            log.info("Received ping packet from {} with payload {}", connection.getRemoteAddress(), clientPingPacket.getPayload());
            connection.sendPacket(new ServerPongPacket(clientPingPacket.getPayload()));
        }
    }
}