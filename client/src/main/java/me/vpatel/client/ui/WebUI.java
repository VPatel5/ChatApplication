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
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.client.ClientListRequestPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public class WebUI extends Application {
    private static final Logger log    = LogManager.getLogger(WebUI.class);
    private static final ConvoClient client = AppContext.getClient();
    private static final ClientApi api      = client.getClientApi();
    private static final Gson gson          = new Gson();

    private WebEngine engine;

    public static void launchUI() {
        launch();
    }

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        engine = webView.getEngine();

        // Handle JS alerts as commands
        engine.setOnAlert(evt -> handleCommand(evt.getData()));

        // Show login page first
        engine.load(getClass().getResource("/ui/login.html").toExternalForm());

        // After authentication, show loading, fetch data, then main UI
        client.getHandler().addListener(new ConvoClientHandler.Listener() {
            @Override
            public void onAuthFinished() {
                Platform.runLater(() -> {
                    // 1) Show loading spinner
                    engine.load(getClass().getResource("/ui/loading.html").toExternalForm());
                    // 2) Request all necessary lists
                    client.getHandler().requestAllLists();
                    // 3) After delay, load real UI
                    new Thread(() -> {
                        try { Thread.sleep(5000); }
                        catch (InterruptedException ignored) {}
                        Platform.runLater(() ->
                                engine.load(getClass().getResource("/ui/index.html").toExternalForm())
                        );
                    }).start();
                });
            }
        });

        stage.setScene(new Scene(webView, 900, 600));
        stage.setTitle("Convo Chat");

        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    @SuppressWarnings("unchecked")
    private void handleCommand(String data) {
        Map<String,Object> cmd = gson.fromJson(data, Map.class);
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

            case "searchFriends" -> {
                String q = (String) cmd.get("query");
                runJS("populateFriends",
                        api.getFriends().stream().map(ConvoUser::getName).toArray(String[]::new)
                );
            }
            case "listFriends" -> listFriends();

            case "searchGroups" -> {
                api.list(ClientListRequestPacket.ListType.GROUPS);
                runJS("populateGroups",
                        api.getGroups().stream().map(ConvoGroup::getName).toArray(String[]::new)
                );
            }
            case "listGroups" -> listGroups();

            case "createGroup" -> {
                String name = (String) cmd.get("groupName");
                api.createGroup(name);
                listGroups();
            }

            case "selectFriend" -> {
                String tgt = (String) cmd.get("target");
                api.list(ClientListRequestPacket.ListType.MESSAGES, tgt);
                runJS("populateMessages",
                        api.getMessages().getOrDefault(tgt, List.of()).stream()
                                .map(m -> m.getSender().getName() + ": " + m.getMessage())
                                .toArray(String[]::new)
                );
            }
            case "sendFriendMessage" -> {
                String tgt = (String) cmd.get("target");
                String msg = (String) cmd.get("message");
                api.chat(tgt, msg);
                handleCommand(gson.toJson(Map.of("action","selectFriend","target",tgt)));
            }
            case "sendGroupMessage" -> {
                String grp = (String) cmd.get("target");
                String msg = (String) cmd.get("message");
                api.groupChat(grp, msg);
                handleCommand(gson.toJson(Map.of("action","selectGroup","target",grp)));
            }

            default -> log.warn("Unknown action: {}", action);
        }
    }

    private void listFriends() {
        api.list(ClientListRequestPacket.ListType.FRIENDS);
        runJS("populateFriends",
                api.getFriends().stream().map(ConvoUser::getName).toArray(String[]::new)
        );
    }

    private void listGroups() {
        api.list(ClientListRequestPacket.ListType.GROUPS);
        runJS("populateGroups",
                api.getGroups().stream().map(ConvoGroup::getName).toArray(String[]::new)
        );
    }

    /** Call a JS function with a JSON array or value */
    private void runJS(String fn, Object arg) {
        String json = gson.toJson(arg);
        Platform.runLater(() -> engine.executeScript(fn + "(" + json + ")"));
    }
}
