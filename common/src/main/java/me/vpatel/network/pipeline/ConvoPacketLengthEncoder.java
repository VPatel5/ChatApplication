package me.vpatel.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvoPacketLengthEncoder extends MessageToByteEncoder<ByteBuf> {

    private static final Logger log = LogManager.getLogger(ConvoPacketLengthDecoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out)
    {
        out.writeInt(msg.readableBytes());
        if (log.isDebugEnabled()) {
            log.debug("Wrote packet with size {}", msg.readableBytes());
        }
        out.writeBytes(msg);
    }
}