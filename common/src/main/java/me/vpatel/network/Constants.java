package me.vpatel.network;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

public class Constants {
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final boolean DEBUG_OFFLINE_MODE = true;
    public static boolean TEST_MODE = false;

    private static PrivateKey privateKey;

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void setPrivateKey(PrivateKey privateKey) {
        Constants.privateKey = privateKey;
    }
}