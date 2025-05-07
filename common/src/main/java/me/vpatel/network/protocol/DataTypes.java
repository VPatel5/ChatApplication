package me.vpatel.network.protocol;

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

    public static byte[] readByteArray(ByteBuf buf) {
        int len = buf.readInt();
        byte[] data = new byte[len];
        buf.readBytes(data);
        return data;
    }

    public static void writeByteArray(byte[] arr, ByteBuf buf) {
        buf.writeInt(arr.length);
        buf.writeBytes(arr);
    }

}
