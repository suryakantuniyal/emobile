package com.android.support;

import java.io.InputStream;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.emobilepos.app.R;
import org.springframework.util.support.Base64;

import android.app.Activity;


public class Encrypt 
{
	private Activity activity;

	public Encrypt()
	{
		
	}
	public Encrypt(Activity activity)
	{
		this.activity = activity;
		Security.addProvider(new BouncyCastleProvider());
	}
    
	
	public String encryptWithAES(String data)
	{
		if(data!=null)
		{
		byte [] input = data.getBytes();
		byte[] secretKey = null;
		byte[] initVector = null;
		byte[] cipherText = null;
		
		KeyGenerator kg;
		try {
			kg = KeyGenerator.getInstance("AES");
			SecretKey sk;
			
			kg.init(256);
			sk = kg.generateKey();
			
			SecretKeySpec keySpec = new SecretKeySpec(sk.getEncoded(),"AES");		//generate random key
			secretKey = this.encryptWithLocalKey(sk.getEncoded());					//encrypt Key with certificate
			
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			byte[] iv = new byte[16];
			sr.nextBytes(iv);
			initVector = this.encryptWithLocalKey(iv);								//encrypt IV with certificate
			
			
			IvParameterSpec ivParams = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec,ivParams);
			
			cipherText = new byte[cipher.getOutputSize(input.length)];
			int ctLength = cipher.update(input, 0,input.length,cipherText,0);
			ctLength+=cipher.doFinal(cipherText, ctLength);							//encrypt the data with AES
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
		
		
		
		
		//concat byte arrays before encoding with Base64
		byte[] finalArray = concatByteArray(concatByteArray(initVector, secretKey), cipherText);
		
		//String encoded = Base64.encodeToString(finalArray,Base64.DEFAULT);
		String encoded = Base64.encodeBytes(finalArray);
		return encoded;
		}
		return "";
	}
	
	
	private byte[] concatByteArray(byte[]A,byte[]B)
	{
		int aLen = A.length;
		int bLen = B.length;
		byte[] C = new byte[aLen + bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
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
 	
 	
 	
 	
 	private String encryptWithLocalKey(String text)
 	{
 		String encryptedText = null;
 		try
 		{
 			
 	 		byte[] cipherText = null;
 	 			Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding","BC");

 	 			// encrypt the plaintext using the public key
 	 			cipher.init(Cipher.ENCRYPT_MODE,getLocalPublicKey() );
 	 			cipherText = cipher.doFinal(text.getBytes());
 	 			encryptedText = Base64.encodeBytes(cipherText);
 	 			
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return encryptedText;
 	}
 	
 	
 	private byte[] encryptWithLocalKey(byte[] text)
 	{
 		byte[] cipherText = null;
 		try
 		{
 			
 	 		
 	 			Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding","BC");

 	 			// encrypt the plaintext using the public key
 	 			cipher.init(Cipher.ENCRYPT_MODE,getLocalPublicKey() );
 	 			cipherText = cipher.doFinal(text);
 	 			//encryptedText = Base64.encodeToString(cipherText, Base64.DEFAULT);
 	 			
 		}catch (Exception e) {
 			e.printStackTrace();
 		}
 		return cipherText;
 	}
 	
 	
 	private PublicKey getLocalPublicKey()throws Exception 
 	{
 		X509Certificate crt = null;
 		InputStream in = activity.getResources().openRawResource( R.raw.public_key);
 		CertificateFactory cf;
		try {
			cf = CertificateFactory.getInstance("X.509");
			crt = (X509Certificate)cf.generateCertificate(in);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			throw e;
		}
		return crt.getPublicKey();
 	}
    
}
