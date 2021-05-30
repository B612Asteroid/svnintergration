package com.ptc.fs.svn.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Arrays;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;

public final class SVNAESCipher
extends PBECipher
{
	private static Key pbeKey;
	private static boolean cipherHasBeenInitialized = false;
	private static final String TRANSFORM = "AES/ECB/PKCS5Padding";

	private static void initCipher() throws GeneralSecurityException {
		if (cipherHasBeenInitialized == true) {
			return;
		}
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);


		String salt = CommonUtils.getCommonAppProperties().getProperty("svn.im.encryption.salt");

		if (SimplePBECipher.isEncryptedPwd(salt)) {
			SVNLogger.logMessage("WARNING", 10, "Salt is encrypted. Decrypting...");
			salt = SimplePBECipher.decryptPwd(salt);
		} 


		if (salt != null && salt.length() > 0) {

			try {
				UUID uuid = UUID.fromString(salt);
				bb.putLong(uuid.getMostSignificantBits());
				bb.putLong(uuid.getLeastSignificantBits());
			}
			catch (IllegalArgumentException e) {

				SVNLogger.logMessage("WARNING", 0, "Specified salt is not in correct format. It needs be a valid UUID.");
			} 
		} else {


			try {
				bb.put("TheWorld wonders".getBytes("UTF-8"));
			}
			catch (UnsupportedEncodingException e) {

				throw new GeneralSecurityException(e);
			} 
			SVNLogger.logMessage("WARNING", 0, "Cipher Salt is not set.");
		} 

		pbeKey = new SecretKeySpec(bb.array(), "AES");

		cipherHasBeenInitialized = true;
	}

	public static String encryptText(char[] clearText) throws GeneralSecurityException {
		initCipher();

		ByteBuffer bb = Charset.forName("UTF-8").encode(CharBuffer.wrap(clearText));
		byte[] clearBytes = new byte[bb.remaining()];
		bb.get(clearBytes);

		String encrypted = PBECipher.encryptWithBase64Coding(clearBytes, pbeKey, null, TRANSFORM);



		Arrays.fill(bb.array(), (byte)127);
		Arrays.fill(clearBytes, (byte)127);

		return encrypted;
	}

	public static char[] decryptText(String encryptedText) throws GeneralSecurityException {
		initCipher();

		byte[] clearBytes = PBECipher.decryptWithBase64Coding(encryptedText, pbeKey, null, TRANSFORM);


		CharBuffer cb = Charset.forName("UTF-8").decode(
				ByteBuffer.wrap(clearBytes));
		char[] clearChars = new char[cb.remaining()];
		cb.get(clearChars);


		Arrays.fill(cb.array(), ' ');
		Arrays.fill(clearBytes, (byte)127);

		return clearChars;
	}

	public static char[] decryptPwd(String encpwd) throws GeneralSecurityException {
		char[] passArr = encpwd.toCharArray();
		if (isEncryptedPwd(encpwd)) {
			passArr = decryptText(encpwd.substring("%%%".length()));
		}
		return passArr;
	}

	public static String encryptPwd(char[] clrpwd) throws GeneralSecurityException {
		return "%%%" + encryptText(clrpwd);
	}
}
