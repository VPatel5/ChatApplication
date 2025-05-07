package me.vpatel.network.pipeline;

import java.util.List;

import javax.crypto.Cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ConvoPacketDecrypter extends ByteToMessageDecoder {

    private final Cipher cipher;

    private byte[] buffer = new byte[0];

    public ConvoPacketDecrypter(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int count = in.readableBytes();
        // resize buffer if needed
        if (buffer.length < count) {
            buffer = new byte[count];
        }

        in.readBytes(buffer, 0, count);

        ByteBuf result = in.alloc().heapBuffer(cipher.getOutputSize(count));

        result.writerIndex(cipher.update(buffer, 0, count, result.array(), result.arrayOffset()));

        out.add(result);
    }
}