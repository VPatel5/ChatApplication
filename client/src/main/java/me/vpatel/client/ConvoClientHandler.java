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

    private List<ConvoUser> users = new ArrayList<>();
    private List<ConvoUser> friends = new ArrayList<>();
    private List<ConvoGroup> groups = new ArrayList<>();
    private List<Invite> incomingFriendInvites = new ArrayList<>();
    private List<Invite> outgoingFriendInvites = new ArrayList<>();
    private Map<String, List<Invite>> incomingGroupInvites = new HashMap<>();
    private Map<String, List<Invite>> outgoingGroupInvites = new HashMap<>();
    private Map<String, List<Message>> messages = new HashMap<>();

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
        if (msg instanceof ServerLoginFailPacket packet)
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
            UIScreenManager.showScreen(new MainUI(AppContext.getClient()));
        }
        else if (!connection.isAuthFinished())
        {
            throw new RuntimeException("Can't accept packet " + msg + " without being logged in!");
        }
        else if (msg instanceof ServerPongPacket packet)
        {
            log.info("PONG! {}", packet.getPayload());
        }
        else if (msg instanceof ServerResponsePacket)
        {
            ServerResponsePacket packet = (ServerResponsePacket) msg;
            log.info("Got response: {} {}", packet.getType(), packet.getMessage());
        }
        else if (msg instanceof ServerListResponsePacket)
        {
            ServerListResponsePacket packet = (ServerListResponsePacket) msg;

            switch (packet.getType()) {
                case FALCUN_USERS -> users = packet.getUsers();
                case INCOMING_FRIEND_INVITES -> incomingFriendInvites = packet.getInvites();
                case OUTGOING_FRIEND_INVITES -> outgoingFriendInvites = packet.getInvites();
                case FRIENDS -> friends = packet.getFriends();
                case INCOMING_GROUP_INVITES -> {
                    incomingGroupInvites = new HashMap<>();
                    for (Invite invite : packet.getInvites()) {
                        List<Invite> invites = incomingGroupInvites.get(invite.getGroup().getName());
                        if (invites == null) {
                            invites = new ArrayList<>();
                        }
                        invites.add(invite);
                        incomingGroupInvites.put(invite.getGroup().getName(), invites);
                    }
                }
                case OUTGOING_GROUP_INVITES -> {
                    outgoingGroupInvites = new HashMap<>();
                    for (Invite invite : packet.getInvites()) {
                        List<Invite> invites = outgoingGroupInvites.get(invite.getGroup().getName());
                        if (invites == null) {
                            invites = new ArrayList<>();
                        }
                        invites.add(invite);
                        outgoingGroupInvites.put(invite.getGroup().getName(), invites);
                    }
                }
                case MESSAGES -> {
                    this.messages.put(packet.getGroupName(), packet.getMessages());
                    log.info("Got {} messages for group {}", packet.getMessages().size(), packet.getGroupName());
                }
                case GROUPS -> groups = packet.getGroups();
            }
        }
    }

    public ConvoConnection getConnection() {
        return connection;
    }
}
