package com.olyware.mathlock.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import android.util.Base64;

public class EncryptionHelper {

	final private String DEFAULT_KEY = "D#a9r3+K";

	private String charsetName = "UTF8";
	private String algorithm = "DES";
	private int base64ModeURL = Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP;
	private int base64Mode = Base64.DEFAULT;
	private byte[] test;

	public String getCharsetName() {
		return charsetName;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public int getBase64ModeURL() {
		return base64ModeURL;
	}

	public void setBase64ModeURL(int base64ModeURL) {
		this.base64ModeURL = base64ModeURL;
	}

	public int getBase64Mode() {
		return base64Mode;
	}

	public void setBase64Mode(int base64Mode) {
		this.base64Mode = base64Mode;
	}

	public String encryptForURL(String data) {
		return encryptForURL(DEFAULT_KEY, data);
	}

	public String decryptForURL(String data) {
		return decryptForURL(DEFAULT_KEY, data);
	}

	public String encryptForURL(String key, String data) {
		if (key == null || data == null)
			return null;
		try {
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(charsetName));
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
			SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
			byte[] dataBytes = data.getBytes(charsetName);
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.encodeToString(cipher.doFinal(dataBytes), base64ModeURL);
		} catch (Exception e) {
			return null;
		}
	}

	public String decryptForURL(String key, String data) {
		if (key == null || data == null)
			return null;
		try {
			byte[] dataBytes = Base64.decode(data, base64ModeURL);
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(charsetName));
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
			SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] dataBytesDecrypted = (cipher.doFinal(dataBytes));
			return new String(dataBytesDecrypted);
		} catch (Exception e) {
			return null;
		}
	}

	/*public String AESencryptForURL(String data) {
		return AESencryptForURL(DEFAULT_KEY, data);
	}

	public String AESdecryptForURL(String data) {
		return AESdecryptForURL(DEFAULT_KEY, data);
	}

	public String AESencryptForURL(String key, String data) {
		if (key == null || data == null)
			return null;
		try {
			byte[] keyStart = key.getBytes(charsetName);
			byte[] dataBytes = data.getBytes(charsetName);
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(keyStart);
			kgen.init(128, sr); // 192 and 256 bits may not be available
			SecretKey skey = kgen.generateKey();
			byte[] keyFinal = skey.getEncoded();
			Log.d("GAtest", "keyFinal1 = " + keyFinal);
			SecretKeySpec secretKeySpec = new SecretKeySpec(keyFinal, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			test = cipher.getIV();
			return Base64.encodeToString(cipher.doFinal(dataBytes), base64ModeURL);
		} catch (Exception e) {
			return null;
		}
	}

	public String AESdecryptForURL(String key, String data) {
		if (key == null || data == null)
			return null;
		try {
			byte[] keyStart = key.getBytes(charsetName);
			byte[] dataBytes = Base64.decode(data, base64ModeURL);
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(keyStart);
			kgen.init(128, sr); // 192 and 256 bits may not be available
			SecretKey skey = kgen.generateKey();
			byte[] keyFinal = skey.getEncoded();
			Log.d("GAtest", "keyFinal = " + keyFinal);
			SecretKeySpec sectretKeySpec = new SecretKeySpec(keyFinal, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, sectretKeySpec, new IvParameterSpec(test));
			byte[] dataBytesDecrypted = (cipher.doFinal(dataBytes));
			return new String(dataBytesDecrypted);
		} catch (Exception e) {
			return null;
		}
	}*/

	public String encrypt(String data) {
		return encrypt(DEFAULT_KEY, data);
	}

	public String decrypt(String data) {
		return decrypt(DEFAULT_KEY, data);
	}

	public String encrypt(String key, String data) {
		if (key == null || data == null)
			return null;
		try {
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(charsetName));
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
			SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
			byte[] dataBytes = data.getBytes(charsetName);
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.encodeToString(cipher.doFinal(dataBytes), base64Mode);
		} catch (Exception e) {
			return null;
		}
	}

	public String decrypt(String key, String data) {
		if (key == null || data == null)
			return null;
		try {
			byte[] dataBytes = Base64.decode(data, base64Mode);
			DESKeySpec desKeySpec = new DESKeySpec(key.getBytes(charsetName));
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
			SecretKey secretKey = secretKeyFactory.generateSecret(desKeySpec);
			Cipher cipher = Cipher.getInstance(algorithm);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] dataBytesDecrypted = (cipher.doFinal(dataBytes));
			return new String(dataBytesDecrypted);
		} catch (Exception e) {
			return null;
		}
	}
}
