package me.vpatel.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class CryptUtil {
    private static final Logger log = LogManager.getLogger(CryptUtil.class);

    public static String genServerHash(SecretKey sharedSecret, PublicKey publicKey) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            for (byte[] bit : new byte[][]{
                    new byte[0], sharedSecret.getEncoded(), publicKey.getEncoded()
            }) {
                sha.update(bit);
            }
            return new BigInteger(sha.digest()).toString(16);
        } catch (Exception e) {
            log.warn("Error creating server hash: ", e);
            return null;
        }
    }

    public static PublicKey decode(byte[] data) {
        try {
            X509EncodedKeySpec x509encodedkeyspec = new X509EncodedKeySpec(data);
            KeyFactory keyfactory = KeyFactory.getInstance("RSA");
            return keyfactory.generatePublic(x509encodedkeyspec);
        } catch (NoSuchAlgorithmException ex) {
            if (log.isDebugEnabled()) {
                log.debug("data: {}", Arrays.toString(data), ex);
            }
            log.error("Public key reconstitute failed!");
            return null;
        } catch (InvalidKeySpecException ex) {
            if (log.isDebugEnabled()) {
                log.debug("data: {}", Arrays.toString(data), ex);
            }
            log.error("Public key reconstitute failed!");
            return null;
        }
    }

    public static byte[] encrypt(Key key, byte[] data) {
        return cipherOp(Cipher.ENCRYPT_MODE, key, data);
    }

    public static byte[] decrypt(Key key, byte[] data) {
        return cipherOp(Cipher.DECRYPT_MODE, key, data);
    }

    private static byte[] cipherOp(int op, Key key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(op, key);
            return cipher.doFinal(data);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Op: {}", op);
                log.debug("Key: {}", key);
                log.debug("data: {}", Arrays.toString(data));
            }
            log.error("Error", e);
            return null;
        }
    }

    public static Cipher createContinuousCipher(int op, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            byte[] iv = Arrays.copyOfRange(key.getEncoded(), 0, 16); // if key is >= 16 bytes
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(op, key, ivSpec);
            return cipher;
        } catch (GeneralSecurityException e) {
            if (log.isDebugEnabled()) {
                log.debug("Op: {}", op);
                log.debug("Key: {}", key);
            }
            log.error("Op: {}", op);
            log.error("Key: {}", key);
            log.error("Error", e);
            return null;
        }
    }
}
