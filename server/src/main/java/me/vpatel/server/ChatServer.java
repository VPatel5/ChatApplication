package me.vpatel.server;

import me.vpatel.console.ServerConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatServer {

    private static final Logger log = LogManager.getLogger(ChatServer.class);

    private final int port;

    private ServerConsole console;

    public ChatServer(int port)
    {
        this.port = port;
    }

    public static void main(String[] args)
    {
        log.info("Starting up Chat Server");

        ChatServer chatServer = new ChatServer(8080);
        chatServer.start();
    }

    public void start()
    {
        log.info("Starting up Server Console");
        console = new ServerConsole();
        console.start();
        log.info("Console Started");
    }
}

