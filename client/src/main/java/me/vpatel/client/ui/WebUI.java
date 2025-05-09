package me.vpatel.client.ui;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import me.vpatel.client.AppContext;
import me.vpatel.client.ConvoClient;
import me.vpatel.client.ConvoClientHandler;
import me.vpatel.client.api.ClientApi;
import me.vpatel.network.api.ConvoGroup;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.api.Invite;
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.server.ServerResponsePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebUI extends Application {
    private static final Logger log = LogManager.getLogger(WebUI.class);
    private static final ConvoClient client = AppContext.getClient();
    private static final ClientApi api = client.getClientApi();
    private static final Gson gson = new Gson();

    private String currentConversation;
    private String currentType; // "friend" or "group"

    private WebEngine engine;
    private ScheduledExecutorService scheduler1, scheduler2;

    public static void launchUI() { launch(); }

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        engine = webView.getEngine();
        engine.setOnAlert(evt -> handleCommand(evt.getData()));

        // 1) show login / register
        engine.load(getClass().getResource("/ui/login.html").toExternalForm());

        // 2) on auth, fetch *all* data and swap to index.html
        client.getHandler().addListener(new ConvoClientHandler.Listener() {
            @Override
            public void onAuthFinished() {
                startDataRefreshTimer();
                Platform.runLater(() ->
                        engine.load(getClass().getResource("/ui/index.html").toExternalForm())
                );
            }

            @Override
            public void onUsersList(List<ConvoUser> list) {
                runJS("populateUsers",
                        list.stream().map(ConvoUser::getName).toArray(String[]::new));
            }

            @Override
            public void onFriendsList(List<ConvoUser> list) {
                runJS("populateFriends",
                        list.stream().map(ConvoUser::getName).toArray(String[]::new));
            }

            @Override
            public void onGroupsList(List<ConvoGroup> list) {
                runJS("populateGroups",
                        list.stream().map(ConvoGroup::getName).toArray(String[]::new));
            }

            @Override
            public void onIncomingFriendInvites(List<Invite> list) {
                runJS("populateFriendRequests",
                        list.stream()
                                .map(inv -> inv.getInviter().getName())
                                .toArray(String[]::new));
            }

            @Override
            public void onOutgoingFriendInvites(List<Invite> list) {
                runJS("populateOutgoingFriendRequests",
                        list.stream()
                                .map(Invite::getInviter)
                                .map(ConvoUser::getName)
                                .toArray(String[]::new));
            }

            @Override
            public void onIncomingGroupInvites(Map<String, List<Invite>> invites) {
                // No UI update needed unless viewing group invites
            }

            @Override
            public void onOutgoingGroupInvites(Map<String, List<Invite>> invites) {
                // No UI update needed unless viewing group invites
            }

            @Override
            public void onDirectMessages(List<Message> msgs) {
                if ("friend".equals(currentType)) {

                    List<String> lines = api.getDirectMessages().stream()
                            .filter(m -> m.getSender().getName().equals(currentConversation)
                                    || m.getRecipient().getName().equals(currentConversation))
                            .map(m -> m.getSender().getName() + ": " + m.getMessage())
                            .toList();
                    runJS("populateDirectMessages", lines.toArray(String[]::new));
                }
            }

            @Override
            public void onGroupMessages(String group, List<Message> msgs) {
                if ("group".equals(currentType) && group.equals(currentConversation)) {
                    List<String> lines = msgs.stream()
                            .map(m -> m.getSender().getName() + ": " + m.getMessage())
                            .toList();
                    runJS("populateGroupMessages", Map.of(
                            "group", group,
                            "messages", lines.toArray(new String[0])
                    ));
                }
            }

            @Override
            public void onResponse(ServerResponsePacket.ResponseType type, String message) {
                runJS("showFeedback", message);
            }
        });

        stage.setScene(new Scene(webView, 900, 600));
        stage.setTitle("Convo Chat");
        stage.setOnCloseRequest(e -> {
            if (client.getHandler().getConnection() != null)
            {
                client.getHandler().getConnection().close("Shutdown");
            }
            if (scheduler1 != null) {
                scheduler1.shutdown();
            }
            if (scheduler2 != null) {
                scheduler2.shutdown();
            }
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    private void startDataRefreshTimer() {
        if (scheduler1 != null) {
            scheduler1.shutdown();
        }
        scheduler1 = Executors.newSingleThreadScheduledExecutor();
        scheduler1.scheduleAtFixedRate(() -> {
            client.getHandler().requestAllLists();
        }, 0, 2000, TimeUnit.MILLISECONDS);

        if (scheduler2 != null) {
            scheduler2.shutdown();
        }
        scheduler2 = Executors.newSingleThreadScheduledExecutor();
        scheduler2.scheduleAtFixedRate(() -> {
            api.requestDirectMessages();
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    private void handleCommand(String data) {
        Map<String, Object> cmd = gson.fromJson(data, Map.class);
        String action = (String) cmd.get("action");

        switch (action) {
            case "login" -> client.getAuthHandler().login(
                    (String) cmd.get("username"),
                    (String) cmd.get("password")
            );
            case "openRegister" -> Platform.runLater(() ->
                    engine.load(getClass().getResource("/ui/register.html").toExternalForm())
            );
            case "register" -> client.getAuthHandler().registerUser(
                    (String) cmd.get("username"),
                    (String) cmd.get("password"),
                    (String) cmd.get("email")
            );
            case "openLogin" -> Platform.runLater(() ->
                    engine.load(getClass().getResource("/ui/login.html").toExternalForm())
            );

            case "searchFriends",
                    "listFriends" -> runJS("populateFriends",
                    api.getFriends().stream().map(ConvoUser::getName).toArray(String[]::new));

            case "searchGroups",
                    "listGroups" -> runJS("populateGroups",
                    api.getGroups().stream().map(ConvoGroup::getName).toArray(String[]::new));

            case "createGroup" -> {
                String name = (String) cmd.get("groupName");
                api.createGroup(name);
            }

            case "selectFriend" -> {
                currentConversation = (String) cmd.get("target");
                currentType = "friend";
                List<String> lines = api.getDirectMessages().stream()
                        .filter(m -> m.getSender().getName().equals(currentConversation)
                                || m.getRecipient().getName().equals(currentConversation))
                        .map(m -> m.getSender().getName() + ": " + m.getMessage())
                        .toList();
                runJS("populateDirectMessages", lines.toArray(String[]::new));
            }
            case "selectGroup" -> {
                currentConversation = (String) cmd.get("target");
                currentType = "group";
                List<String> lines = api.getGroupMessages().getOrDefault(currentConversation, List.of())
                        .stream()
                        .map(m -> m.getSender().getName() + ": " + m.getMessage())
                        .toList();
                runJS("populateGroupMessages", Map.of(
                        "group", currentConversation,
                        "messages", lines.toArray(new String[0])
                ));
            }

            case "sendFriendMessage" -> {
                String tgt = (String) cmd.get("target");
                String msg = (String) cmd.get("message");
                api.chat(tgt, msg);
            }
            case "sendGroupMessage" -> {
                String grp = (String) cmd.get("target");
                String msg = (String) cmd.get("message");
                api.groupChat(grp, msg);
            }

            case "removeFriend" -> {
                String u = (String) cmd.get("target");
                api.removeFriend(u);
            }

            case "sendFriendRequest" -> {
                String u = (String) cmd.get("target");
                api.sendFriendInvite(u);
            }
            case "getFriendRequests" -> runJS("populateFriendRequests",
                    api.getIncomingFriendInvites().stream()
                            .map(i -> i.getInviter().getName())
                            .toArray(String[]::new));
            case "respondFriendRequest" -> {
                String u = (String) cmd.get("target");
                boolean accept = (Boolean) cmd.get("accept");
                if (accept) {
                    api.acceptFriendInvite(u);
                } else {
                    api.declineFriendInvite(u);
                }
            }

            default -> log.warn("Unknown action: {}", action);
        }
    }

    private void runJS(String fn, Object arg) {
        String json = gson.toJson(arg);
        Platform.runLater(() -> engine.executeScript(fn + "(" + json + ")"));
    }
}