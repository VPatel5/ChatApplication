package me.vpatel.network;

import java.security.PrivateKey;

public class Constants {

    private static PrivateKey privateKey;

    public static PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static void setPrivateKey(PrivateKey privateKey) {
        Constants.privateKey = privateKey;
    }
}