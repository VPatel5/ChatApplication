package me.vpatel.network.protocol;

import me.vpatel.network.ConvoConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ConvoPacketHandler {

    private static final Logger log = LogManager.getLogger(ConvoPacketHandler.class);

    private final ConvoHandler handler;

    public ConvoPacketHandler(ConvoHandler handler)
    {
        this.handler = handler;
    }

    public <T extends ConvoPacket> void handle(ConvoConnection connection, T packet) {
        log.debug("Got packet {} by {}", packet, connection);

        if (handler != null) {
            try {
                handler.handle(connection, packet);
            } catch (Exception ex) {
                log.error("Internal error while processing packet {} from {}", packet, connection, ex);
            }
        }
    }

    public abstract PacketDirection getDirection();

    public abstract void init();
}
