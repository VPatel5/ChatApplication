package me.vpatel.client;

public class AppContext {
    private static final ConvoClient client = new ConvoClient();

    public static ConvoClient getClient() {
        return client;
    }
}
