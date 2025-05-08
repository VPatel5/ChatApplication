package me.vpatel.server;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.client.*;
import me.vpatel.network.protocol.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
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
        else if (msg instanceof ClientListRequestPacket packet) {
            switch (packet.getType()) {
                case FALCUN_USERS ->
                        connection.sendPacket(new ServerListResponsePacket(getUsers(), true));
                case INCOMING_FRIEND_INVITES ->
                        connection.sendPacket(new ServerListResponsePacket(packet.getType(), server.getFriendsHandler().getIncomingInvites(connection.getUser())));
                case OUTGOING_FRIEND_INVITES ->
                        connection.sendPacket(new ServerListResponsePacket(packet.getType(), server.getFriendsHandler().getOutgoingInvites(connection.getUser())));
                case INCOMING_GROUP_INVITES ->
                        connection.sendPacket(new ServerListResponsePacket(packet.getType(), server.getGroupsHandler().getIncomingInvites(connection.getUser())));
                case OUTGOING_GROUP_INVITES ->
                        connection.sendPacket(new ServerListResponsePacket(packet.getGroupName(), server.getGroupsHandler().getOutgoingInvites(packet.getGroupName(), connection.getUser())));
                case FRIENDS ->
                        connection.sendPacket(new ServerListResponsePacket(server.getFriendsHandler().getFriends(connection.getUser())));
                case GROUPS ->
                        connection.sendPacket(new ServerListResponsePacket(server.getGroupsHandler().getGroups(connection.getUser(), true), 1337));
                case MESSAGES ->
                        connection.sendPacket(new ServerListResponsePacket(server.getGroupsHandler().getMessages(packet.getGroupName(), connection.getUser()), packet.getGroupName()));
                default -> {
                    connection.sendPacket(new ServerResponsePacket("Unknown list request type", ServerResponsePacket.ResponseType.ERROR));
                    log.error("Unknown list type {}", packet.getType());
                }
            }
        }
        else if (msg instanceof ClientActionPacket packet) {

        }
    }

    public ConvoConnection getConnection(ConvoUser user)
    {
        return connections.stream()
                .filter(c -> c.getUser().getInternalId() == user.getInternalId())
                .findFirst()
                .orElse(null);
    }

    public List<ConvoUser> getUsers() {
        return connections.stream()
                .filter(ConvoConnection::isAuthFinished)
                .map(ConvoConnection::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}