package me.vpatel.client;

import me.vpatel.network.protocol.ConvoHandler;
import me.vpatel.network.protocol.ConvoPacketHandler;
import me.vpatel.network.protocol.PacketDirection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoClientPacketHandler extends ConvoPacketHandler {
    private static final Logger log = LogManager.getLogger(ConvoClientPacketHandler.class);

    public ConvoClientPacketHandler(ConvoHandler handler) {
        super(handler);
    }

    @Override
    public PacketDirection getDirection() {
        return PacketDirection.TO_CLIENT;
    }

    @Override
    public void init() {
        log.info("Init ConvoClientPacketHandler");
    }
}