package com.ptc.fs.svn.connections;

import com.mks.api.CmdRunner;
import com.mks.api.IntegrationPoint;
import com.mks.api.IntegrationPointFactory;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.ptc.fs.svn.utils.CommonUtils;
import com.ptc.fs.svn.utils.SVNAESCipher;
import com.ptc.fs.svn.utils.SVNLogger;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Properties;

public class ConnectionManager
{
	private static final int API_VERSION_MAJOR = 4;
	private static final int API_VERSION_MINOR = 16;
	private static final int portNumber;
	private static final String userName;
	private static final String encryptedPassword;
	private static final boolean secureServer;
	private static final String hostname;

	static {
		Properties commonProps = CommonUtils.getCommonAppProperties();
		hostname = commonProps.getProperty("svn.im.server.hostname");
		portNumber = Integer.parseInt(commonProps.getProperty("svn.im.server.port"));
		userName = commonProps.getProperty("svn.im.integrationuser.username");
		encryptedPassword = commonProps.getProperty("svn.im.integrationuser.encryptedpassword");
		secureServer = Boolean.valueOf(commonProps.getProperty("svn.im.server.secure")).booleanValue();
	}


	public static Session connectAPI() throws APIException, GeneralSecurityException {
		Session session = null;

		String sessionName = "SVN_Integration_Session_" + Calendar.getInstance().getTimeInMillis();

		IntegrationPointFactory integrationFactory = IntegrationPointFactory.getInstance();
		SVNLogger.logMessage("DEBUG", 20, "Trying to connect to - [ " + hostname + " : " + portNumber + " ] with user - " + userName);

		try {
			IntegrationPoint integrationPoint = integrationFactory.createIntegrationPoint(hostname, portNumber, secureServer, API_VERSION_MAJOR, API_VERSION_MINOR);

			SVNLogger.logMessage("DEBUG", 20, "Integration Point created.");
			session = integrationPoint.createNamedSession("SVN_Integration_Session", null, userName, decrypt(encryptedPassword));
			SVNLogger.logMessage("DEBUG", 20, "Named session created : " + sessionName);
			CmdRunner cmdrunner = session.createCmdRunner();
			cmdrunner.execute(new String[] { "api", "ping" });
		} catch (APIException AEx) {
			AEx.setMessage("MKS1133411: Unable to connect to IM Server: " + AEx.getMessage());
			throw AEx;
		}
		catch (GeneralSecurityException GSEx) {
			throw new GeneralSecurityException("MKS1133411: Unable to connect to IM Server: " + GSEx
					.getMessage(), GSEx);
		} 
		SVNLogger.logMessage("DEBUG", 20, "API ping successful. ");

		return session;
	}

	private static String decrypt(String string) throws GeneralSecurityException {
		return new String(SVNAESCipher.decryptPwd(string));
	}
}
