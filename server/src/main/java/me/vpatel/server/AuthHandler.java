package me.vpatel.server;

import com.google.gson.Gson;
import me.vpatel.db.UserDao;
import me.vpatel.db.tables.UsersTable;
import me.vpatel.network.Constants;
import me.vpatel.network.ConvoConnection;
import me.vpatel.network.api.ConvoUser;
import me.vpatel.network.protocol.client.ClientEncryptionResponsePacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthHandler {
    private static final Logger log = LogManager.getLogger(ConvoServerHandler.class);
    private static final SecureRandom random = new SecureRandom();

    private final ConvoServer server;

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

    public ConvoUser registerUser(String username, String password, String salt) {
        try {
            boolean exists = server.getDbHandler()
                    .jdbi()
                    .withExtension(UserDao.class, dao -> dao.getByName(username) != null);

            if (exists) {
                log.info("Registration failed: username '{}' already exists", username);
                return null;
            }

            UsersTable user = new UsersTable();
            user.setUuid(UUID.nameUUIDFromBytes(username.getBytes()).toString());
            user.setName(username);
            user.setPasswordHash(password);
            user.setSalt(salt);

            server.getDbHandler().jdbi().useExtension(UserDao.class, dao -> dao.create(user));
            return user.convert();
        } catch (Exception e) {
            log.error("Failed to register user", e);
            return null;
        }
    }

    public ConvoUser registerAI() {
        ConvoUser AI = ConvoUser.AI_USER;
        try {
            boolean exists = server.getDbHandler()
                    .jdbi()
                    .withExtension(UserDao.class, dao -> dao.getByName(AI.getName()) != null);

            if (exists) {
                return AI;
            }

            UsersTable user = new UsersTable();
            user.setUuid(AI.getId().toString());
            user.setName(AI.getName());
            user.setPasswordHash("password");
            user.setSalt("salt");

            server.getDbHandler().jdbi().useExtension(UserDao.class, dao -> dao.create(user));
            return user.convert();
        } catch (Exception e) {
            log.error("Failed to register user", e);
            return null;
        }
    }

    public ConvoUser loginUser(String username, String password) {
        registerAI();
        return server.getDbHandler().jdbi().withExtension(UserDao.class, dao -> {
            UsersTable user = dao.getByName(username);
            if (user == null) return null;

            String hashed = hashPassword(password, user.getSalt());
            if (hashed.equals(user.getPasswordHash())) {
                return new ConvoUser(UUID.fromString(user.getUuid()), username);
            }
            return null;
        });
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

        UUID id = UUID.nameUUIDFromBytes(connection.getUser().getName().getBytes());

        if (id == null) {
            log.warn("Auth failed for {}", connection.getUser());
            connection.close("Auth failed");
            return false;
        }

        ConvoUser dbUser = server.getUsersHandler().createOrLoadUser(id, connection.getUser().getName());
        if (dbUser == null) {
            log.warn("Error while loading user {}", connection.getUser());
            connection.close("Error while loading your data");
            return false;
        }

        connection.setUser(dbUser);
        connection.setAuthFinished(true);
        connection.enableEncryption(packet.getSharedSecret());
        log.info("User logged in");
        return true;
    }

}
