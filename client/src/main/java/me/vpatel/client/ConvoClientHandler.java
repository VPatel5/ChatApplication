package me.vpatel.client;

import me.vpatel.client.ui.LoginUI;
import me.vpatel.client.ui.MainUI;
import me.vpatel.client.ui.RegisterUI;
import me.vpatel.client.ui.UIScreenManager;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoGroup;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvoClientHandler extends ConvoHandler {

    private static final Logger log = LogManager.getLogger(ConvoClientHandler.class);

    private static final ConvoClient client = AppContext.getClient();
    private ConvoConnection connection;

    public ConvoClientHandler() {
    }

    @Override
    public void join(ConvoConnection connection) {
        this.connection = connection;
        log.info("Joined connection: {}", connection);
    }

    @Override
    public void leave(ConvoConnection connection) {
        this.connection = null;
        log.info("Connection closed: {}", connection);
        client.getConsole().shutdown();
    }

    @Override
    public void handle(ConvoConnection connection, ConvoPacket msg) {
        log.debug("Received packet: {}", msg.getClass().getSimpleName());
        if (msg instanceof ServerLoginFailPacket packet) {
            log.info("Login failed: {}", packet.getMessage());
            if (UIScreenManager.getCurrentFrame() instanceof LoginUI ui) {
                ui.statusLabel.setText(packet.getMessage());
            }
        } else if (msg instanceof ServerLoginSuccessPacket packet) {
            client.setUser(new ConvoUser(packet.getUuid(), packet.getUsername()));
            log.info("Login successful: {}", client.getUser());
        } else if (msg instanceof ServerRegisterResponsePacket packet) {
            if (packet.isSuccess()) {
                log.info("Registration successful for user");
            } else {
                log.warn("Registration failed for user");
                if (UIScreenManager.getCurrentFrame() instanceof RegisterUI ui) {
                    ui.statusLabel.setText("Error occurred registering");
                }
            }
        } else if (msg instanceof ServerEncryptionRequestPacket packet) {
            log.info("Encryption request received, proceeding with auth");
            client.getAuthHandler().auth(packet, connection);
        } else if (msg instanceof ServerAuthFinishedPacket packet) {
            log.info("Authentication finished successfully");
            connection.setAuthFinished(true);
            UIScreenManager.showScreen(new MainUI(AppContext.getClient()));
        } else if (!connection.isAuthFinished()) {
            String error = "Cannot accept packet " + msg + " without being logged in";
            log.error(error);
            throw new RuntimeException(error);
        } else if (msg instanceof ServerPongPacket packet) {
            log.info("PONG received: {}", packet.getPayload());
        } else if (msg instanceof ServerResponsePacket packet) {
            log.info("Server response [{}]: {}", packet.getType(), packet.getMessage());
        } else if (msg instanceof ServerListResponsePacket packet) {
            log.info("List response received: {} ({} items)", packet.getType(),
                    packet.getInvites() != null ? packet.getInvites().size() :
                            packet.getUsers() != null ? packet.getUsers().size() :
                                    packet.getFriends() != null ? packet.getFriends().size() : 0);
            switch (packet.getType()) {
                case CONVO_USERS -> {
                    client.getClientApi().setUsers(packet.getUsers());
                    log.info("Loaded {} users", packet.getUsers().size());
                }
                case INCOMING_FRIEND_INVITES -> {
                    client.getClientApi().setIncomingFriendInvites(packet.getInvites());
                    log.info("Loaded {} incoming friend invites", packet.getInvites().size());
                }
                case OUTGOING_FRIEND_INVITES -> {
                    client.getClientApi().setOutgoingFriendInvites(packet.getInvites());
                    log.info("Loaded {} outgoing friend invites", packet.getInvites().size());
                }
                case FRIENDS -> {
                    client.getClientApi().setFriends(packet.getFriends());
                    log.info("Loaded {} friends", packet.getFriends().size());
                }
                case INCOMING_GROUP_INVITES -> {
                    Map<String, List<Invite>> map = new HashMap<>();
                    for (Invite invite : packet.getInvites()) {
                        map.computeIfAbsent(invite.getGroup().getName(), k -> new ArrayList<>()).add(invite);
                    }
                    client.getClientApi().setIncomingGroupInvites(map);
                    log.info("Loaded incoming group invites for {} groups", map.size());
                }
                case OUTGOING_GROUP_INVITES -> {
                    Map<String, List<Invite>> map = new HashMap<>();
                    for (Invite invite : packet.getInvites()) {
                        map.computeIfAbsent(invite.getGroup().getName(), k -> new ArrayList<>()).add(invite);
                    }
                    client.getClientApi().setOutgoingGroupInvites(map);
                    log.info("Loaded outgoing group invites for {} groups", map.size());
                }
                case MESSAGES -> {
                    client.getClientApi().getMessages().put(packet.getGroupName(), packet.getMessages());
                    log.info("Loaded {} messages for group {}", packet.getMessages().size(), packet.getGroupName());
                }
                case GROUPS -> {
                    client.getClientApi().setGroups(packet.getGroups());
                    log.info("Loaded {} groups", packet.getGroups().size());
                }
            }
        } else {
            log.warn("Unhandled packet type: {}", msg.getClass().getSimpleName());
        }
    }

    public ConvoConnection getConnection() {
        return connection;
    }
}