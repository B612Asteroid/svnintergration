package com.ptc.fs.svn.utils;

import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

//@Deprecated
public final class SimplePBECipher
extends PBECipher
{
	private static PBEParameterSpec pbeParamSpec;
	private static SecretKey pbeKey;
	private static boolean cipherHasBeenInitialized = false;
	private static final String TRANSFORM = "PBEWithMD5AndDES";

	private static void initCipher() throws GeneralSecurityException {
		if (cipherHasBeenInitialized == true) {
			return;
		}
		char[] PWD = "The top of the world".toCharArray();

		byte[] salt = { -57, 115, 33, -116, 126, -56, -18, -103 };


		int count = 30;
		pbeParamSpec = new PBEParameterSpec(salt, count);
		PBEKeySpec pbeKeySpec = new PBEKeySpec(PWD);
		SecretKeyFactory keyFac = SecretKeyFactory.getInstance(TRANSFORM);
		pbeKey = keyFac.generateSecret(pbeKeySpec);
		cipherHasBeenInitialized = true;
	}

	public static String encryptString(String clrstr) throws GeneralSecurityException {
		initCipher();
		return PBECipher.encryptWithBase64Coding(clrstr.getBytes(), pbeKey, pbeParamSpec, TRANSFORM);
	}

	public static String decryptString(String encstr) throws GeneralSecurityException {
		initCipher();
		return new String(PBECipher.decryptWithBase64Coding(encstr, pbeKey, pbeParamSpec, TRANSFORM));
	}

	public static String decryptPwd(String encpwd) throws GeneralSecurityException {
		if (!isEncryptedPwd(encpwd)) {
			String msg = MessageFormat.format("A valid encrypted password must begin with \"{0}\".", new Object[] { "%%%" });


			throw new GeneralSecurityException(msg);
		} 
		return decryptString(encpwd.substring("%%%".length()));
	}

	public static String encryptPwd(String clrpwd) throws GeneralSecurityException {
		return "%%%" + encryptString(clrpwd);
	}
}
