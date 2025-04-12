package me.vpatel.network;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class DataTypes {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static String readString(ByteBuf buf) {
        int length = buf.readInt();

        byte[] bytes = new byte[length];
        buf.readBytes(bytes);

        return new String(bytes, CHARSET);
    }

    public static void writeString(String string, ByteBuf buf) {
        if (string == null) {
            buf.writeInt(0);
            return;
        }
        byte[] bytes = string.getBytes(CHARSET);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

}
