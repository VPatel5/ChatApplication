package me.vpatel.network.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class ChatServerPacketLengthDecoder extends ByteToMessageDecoder {

    private static final Logger log = LogManager.getLogger(ChatServerPacketLengthDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
    {
        in.markReaderIndex();

        if (in.readableBytes() < 4) {
            log.warn("Can't decode incoming packet length, it only contains {} bytes!", in.readableBytes());
            in.skipBytes(in.readableBytes());
            return;
        }

        int packetLength = in.readInt();

        if (in.readableBytes() < packetLength) {
            in.resetReaderIndex();
        } else {
            out.add(in.readBytes(packetLength));
        }
    }
}