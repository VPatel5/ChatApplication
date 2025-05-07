package me.vpatel.server;

import com.google.gson.Gson;
import me.vpatel.network.Constants;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.client.ClientEncryptionResponsePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.*;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthHandler {
    private static final Logger log = LogManager.getLogger(ConvoServerHandler.class);
    private static final SecureRandom random = new SecureRandom();

    private final ConvoServer server;
    private final Gson gson = new Gson();

    private final Map<String, byte[]> verificationTokens = new ConcurrentHashMap<>();

    private final KeyPair keypair;

    public AuthHandler(ConvoServer server) {
        this.server = server;
        this.keypair = generateKey();
        Constants.setPrivateKey(getPrivateKey());
    }

    public PublicKey getPublicKey() {
        return keypair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keypair.getPrivate();
    }

    public byte[] genVerificationToken(String user) {
        byte[] token = new byte[4];
        random.nextBytes(token);
        verificationTokens.put(user, token);
        return token;
    }

    public static KeyPair generateKey() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            return gen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean auth(ClientEncryptionResponsePacket packet, ConvoConnection connection) {
        if (!verificationTokens.containsKey(connection.getUser().getName())) {
            connection.close("Timed out");
            return false;
        }

        byte[] token = verificationTokens.get(connection.getUser().getName());
        if (!Arrays.equals(token, packet.getVerificationToken())) {
            connection.close("Access denied");
            return false;
        }

        connection.setAuthFinished(true);
        connection.enableEncryption(packet.getSharedSecret());
        log.info("User logged in");
        return true;
    }

}
