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
import me.vpatel.network.api.Message;
import me.vpatel.network.protocol.client.*;

import java.util.List;
import java.util.Map;

public class WebUI extends Application {
    private static final ConvoClient client = AppContext.getClient();
    private static ClientApi api = AppContext.getClient().getClientApi();
    private static final Gson gson = new Gson();

    private Stage stage;
    private ConvoClientHandler handler;

    /** Call this once at startup */
    public static void launchUI() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage   = primaryStage;
        this.handler = client.getHandler();

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // catch alert(JSON) from JS
        engine.setOnAlert(evt -> handleCommand(evt.getData(), engine));

        // subscribe to server events
        handler.addListener(new ConvoClientHandler.Listener() {
            @Override
            public void onLoginFailed(String message) {
                runJS(engine, "setStatus", message, false);
            }
            @Override
            public void onRegisterFailed(String message) {
                runJS(engine, "setStatus", message, false);
            }
            @Override
            public void onRegisterSuccess() {
                runJS(engine, "setStatus", "Registration successful", true);
            }
            @Override
            public void onAuthFinished() {
                Platform.runLater(() -> engine.load(
                        getClass().getResource("/ui/index.html").toExternalForm()
                ));
            }
        });

        // start at login page
        engine.load(getClass().getResource("/ui/login.html").toExternalForm());

        primaryStage.setScene(new Scene(webView, 900, 600));
        primaryStage.setTitle("Convo Chat");
        primaryStage.show();
    }

    private void handleCommand(String data, WebEngine engine) {
        Map cmd = gson.fromJson(data, Map.class);
        String action = (String) cmd.get("action");

        switch (action) {
            case "login" -> {
                String u = (String) cmd.get("username");
                String p = (String) cmd.get("password");
                client.getAuthHandler().login(u, p);
            }
            case "openRegister" -> Platform.runLater(() ->
                    engine.load(getClass().getResource("/ui/register.html").toExternalForm())
            );
            case "register" -> {
                String u = (String) cmd.get("username");
                String e = (String) cmd.get("email");
                String p = (String) cmd.get("password");
                client.getAuthHandler().registerUser(u, p, e);
            }
            case "openLogin" -> Platform.runLater(() ->
                    engine.load(getClass().getResource("/ui/login.html").toExternalForm())
            );
            case "listFriends" -> {
                api.list(ClientListRequestPacket.ListType.FRIENDS);
                String[] arr = api.getFriends().stream().map(x->x.getName()).toArray(String[]::new);
                runJS(engine, "populateFriends", arr);
            }
            case "listGroups" -> {
                api.list(ClientListRequestPacket.ListType.GROUPS);
                String[] arr = api.getGroups().stream().map(x->x.getName()).toArray(String[]::new);
                runJS(engine, "populateGroups", arr);
            }
            case "selectTarget" -> {
                String tgt = (String) cmd.get("target");
                api.list(ClientListRequestPacket.ListType.MESSAGES, tgt);
                List<Message> msgs = api.getMessages().getOrDefault(tgt, List.of());
                String[] lines = msgs.stream()
                        .map(m->m.getSender().getName()+ ":" +m.getMessage())
                                .toArray(String[]::new);
                runJS(engine, "populateMessages", lines);
            }
            case "sendMessage" -> {
                String tgt = (String) cmd.get("target");
                String msg = (String) cmd.get("message");
                api.chat(tgt, msg);
                // refresh messages
                handleCommand(gson.toJson(Map.of("action","selectTarget","target",tgt)), engine);
            }
        }
    }

    /** helper to call JS fn with JSON-encoded args */
    private void runJS(WebEngine engine, String fn, Object... args) {
        String jsonArgs = gson.toJson(args.length==1? args[0] : args);
        String script = fn + "(" + jsonArgs + ")";
        Platform.runLater(() -> engine.executeScript(script));
    }
}
