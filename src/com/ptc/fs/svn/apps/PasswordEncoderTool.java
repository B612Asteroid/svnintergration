package com.ptc.fs.svn.apps;

import com.ptc.fs.svn.utils.CommonUtils;
import com.ptc.fs.svn.utils.SVNAESCipher;
import com.ptc.fs.svn.utils.SimplePBECipher;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.UUID;

public class PasswordEncoderTool
{
	static String salt = "";

	public static void main(String[] args) {
		salt = generateSalt();
		encryptPassword();
	}


	private static void encryptPassword() {
		try {
			char[] chaArray = getInputPassword();

			String encryptedPassword = SVNAESCipher.encryptPwd(chaArray);
			System.out.println("Encrypted password : " + encryptedPassword + "\n");
			System.out.println("( This will get set as a value of property - \nsvn.im.integrationuser.encryptedpassword in - $SVN_IM_INSTALL_DIR/config/SVNIntegration.properties. ) \n");

			chaArray = null;
			changePropertyValue("svn.im.integrationuser.encryptedpassword", encryptedPassword);
		}
		catch (GeneralSecurityException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		} 
	}

	private static String generateSalt() {
		String generatedSalt = null;
		try {
			System.out.println("Do you wish to generate encryption salt as well? [yn](y)");
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			String choice = bufferRead.readLine();
			if (choice.equalsIgnoreCase("y")) {
				UUID uuid = UUID.randomUUID();
				generatedSalt = uuid.toString();
				System.out.println("Do you wish to encrypt the salt? [yn](y)");
				choice = bufferRead.readLine();
				if (choice.equalsIgnoreCase("y")) {
					generatedSalt = SimplePBECipher.encryptPwd(generatedSalt);
				}
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		catch (GeneralSecurityException ex) {
			ex.printStackTrace();
		} 

		if (null != generatedSalt) {
			System.out.println("Generated Salt : " + generatedSalt + "\n");
			System.out.println("(This will get set as a value of property - \nsvn.im.encryption.salt in - $SVN_IM_INSTALL_DIR/config/SVNIntegration.properties. )");


			System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - \n");
			changePropertyValue("svn.im.encryption.salt", generatedSalt);
		} 

		return generatedSalt;
	}

	private static char[] getPassword() throws IOException {
		char[] array = null;
		Console cnsl = System.console();
		if (cnsl != null) {
			array = cnsl.readPassword();
		} else {

			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			array = bufferRead.readLine().toCharArray();
		} 
		return array;
	}

	private static char[] getInputPassword() throws IOException {
		boolean equal = false;
		char[] array = new char[1];
		int attemptCount = 0;
		while (false == equal && attemptCount < 3) {
			System.out.println("Enter password to encrypt :");
			char[] s = getPassword();
			System.out.println("Enter password to encrypt( again ):");
			char[] s2 = getPassword();
			attemptCount++;
			if (Arrays.equals(s, s2)) {
				equal = true;
				array = s;
				continue;
			} 
			System.out.println("Both passwords didn't match. Please retry. \n");
		} 

		if (attemptCount >= 3) {
			System.out.println("You have exceeded maximum permitted attempts.\n");
		}
		return array;
	}

	private static void changePropertyValue(String propertyName, String propertyValue) {
		BufferedReader bufferReaderOrigFile = null;
		PrintWriter printWriterTempFile = null;
		BufferedReader bufferReaderTempFile = null;
		PrintWriter printWriterOrigFile = null;
		File tempFile = null;
		boolean isPWStreamClosed = false;
		try {
			String propertyFileName = CommonUtils.getAppConfigFileName();
			File tempDirectory = new File(CommonUtils.getTempDir());
			tempFile = File.createTempFile("temp", ".tmp", tempDirectory);

			FileInputStream fisOrigFile = new FileInputStream(propertyFileName);
			bufferReaderOrigFile = new BufferedReader(new InputStreamReader(fisOrigFile));

			FileOutputStream fosTempFile = new FileOutputStream(tempFile);
			printWriterTempFile = new PrintWriter(fosTempFile);
			String currLine = "";
			while ((currLine = bufferReaderOrigFile.readLine()) != null) {
				if (currLine.contains(propertyName)) {
					printWriterTempFile.println(propertyName + "=" + propertyValue);

					continue;
				} 
				printWriterTempFile.println(currLine);
			} 
			printWriterTempFile.flush();
			printWriterTempFile.close();


			FileInputStream fisTempFile = new FileInputStream(tempFile);
			bufferReaderTempFile = new BufferedReader(new InputStreamReader(fisTempFile));

			FileOutputStream fosOrigFile = new FileOutputStream(propertyFileName);
			printWriterOrigFile = new PrintWriter(fosOrigFile);
			while ((currLine = bufferReaderTempFile.readLine()) != null) {
				printWriterOrigFile.println(currLine);
			}
			printWriterOrigFile.flush();
			printWriterOrigFile.close();

			isPWStreamClosed = true;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {

			if (!isPWStreamClosed) {
				if (printWriterTempFile != null) {
					printWriterTempFile.close();
				}
				if (printWriterOrigFile != null)
					printWriterOrigFile.close(); 
			} 
			try {
				bufferReaderOrigFile.close();
				bufferReaderTempFile.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			} 

			tempFile.delete();
		} 
	}
}