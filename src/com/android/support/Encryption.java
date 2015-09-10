package com.android.support;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Guard;
import java.security.GuardedObject;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.PropertyPermission;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import android.app.Activity;
import android.util.Base64;

public class Encryption {

	//private Key pubKey, privKey;

	//private KeyPair keyPair;
	
	
	private Activity activity;
	

	public Encryption(Activity activity)
	{
		this.activity = activity;
		Security.addProvider(new BouncyCastleProvider());
		
		generateRSAKey();
	}

	
	
	
	public static SecretKey generatePasswordKey(char[]passphrase,byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		final int iteration = 1000;
		
		final int outputKeyLength = 256;
		
		SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec keySpec = new PBEKeySpec(passphrase,salt,iteration,outputKeyLength);
		SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);
		return secretKey;
	}
	
	
	
	
	private  void generateRSAKey() {
		KeyPairGenerator keyGen;
		KeyPair keyPair;
		MyPreferences myPref = new MyPreferences(activity);
		Guard guard = new PropertyPermission("java.home","read");
		
		GuardedObject pubRSAGuarded;// = new GuardedObject( , guard);
		GuardedObject privRSAGuarded;// = new GuardedObject( , guard);
		try {
			keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			keyPair = keyGen.generateKeyPair();
			pubRSAGuarded = new GuardedObject(keyPair.getPublic().getEncoded(),guard);
			privRSAGuarded = new GuardedObject(keyPair.getPrivate().getEncoded(),guard);
			
			myPref.setRSAKeys(privRSAGuarded, pubRSAGuarded);		//store keys in secure area while GuardedObject
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	
	
	/**
	 * Encrypt the data with the AES key and iv retrieved from the server.
	 * 
	 */
	 public String encryptToAES(String data)
	 {
		 MyPreferences myPref = new MyPreferences(this.activity);
		 GuardedObject aesKeyGuarded = myPref.getAESKey();
		 GuardedObject aesIVGuarded = myPref.getAESIV();
		 
		 Key AES_KEY;
		 IvParameterSpec AES_IV;
		 
		 //AES_KEY & AES_IV are received as as Base64.decoded() byte array with an RSA encryption
		 AES_KEY = new SecretKeySpec((decrypt((byte[])aesKeyGuarded.getObject())),"AES");
		 AES_IV = new IvParameterSpec((decrypt((byte[])aesIVGuarded.getObject())));
		 
		 
		 
		 byte[] input = data.getBytes();
		 Cipher cipher = null;
		 byte[]cipherText = null;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
		    cipher.init(Cipher.ENCRYPT_MODE,AES_KEY,AES_IV);
		    cipherText = new byte[cipher.getOutputSize(input.length)];
		    int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
		    ctLength += cipher.doFinal(cipherText, ctLength);
		    
		    
		    
		    myPref.deleteStoredEncryptionKeys();		//delete the stored private RSA,AES_KEY,and AES_IV
		    
		    
		    
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ShortBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    return Base64.encodeToString(cipherText, 0, cipherText.length, Base64.DEFAULT);
    }
	

	/**
	 * Encrypt a text using public key.
	 * 
	 * @param text
	 *            The original unencrypted text
	 * @param key
	 *            The public key
	 * @return Encrypted text
	 * @throws java.lang.Exception
	 */
	public byte[] encrypt(byte[] text) throws Exception {
		MyPreferences myPref = new MyPreferences(activity);
		byte[] cipherText = null;
		try {
			// get an RSA cipher object
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

			// encrypt the plaintext using the public key
			cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromString((String)myPref.getRSAPubKey().getObject()));
			cipherText = cipher.doFinal(text);
		} catch (Exception e) {
			throw e;
		}
		return cipherText;
	}

	/**
	 * Encrypt a text using public key. The result is enctypted BASE64 encoded
	 * text
	 * 
	 * @param text
	 *            The original unencrypted text
	 * @param key
	 *            The public key
	 * @return Encrypted text encoded as BASE64
	 * @throws java.lang.Exception
	 */
	public String encrypt(String text) throws Exception {
		String encryptedText;
		try {
			byte[] cipherText = encrypt(text.getBytes("UTF-8"));
			encryptedText = encodeBASE64(cipherText);
		} catch (Exception e) {
			throw e;
		}
		return encryptedText;
	}

	/**
	 * Decrypt text using private key
	 * 
	 * @param text
	 *            The encrypted text
	 * @param key
	 *            The private key
	 * @return The unencrypted text
	 * @throws java.lang.Exception
	 */
	public  byte[] decrypt(byte[] text) {
		byte[] dectyptedText = null;
		MyPreferences myPref = new MyPreferences(activity);
		GuardedObject privKeyGuarded = myPref.getRSAPrivKey();
		try {
			// decrypt the text using the private key
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE,getPrivateKeyFromString((String)privKeyGuarded.getObject()));
			dectyptedText = cipher.doFinal(text);
		} catch (Exception e) {
			;
		}
		return dectyptedText;

	}

	/**
	 * Decrypt BASE64 encoded text using private key
	 * 
	 * @param text
	 *            The encrypted text, encoded as BASE64
	 * @param key
	 *            The private key
	 * @return The unencrypted text encoded as UTF8
	 * @throws java.lang.Exception
	 */
	public String decrypt(String text) throws Exception {
		String result;
		try {
			// decrypt the text using the private key
			byte[] dectyptedText = decrypt(decodeBASE64(text));
			result = new String(dectyptedText, "UTF-8");
		} catch (Exception e) {
			throw e;
		}
		return result;

	}

	/**
	 * Convert a Key to string encoded as BASE64
	 * 
	 * @param key
	 *            The key (private or public)
	 * @return A string representation of the key
	 */
	public static String getKeyAsString(Key key) {
		return Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
	}

	/**
	 * Generates Private Key from BASE64 encoded string
	 * 
	 * @param key
	 *            BASE64 encoded string which represents the key
	 * @return The PrivateKey
	 * @throws java.lang.Exception
	 */
	public static PrivateKey getPrivateKeyFromString(String key) throws Exception 
	{
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.decode(key, Base64.DEFAULT));
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
		return privateKey;
	}

	/**
	 * Generates Public Key from BASE64 encoded string
	 * 
	 * @param key
	 *            BASE64 encoded string which represents the key
	 * @return The PublicKey
	 * @throws java.lang.Exception
	 */
	public static PublicKey getPublicKeyFromString(String key) throws Exception {
		// BASE64Decoder b64 = new BASE64Decoder();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decode(key, Base64.DEFAULT));
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
		return publicKey;
	}

	/**
	 * Encode bytes array to BASE64 string
	 * 
	 * @param bytes
	 * @return Encoded string
	 */
	private static String encodeBASE64(byte[] bytes) {
		String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
		return encoded;
	}

	/**
	 * Decode BASE64 encoded string to bytes array
	 * 
	 * @param text
	 *            The string
	 * @return Bytes array
	 * @throws IOException
	 */
	private static byte[] decodeBASE64(String text) {

		byte[] decoded = null;
		try {
			decoded = Base64.decode(text.getBytes("UTF-8"), Base64.DEFAULT);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return decoded;
	}

}
