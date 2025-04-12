package me.vpatel.server;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Queue;
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
        log.info("Now handling {} connections", connections.size());
    }

    @Override
    public void leave(ConvoConnection connection) {
        connections.remove(connection);
        log.info("Now handling {} connections", connections.size());
    }

    @Override
    public void handle(ConvoConnection connection, ConvoPacket msg) {

    }
}