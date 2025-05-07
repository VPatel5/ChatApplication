package me.vpatel.client;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.client.ClientEncryptionResponsePacket;
import me.vpatel.network.protocol.server.ServerEncryptionRequestPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class AuthHandler {
    private static final Logger log = LogManager.getLogger(AuthHandler.class);

    public void auth(ServerEncryptionRequestPacket packet, ConvoConnection connection) {
        SecretKey sharedSecret = generateSharedKey();

        connection.sendPacket(new ClientEncryptionResponsePacket(sharedSecret, packet.getVerifyToken(), packet.getPublicKey()));

        connection.enableEncryption(sharedSecret);
    }

    private SecretKey generateSharedKey() {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            return gen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}