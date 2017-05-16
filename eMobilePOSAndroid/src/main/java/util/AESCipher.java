package util;


import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.support.Base64;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Guarionex
 */
public class AESCipher {

    public AESCipher() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public String getAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey generateKey = keyGen.generateKey();
        byte[] encodedKey = generateKey.getEncoded();
        return Base64.encodeBytes(encodedKey);
    }

    public String getAesInitVector() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[16];
        sr.nextBytes(iv);
        return Base64.encodeBytes(iv);

    }

    public String encrypt(String key, String initVector, String value) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
        IvParameterSpec iv = new IvParameterSpec(Base64.decode(initVector));
        SecretKeySpec skeySpec = new SecretKeySpec(Base64.decode(key), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.encodeBytes(encrypted);
    }

    public String decrypt(String key, String initVector, String encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException {
        IvParameterSpec iv = new IvParameterSpec(Base64.decode(initVector));
        SecretKeySpec skeySpec = new SecretKeySpec(Base64.decode(key), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] original = cipher.doFinal(Base64.decode(encrypted));

        return new String(original);

    }

    public static String getSha256Hash(String value) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] shaBytes = digest.digest(value.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte shaByte : shaBytes) {
            hexString.append(Integer.toHexString(0xFF & shaByte));
        }
        return  hexString.toString();
    }
}