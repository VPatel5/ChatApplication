package me.vpatel.server;

import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacketHandler;
import me.vpatel.network.protocol.PacketDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoServerPacketHandler extends ConvoPacketHandler {

    private static final Logger log = LogManager.getLogger(ConvoServerPacketHandler.class);

    public ConvoServerPacketHandler(ConvoHandler handler) {
        super(handler);
    }

    @Override
    public PacketDirection getDirection() {
        return PacketDirection.TO_SERVER;
    }

    @Override
    public void init() {
        log.info("Init ConvoServerPacketHandler");
    }
}