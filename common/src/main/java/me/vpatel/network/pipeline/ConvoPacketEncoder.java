package me.vpatel.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.vpatel.network.protocol.ConvoPacket;
import me.vpatel.network.protocol.ConvoPacketRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoPacketEncoder extends MessageToByteEncoder<ConvoPacket> {

    private static final Logger log = LogManager.getLogger(ConvoPacketEncoder.class);

    private final ConvoPacketRegistry packetRegistry;

    public ConvoPacketEncoder(ConvoPacketRegistry packetRegistry)
    {
        this.packetRegistry = packetRegistry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ConvoPacket packet, ByteBuf out)
    {
        packetRegistry.fillInfo(packet);
        if (log.isDebugEnabled()) {
            log.debug("Writing packet {}: {}", packet.getId(), packet);
        }

        out.writeInt(packet.getId());

        try {
            packet.toWire(out);
        } catch (Exception ex) {
            log.warn("Error while encoding packet {}", packet, ex);
            ctx.close();
        }
    }
}
