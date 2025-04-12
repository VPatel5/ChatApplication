package me.vpatel.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import me.vpatel.network.protocol.ChatServerPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatServerPacketEncoder extends MessageToByteEncoder<ChatServerPacket> {

    private static final Logger log = LogManager.getLogger(ChatServerPacketEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ChatServerPacket packet, ByteBuf out)
    {
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
