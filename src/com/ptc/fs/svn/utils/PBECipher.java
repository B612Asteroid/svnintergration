package com.ptc.fs.svn.utils;

import com.mks.api.util.Base64;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;


public abstract class PBECipher
{
	public static final String ENCRYPTED_PWD_PREFIX = "%%%";

	protected static byte[] encrypt(byte[] clearBytes, Key pbeKey, AlgorithmParameterSpec pbeParamSpec, String algorithm) throws GeneralSecurityException {
		Cipher cipher = null;
		byte[] encBytes = null;
		cipher = Cipher.getInstance(algorithm);
		cipher.init(1, pbeKey, pbeParamSpec);
		encBytes = cipher.doFinal(clearBytes);
		return encBytes;
	}

	protected static String encryptWithBase64Coding(byte[] clearBytes, Key pbeKey, AlgorithmParameterSpec pbeParamSpec, String algorithm) throws GeneralSecurityException {
		byte[] bytes = encrypt(clearBytes, pbeKey, pbeParamSpec, algorithm);
		return Base64.encode(bytes);
	}

	protected static byte[] decrypt(byte[] encBytes, Key pbeKey, AlgorithmParameterSpec pbeParamSpec, String algorithm) throws GeneralSecurityException {
		if (encBytes == null) {
			throw new IllegalArgumentException("null");
		}
		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(2, pbeKey, pbeParamSpec);
		byte[] clearBytes = cipher.doFinal(encBytes);
		return clearBytes;
	}

	protected static byte[] decryptWithBase64Coding(String encStr, Key pbeKey, AlgorithmParameterSpec pbeParamSpec, String algorithm) throws GeneralSecurityException {
		byte[] encBytes = Base64.decodeToBytes(encStr);
		return decrypt(encBytes, pbeKey, pbeParamSpec, algorithm);
	}

	public static boolean isEncryptedPwd(String encpwd) {
		return (encpwd == null) ? false : encpwd
				.startsWith("%%%");
	}
}
