package me.vpatel.network.pipeline;

import javax.crypto.Cipher;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ConvoPacketEncrypter extends MessageToByteEncoder<ByteBuf> {

    private final Cipher cipher;

    private byte[] buffer = new byte[0];
    private byte[] cryptBuffer = new byte[0];

    public ConvoPacketEncrypter(Cipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int count = msg.readableBytes();

        // resize buffer if needed
        if (buffer.length < count) {
            buffer = new byte[count];
        }

        msg.readBytes(buffer, 0, count);

        int outSize = cipher.getOutputSize(count);

        // resize buffer if needed
        if (cryptBuffer.length < outSize) {
            cryptBuffer = new byte[outSize];
        }

        out.writeBytes(cryptBuffer, 0, cipher.update(buffer, 0, count, cryptBuffer));
    }
}