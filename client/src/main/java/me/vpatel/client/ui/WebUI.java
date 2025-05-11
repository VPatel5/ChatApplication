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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

        engine.load(getClass().getResource("/ui/login.html").toExternalForm());

        client.getHandler().addListener(new ConvoClientHandler.Listener() {
            @Override
            public void onAuthFinished() {
                startDataRefreshTimer();
                Platform.runLater(() -> {
                    engine.load(getClass().getResource("/ui/index.html").toExternalForm());
                });
            }

            @Override
            public void onFriendsList(List<ConvoUser> list) {
                runJS("updateUsernameDisplay", client.getUser().getName());
                runJS("populateFriends", list.stream().map(ConvoUser::getName).collect(Collectors.toList()));
            }

            @Override
            public void onGroupsList(List<ConvoGroup> list) {
                runJS("populateGroups", list.stream().map(ConvoGroup::getName).collect(Collectors.toList()));
                list.stream().map(ConvoGroup::getName).forEach(client.getClientApi()::requestGroupMessages);
            }

            @Override
            public void onIncomingFriendInvites(List<Invite> list) {
                runJS("populateFriendRequests", list.stream()
                        .map(inv -> inv.getInviter().getName())
                        .collect(Collectors.toList()));
            }

            @Override
            public void onIncomingGroupInvites(Map<String, List<Invite>> invites) {
                runJS("populateGroupInvites", invites.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().stream()
                                        .map(inv -> Map.of(
                                                "group", e.getKey(),
                                                "inviter", inv.getInviter().getName()
                                        ))
                                        .collect(Collectors.toList())
                        )));
            }

            @Override
            public void onDirectMessages(List<Message> msgs) {
                if ("friend".equals(currentType)) {
                    List<String> messages = api.getDirectMessages().stream()
                            .filter(m -> m.getSender().getName().equals(currentConversation) ||
                                    m.getRecipient().getName().equals(currentConversation))
                            .map(m -> m.getSender().getName() + ": " + m.getMessage())
                            .toList();
                    runJS("populateDirectMessages", messages);
                }
            }

            @Override
            public void onGroupMessages(String group, List<Message> msgs) {
                if ("group".equals(currentType) && group.equals(currentConversation)) {
                    List<String> messages = msgs.stream()
                            .map(m -> m.getSender().getName() + ": " + m.getMessage())
                            .toList();
                    runJS("populateGroupMessages", Map.of(
                            "group", group,
                            "messages", messages
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
            if (client.getHandler().getConnection() != null) client.getHandler().getConnection().close("Shutdown");
            if (scheduler1 != null) scheduler1.shutdown();
            if (scheduler2 != null) scheduler2.shutdown();
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    private void startDataRefreshTimer() {
        if (scheduler1 != null) scheduler1.shutdown();
        scheduler1 = Executors.newSingleThreadScheduledExecutor();
        scheduler1.scheduleAtFixedRate(() -> client.getHandler().requestAllLists(), 0, 2, TimeUnit.SECONDS);

        if (scheduler2 != null) scheduler2.shutdown();
        scheduler2 = Executors.newSingleThreadScheduledExecutor();
        scheduler2.scheduleAtFixedRate(() -> api.requestDirectMessages(), 0, 1, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private void handleCommand(String data) {
        try {
            Map<String, Object> cmd = gson.fromJson(data, Map.class);
            String action = (String) cmd.get("action");

            switch (action) {
                case "login" -> client.getAuthHandler().login(
                        (String) cmd.get("username"),
                        (String) cmd.get("password")
                );
                case "openRegister" ->
                        Platform.runLater(() -> engine.load(getClass().getResource("/ui/register.html").toExternalForm()));
                case "register" -> client.getAuthHandler().registerUser(
                        (String) cmd.get("username"),
                        (String) cmd.get("password"),
                        (String) cmd.get("email")
                );
                case "openLogin" ->
                        Platform.runLater(() -> engine.load(getClass().getResource("/ui/login.html").toExternalForm()));
                case "listFriends", "searchFriends" -> runJS("populateFriends", api.getFriends().stream()
                        .map(ConvoUser::getName)
                        .collect(Collectors.toList()));
                case "listGroups", "searchGroups" -> runJS("populateGroups", api.getGroups().stream()
                        .map(ConvoGroup::getName)
                        .collect(Collectors.toList()));
                case "createGroup" -> api.createGroup((String) cmd.get("groupName"));
                case "getGroupInvites" ->
                        runJS("populateGroupInvites", api.getIncomingGroupInvites().entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> e.getValue().stream()
                                                .map(inv -> Map.of(
                                                        "group", e.getKey(),
                                                        "inviter", inv.getInviter().getName()
                                                ))
                                                .collect(Collectors.toList())
                                )));
                case "inviteToGroup" -> api.sendGroupInvite(
                        currentConversation,
                        (String) cmd.get("friend")
                );
                case "respondGroupInvite" -> {
                    boolean accept = (Boolean) cmd.get("accept");
                    String grp = (String) cmd.get("group");
                    if (accept) {
                        api.acceptGroupInvite(grp);
                    } else {
                        api.declineGroupInvite(grp);
                    }
                }
                case "selectFriend" -> {
                    currentConversation = (String) cmd.get("target");
                    currentType = "friend";
                    List<String> messages = api.getDirectMessages().stream()
                            .filter(m -> m.getSender().getName().equals(currentConversation) ||
                                    m.getRecipient().getName().equals(currentConversation))
                            .map(m -> m.getSender().getName() + ": " + m.getMessage())
                            .collect(Collectors.toList());
                    runJS("populateDirectMessages", messages);
                    runJS("showFeedback", "Now chatting with " + currentConversation);
                }
                case "switchToFriendsTab", "switchToGroupsTab" -> {
                    currentConversation = null;
                    currentType = null;
                }
                case "selectGroup" -> {
                    currentConversation = (String) cmd.get("target");
                    currentType = "group";
                    List<String> messages = api.getGroupMessages()
                            .getOrDefault(currentConversation, List.of()).stream()
                            .map(m -> m.getSender().getName() + ": " + m.getMessage())
                            .toList();
                    runJS("populateGroupMessages", Map.of(
                            "group", currentConversation,
                            "messages", messages
                    ));
                }
                case "sendFriendMessage" -> api.chat(
                        (String) cmd.get("target"),
                        (String) cmd.get("message")
                );
                case "sendGroupMessage" -> api.groupChat(
                        (String) cmd.get("target"),
                        (String) cmd.get("message")
                );
                case "removeFriend" -> api.removeFriend((String) cmd.get("target"));
                case "sendFriendRequest" -> api.sendFriendInvite((String) cmd.get("target"));
                case "getFriendRequests" -> runJS("populateFriendRequests", api.getIncomingFriendInvites().stream()
                        .map(i -> i.getInviter().getName())
                        .collect(Collectors.toList()));
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
        } catch (Exception e) {
            log.error("Error handling command", e);
            runJS("showFeedback", "Error processing your request");
        }
    }

    private void runJS(String fn, Object arg) {
        try {
            String json = gson.toJson(arg);
            Platform.runLater(() -> {
                try {
                    engine.executeScript(fn + "(" + json + ")");
                } catch (Exception e) {
                    log.error("Error executing JS function: " + fn, e);
                }
            });
        } catch (Exception e) {
            log.error("Error preparing JS call", e);
        }
    }
}