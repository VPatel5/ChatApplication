package me.vpatel.server;

import io.netty.util.concurrent.Promise;
import me.vpatel.db.MessageDao;
import me.vpatel.db.tables.MessageTable;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.Message;
import me.vpatel.network.api.MessageType;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.client.*;
import me.vpatel.network.protocol.server.*;
import me.vpatel.network.protocol.server.ServerResponsePacket.ResponseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
        log.info("Connection joined: {} (total {})", connection.getRemoteAddress(), connections.size());
    }

    @Override
    public void leave(ConvoConnection connection) {
        connections.remove(connection);
        log.info("Connection left: {} (remaining {})", connection.getRemoteAddress(), connections.size());
    }

    @Override
    public void handle(ConvoConnection connection, ConvoPacket msg) {
        log.debug("Handling packet {} from {}", msg.getClass().getSimpleName(), connection.getRemoteAddress());

        if (msg instanceof ClientLoginStartPacket packet) {
            String username = packet.getUsername();
            log.info("Login attempt from {} as '{}'", connection.getRemoteAddress(), username);

            ConvoUser user = server.getAuthHandler().loginUser(username, packet.getPassword());
            if (user != null) {

                boolean already = connections.stream()
                        .filter(c -> c.getUser() != null && c.getUser().getName() != null)
                        .anyMatch(c -> c.getUser().getName().equalsIgnoreCase(username));

                if (already) {
                    log.warn("Rejecting login for '{}' - already connected", username);
                    connection.close("User already connected");
                    return;
                }

                connection.setUser(user);
                connection.initUser(username);
                log.info("Login successful for '{}'", username);
                connection.sendPacket(new ServerLoginSuccessPacket(user.getName(), user.getId()));
                connection.sendPacket(new ServerEncryptionRequestPacket(
                        server.getAuthHandler().getPublicKey(),
                        server.getAuthHandler().genVerificationToken(username)));
            } else {
                log.warn("Login failed for '{}'", username);
                connection.sendPacket(new ServerLoginFailPacket("Invalid login information"));
            }

        } else if (msg instanceof ClientRegisterRequestPacket packet) {
            String username = packet.getUsername();
            log.info("Registration attempt from {} as '{}'", connection.getRemoteAddress(), username);

            boolean exists = connections.stream()
                    .filter(c -> c.getUser() != null && c.getUser().getName() != null)
                    .anyMatch(c -> c.getUser().getName().equalsIgnoreCase(username));
            if (exists) {
                log.warn("Rejecting registration for '{}' - already connected", username);
                connection.close("User already connected");
                return;
            }

            ConvoUser user = server.getAuthHandler().registerUser(username, packet.getPasswordHash(), packet.getSalt());
            if (user != null) {
                connection.setUser(user);
                connection.initUser(username);
                log.info("Registration successful for '{}'", username);
                connection.sendPacket(new ServerLoginSuccessPacket(user.getName(), user.getId()));
                connection.sendPacket(new ServerEncryptionRequestPacket(
                        server.getAuthHandler().getPublicKey(),
                        server.getAuthHandler().genVerificationToken(username)));
            } else {
                log.error("Registration failed for '{}'", username);
                connection.sendPacket(new ServerRegisterResponsePacket(false, "Error occurred registering"));
            }

        } else if (msg instanceof ClientEncryptionResponsePacket) {
            log.info("Received encryption response from {}", connection.getRemoteAddress());
            server.getAuthHandler().auth((ClientEncryptionResponsePacket) msg, connection);
            connection.sendPacket(new ServerAuthFinishedPacket());
            log.info("Authentication finished for {}", connection.getRemoteAddress());

        } else if (!connection.isAuthFinished()) {
            log.warn("Unauthenticated packet {} from {} - closing", msg.getClass().getSimpleName(), connection.getRemoteAddress());
            connection.close("Not authenticated");

        } else if (msg instanceof ClientPingPacket ping) {
            log.info("Ping received from {}: {}", connection.getRemoteAddress(), ping.getPayload());
            connection.sendPacket(new ServerPongPacket(ping.getPayload()));

        } else if (msg instanceof ClientListRequestPacket req) {
            log.debug("List request '{}' from {}", req.getType(), connection.getUser().getName());
            switch (req.getType()) {
                case CONVO_USERS -> {
                    var all = server.getUsersHandler().getAll();
                    connection.sendPacket(new ServerListResponsePacket(all, true));
                    log.debug("Sent {} users", all.size());
                }
                case INCOMING_FRIEND_INVITES -> {
                    var invites = server.getFriendsHandler().getIncomingInvites(connection.getUser());
                    connection.sendPacket(new ServerListResponsePacket(req.getType(), invites));
                    log.debug("Sent {} incoming friend invites", invites.size());
                }
                case OUTGOING_FRIEND_INVITES -> {
                    var invites = server.getFriendsHandler().getOutgoingInvites(connection.getUser());
                    connection.sendPacket(new ServerListResponsePacket(req.getType(), invites));
                    log.debug("Sent {} outgoing friend invites", invites.size());
                }
                case FRIENDS -> {
                    var friends = server.getFriendsHandler().getFriends(connection.getUser());
                    connection.sendPacket(new ServerListResponsePacket(friends));
                    log.debug("Sent {} friends", friends.size());
                }
                case GROUPS -> {
                    var groups = server.getGroupsHandler().getGroups(connection.getUser(), true);
                    connection.sendPacket(new ServerListResponsePacket(groups, 0));
                    log.debug("Sent {} groups", groups.size());
                }
                case INCOMING_GROUP_INVITES -> {
                    var invites = server.getGroupsHandler().getIncomingInvites(connection.getUser());
                    connection.sendPacket(new ServerListResponsePacket(req.getType(), invites));
                    log.debug("Sent {} incoming group invites", invites.size());
                }
                case OUTGOING_GROUP_INVITES -> {
                    var invites = server.getGroupsHandler().getOutgoingInvites(req.getGroupName(), connection.getUser());
                    connection.sendPacket(new ServerListResponsePacket(req.getGroupName(), invites));
                    log.debug("Sent {} outgoing group invites for {}", invites.size(), req.getGroupName());
                }
                default -> {
                    log.error("Unknown list type {} from {}", req.getType(), connection.getRemoteAddress());
                    connection.sendPacket(new ServerResponsePacket("Unknown list request type", ResponseType.ERROR));
                }
            }

        } else if (msg instanceof ClientActionPacket action) {
            log.info("Action '{}' from {}: {}", action.getAction(), connection.getUser().getName(), action);
            String status;
            switch (action.getAction()) {
                case SEND_FRIEND_INVITE, SEND_GROUP_INVITE -> {
                    Invite invite = (action.getAction() == ClientActionPacket.Action.SEND_FRIEND_INVITE)
                            ? server.getFriendsHandler().sendInvite(connection.getUser(), action.getUsername())
                            : server.getGroupsHandler().sendInvite(action.getGroup(), connection.getUser(), action.getUsername());
                    if (invite != null) {
                        ConvoConnection target = server.getHandler().getConnection(invite.getUser());
                        if (target != null) {
                            target.sendPacket(new ServerInviteStatusPacket(invite, ServerInviteStatusPacket.InviteStatus.NEW));
                            log.info("Notified {} of new invite to {}", invite.getUser().getName(), invite.getInviter().getName());
                        }
                        connection.sendPacket(new ServerResponsePacket("OK", ResponseType.OK));
                        log.info("Invite created successfully");
                    } else {
                        connection.sendPacket(new ServerResponsePacket("Error", ResponseType.ERROR));
                        log.error("Failed to create invite");
                    }
                    return;
                }
                case ACCEPT_FRIEND_INVITE, ACCEPT_GROUP_INVITE -> {
                    status = (action.getAction() == ClientActionPacket.Action.ACCEPT_FRIEND_INVITE)
                            ? server.getFriendsHandler().acceptInvite(connection.getUser(), action.getInvite())
                            : server.getGroupsHandler().acceptInvite(connection.getUser(), action.getInvite());
                    log.info("Invite {} accepted, status: {}", action.getInvite(), status);
                }
                case DECLINE_FRIEND_INVITE, DECLINE_GROUP_INVITE -> {
                    status = (action.getAction() == ClientActionPacket.Action.DECLINE_FRIEND_INVITE)
                            ? server.getFriendsHandler().declineInvite(connection.getUser(), action.getInvite())
                            : server.getGroupsHandler().declineInvite(connection.getUser(), action.getInvite());
                    log.info("Invite {} declined, status: {}", action.getInvite(), status);
                }
                case REVOKE_FRIEND_INVITE, REVOKE_GROUP_INVITE -> {
                    status = (action.getAction() == ClientActionPacket.Action.REVOKE_FRIEND_INVITE)
                            ? server.getFriendsHandler().revokeInvite(connection.getUser(), action.getInvite())
                            : server.getGroupsHandler().revokeInvite(connection.getUser(), action.getInvite());
                    log.info("Invite {} revoked, status: {}", action.getInvite(), status);
                }
                case REMOVE_FRIEND -> {
                    status = server.getFriendsHandler().removeUser(connection.getUser(), action.getUser());
                    log.info("Friend {} removed, status: {}", action.getUser().getName(), status);
                }
                case KICK_USER -> {
                    status = server.getGroupsHandler().kick(action.getGroup(), connection.getUser(), action.getUser());
                    log.info("User {} kicked from group {}, status: {}", action.getUser().getName(), action.getGroup().getName(), status);
                }
                case CREATE_GROUP -> {
                    status = server.getGroupsHandler().createGroup(action.getGroupName(), connection.getUser());
                    log.info("Group {} creation by {}, status: {}", action.getGroupName(), connection.getUser().getName(), status);
                }
                case DELETE_GROUP -> {
                    status = server.getGroupsHandler().deleteGroup(action.getGroupName(), connection.getUser());
                    log.info("Group {} deletion by {}, status: {}", action.getGroupName(), connection.getUser().getName(), status);
                }
                default -> {
                    log.warn("Unknown action {} from {}", action.getAction(), connection.getUser().getName());
                    connection.sendPacket(new ServerResponsePacket("Unknown action", ResponseType.ERROR));
                    return;
                }
            }
            connection.sendPacket(new ServerResponsePacket(status, "OK".equals(status) ? ResponseType.OK : ResponseType.ERROR));
        } else if (msg instanceof ClientDirectMessagePacket packet) {
            log.debug("Received ClientDMPacket Type {}", packet.getMessageType());
            ConvoUser user = server.getUsersHandler().getUser(packet.getUser());
            if (user == null) {
                connection.sendPacket(new ServerResponsePacket("Unknown user", ResponseType.WARNING));
            } else {
                String response = server.getFriendsHandler().chat(connection.getUser(), user, packet.getMessage());
                connection.sendPacket(new ServerResponsePacket(response, "Send".equals(response) ? ResponseType.OK : ResponseType.WARNING));
            }
        } else if (msg instanceof ClientGroupMessagePacket packet) {
            log.debug("Received ClientGMPacket Type {}", packet.getMessageType());
            String response = server.getGroupsHandler().chat(packet.getName(), connection.getUser(), packet.getMessage());
            connection.sendPacket(new ServerResponsePacket(response, "Send".equals(response) ? ResponseType.OK : ResponseType.WARNING));
        }
        else if (msg instanceof ClientDirectMessagesRequestPacket packet)
        {
            List<Message> messages = server.getGroupsHandler().getMessages(connection.getUser());
            if (messages == null)
            {
                messages = new ArrayList<>();
            }
            connection.sendPacket(new ServerDirectMessagesReponsePacket(messages));
        }
        else if (msg instanceof ClientGroupMessagesRequestPacket packet)
        {
            List<Message> messages = server.getGroupsHandler().getMessages(packet.getGroupName(), connection.getUser());
            if (messages == null)
            {
                messages = new ArrayList<>();
            }
            connection.sendPacket(new ServerGroupMessagesReponsePacket(messages, packet.getGroupName()));
        }
        else if (msg instanceof ClientGeminiRequestPacket packet)
        {
            log.debug("Received Gemini Request: {}, {}", packet.getMessageHistory(), packet.getUserInput());
            CompletableFuture<String> future = OpenAIClientProvider.askGemini(packet.getMessageHistory(), packet.getUserInput());


            future.whenComplete((response, error) -> {
                MessageTable aiResponse = new MessageTable();
                aiResponse.setSenderId(0); // AI user has ID 0
                aiResponse.setRecipientId(connection.getUser().getInternalId());
                aiResponse.setTimestamp(OffsetDateTime.now());
                aiResponse.setGroupId(-1);
                aiResponse.setMessage(response);

                server.getDbHandler().jdbi().withExtension(MessageDao.class, handle -> {
                    handle.saveMessage(aiResponse);
                    return null;
                });

                connection.sendPacket(new ServerGeminiResponsePacket(response));
            });
        }
    }

    public ConvoConnection getConnection(ConvoUser user) {
        return connections.stream()
                .filter(c -> c.getUser() != null && c.getUser().getId().equals(user.getId()))
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