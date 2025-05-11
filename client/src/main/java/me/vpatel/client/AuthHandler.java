package me.vpatel.client;

import me.vpatel.network.ConvoConnection;
import me.vpatel.network.protocol.client.ClientEncryptionResponsePacket;
import me.vpatel.network.protocol.client.ClientLoginStartPacket;
import me.vpatel.network.protocol.client.ClientRegisterRequestPacket;
import me.vpatel.network.protocol.server.ServerEncryptionRequestPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthHandler {
    private static final Logger log = LogManager.getLogger(AuthHandler.class);

    private static final SecureRandom random = new SecureRandom();

    public void auth(ServerEncryptionRequestPacket packet, ConvoConnection connection) {
        SecretKey sharedSecret = generateSharedKey();

        connection.sendPacket(new ClientEncryptionResponsePacket(sharedSecret, packet.getVerifyToken(), packet.getPublicKey()));

        connection.enableEncryption(sharedSecret);
    }

    public void login(String username, String password) {
        ConvoConnection connection = AppContext.getClient().getHandler().getConnection();

        connection.sendPacket(new ClientLoginStartPacket(username, password));
    }

    public void registerUser(String username, String password, String email) {
        ConvoConnection connection = AppContext.getClient().getHandler().getConnection();

        String salt = generateSalt();
        String passHashed = hashPassword(password, salt);

        connection.sendPacket(new ClientRegisterRequestPacket(username, passHashed, salt, email));
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

    private String hashPassword(String password, String salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 128);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}