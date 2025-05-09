package me.vpatel.client;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoGroup;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.client.ClientListRequestPacket;
import me.vpatel.network.protocol.server.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConvoClientHandler extends ConvoHandler {

    private static final Logger log = LogManager.getLogger(ConvoClientHandler.class);
    private static final ConvoClient client = AppContext.getClient();

    private ConvoConnection connection;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    /** Subscribe to connection events */
    public void addListener(Listener l) {
        listeners.add(l);
    }

    /** Unsubscribe */
    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    @Override
    public void join(ConvoConnection conn) {
        this.connection = conn;
        log.info("Joined connection: {}", conn);
        listeners.forEach(l -> l.onConnect(conn));
    }

    @Override
    public void leave(ConvoConnection conn) {
        this.connection = null;
        log.info("Connection closed: {}", conn);
        client.getConsole().shutdown();
        listeners.forEach(Listener::onDisconnect);
    }

    @Override
    public void handle(ConvoConnection conn, ConvoPacket msg) {
        log.debug("Received packet: {}", msg.getClass().getSimpleName());

        // LOGIN FAIL
        if (msg instanceof ServerLoginFailPacket fail) {
            log.info("Login failed: {}", fail.getMessage());
            listeners.forEach(l -> l.onLoginFailed(fail.getMessage()));

            // LOGIN SUCCESS
        } else if (msg instanceof ServerLoginSuccessPacket success) {
            ConvoUser user = new ConvoUser(success.getUuid(), success.getUsername());
            client.setUser(user);
            log.info("Login successful: {}", user);
            listeners.forEach(l -> l.onLoginSuccess(user));

            // REGISTER RESPONSE
        } else if (msg instanceof ServerRegisterResponsePacket reg) {
            if (reg.isSuccess()) {
                log.info("Registration succeeded");
                listeners.forEach(Listener::onRegisterSuccess);
            } else {
                log.warn("Registration failed");
                listeners.forEach(l -> l.onRegisterFailed(reg.getMessage()));
            }

            // ENCRYPTION REQUEST
        } else if (msg instanceof ServerEncryptionRequestPacket encReq) {
            log.info("Received encryption request");
            listeners.forEach(Listener::onEncryptionRequest);
            client.getAuthHandler().auth(encReq, connection);

            // AUTH FINISHED
        } else if (msg instanceof ServerAuthFinishedPacket authFin) {
            log.info("Authentication finished");
            connection.setAuthFinished(true);
            listeners.forEach(Listener::onAuthFinished);

            // NOT AUTHENTICATED
        } else if (!connection.isAuthFinished()) {
            log.error("Packet {} before auth, closing", msg.getClass().getSimpleName());
            connection.close("Not authenticated");
            listeners.forEach(l -> l.onError("Received " + msg + " before auth"));

            // PONG
        } else if (msg instanceof ServerPongPacket pong) {
            log.info("PONG: {}", pong.getPayload());
            listeners.forEach(l -> l.onPong(pong.getPayload()));

            // GENERIC RESPONSE
        } else if (msg instanceof ServerResponsePacket resp) {
            log.info("Response [{}]: {}", resp.getType(), resp.getMessage());
            listeners.forEach(l -> l.onResponse(resp.getType(), resp.getMessage()));

            // LIST RESPONSE
        } else if (msg instanceof ServerListResponsePacket list) {
            log.debug("List {} with {} items", list.getType(),
                    Optional.ofNullable(list.getUsers()).map(List::size)
                            .or(() -> Optional.ofNullable(list.getFriends()).map(List::size))
                            .or(() -> Optional.ofNullable(list.getInvites()).map(List::size))
                            .orElse(0)
            );
            // dispatch by type
            switch (list.getType()) {
                case CONVO_USERS -> {
                    client.getClientApi().setUsers(list.getUsers());
                    listeners.forEach(l -> l.onUsersList(list.getUsers()));
                }
                case FRIENDS -> {
                    client.getClientApi().setFriends(list.getFriends());
                    listeners.forEach(l -> l.onFriendsList(list.getFriends()));
                }
                case INCOMING_FRIEND_INVITES -> {
                    client.getClientApi().setIncomingFriendInvites(list.getInvites());
                    listeners.forEach(l -> l.onIncomingFriendInvites(list.getInvites()));
                }
                case OUTGOING_FRIEND_INVITES -> {
                    client.getClientApi().setOutgoingFriendInvites(list.getInvites());
                    listeners.forEach(l -> l.onOutgoingFriendInvites(list.getInvites()));
                }
                case INCOMING_GROUP_INVITES -> {
                    // build map
                    Map<String, List<Invite>> inMap = new HashMap<>();
                    for (Invite i : list.getInvites()) {
                        inMap.computeIfAbsent(i.getGroup().getName(), k -> new ArrayList<>()).add(i);
                    }
                    client.getClientApi().setIncomingGroupInvites(inMap);
                    listeners.forEach(l -> l.onIncomingGroupInvites(inMap));
                }
                case OUTGOING_GROUP_INVITES -> {
                    Map<String, List<Invite>> outMap = new HashMap<>();
                    for (Invite i : list.getInvites()) {
                        outMap.computeIfAbsent(i.getGroup().getName(), k -> new ArrayList<>()).add(i);
                    }
                    client.getClientApi().setOutgoingGroupInvites(outMap);
                    listeners.forEach(l -> l.onOutgoingGroupInvites(outMap));
                }
                case GROUPS -> {
                    client.getClientApi().setGroups(list.getGroups());
                    listeners.forEach(l -> l.onGroupsList(list.getGroups()));
                }
                case MESSAGES -> {
                    client.getClientApi().getMessages()
                            .put(list.getGroupName(), list.getMessages());
                    listeners.forEach(l -> l.onMessages(list.getGroupName(), list.getMessages()));
                }
            }

            // UNHANDLED
        } else {
            log.warn("Unhandled packet: {}", msg.getClass().getSimpleName());
            listeners.forEach(l -> l.onUnhandled(msg));
        }
    }

    public ConvoConnection getConnection() {
        return connection;
    }

    /** Listener interface for packet-based events */
    public interface Listener {
        default void onConnect(ConvoConnection conn) {}
        default void onDisconnect() {}
        default void onLoginSuccess(ConvoUser user) {}
        default void onLoginFailed(String message) {}
        default void onRegisterSuccess() {}
        default void onRegisterFailed(String message) {}
        default void onEncryptionRequest() {}
        default void onAuthFinished() {}
        default void onPong(String payload) {}
        default void onResponse(ServerResponsePacket.ResponseType type, String message) {}
        default void onUsersList(List<ConvoUser> users) {}
        default void onFriendsList(List<ConvoUser> friends) {}
        default void onIncomingFriendInvites(List<Invite> invites) {}
        default void onOutgoingFriendInvites(List<Invite> invites) {}
        default void onIncomingGroupInvites(Map<String,List<Invite>> invites) {}
        default void onOutgoingGroupInvites(Map<String,List<Invite>> invites) {}
        default void onGroupsList(List<ConvoGroup> groups) {}
        default void onMessages(String target, List<Message> messages) {}
        default void onUnhandled(ConvoPacket packet) {}
        default void onError(String error) {}
    }

    public void requestAllLists()
    {
        Arrays.stream(ClientListRequestPacket.ListType.values()).forEach(client.getClientApi()::list);
    }
}
