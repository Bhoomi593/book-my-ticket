package com.jsp.book.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	private static final String SECRET_KEY = "pioqwbautwqnmsduoi";
	private static final String SALTVALUE = "uasbnasddoiuqw";
	
	private static final String KEY_DERIVATION_ALGO="PBKDF2WithHmacSHA256";
	private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
	private static final int ITERATIONS = 65536;
	private static final int KEY_SIZE = 256;
	
	private static final byte[] IV = new byte[16];

	private AES() {
	}

	public static String encrypt(String strToEncrypt) {
		try {
			byte[] iv={ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec=new IvParameterSpec(iv);
			SecretKeyFactory factory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec=new PBEKeySpec(SECRET_KEY.toCharArray(),SALTVALUE.getBytes(),65536,256);
			SecretKey tmp=factory.generateSecret(spec);
			SecretKeySpec secretKey=new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
		} catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
			System.out.println("Error occured during encryption: "+e.toString());
		}
		return null;
	}

	public static String decrypt(String strToEncrypt) {
		try {
			byte[] iv={ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec=new IvParameterSpec(iv);
			SecretKeyFactory factory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec=new PBEKeySpec(SECRET_KEY.toCharArray(),SALTVALUE.getBytes(),65536,256);
			SecretKey tmp=factory.generateSecret(spec);
			SecretKeySpec secretKey=new SecretKeySpec(tmp.getEncoded(), "AES");
			Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToEncrypt)));
		} catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
			System.out.println("Error occured during encryption: "+e.toString());
		}
		return null;
	}
	
	public static SecretKeySpec getSecretKey() throws Exception{
		SecretKeyFactory factory=SecretKeyFactory.getInstance(KEY_DERIVATION_ALGO);
		
		KeySpec spec=new PBEKeySpec(SECRET_KEY.toCharArray(), SALTVALUE.getBytes(StandardCharsets.UTF_8), ITERATIONS, KEY_SIZE);
		SecretKey tmp=factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
		
	}
}
