package com.ptc.fs.svn.apps;

import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.ptc.fs.svn.commands.CommandUtils;
import com.ptc.fs.svn.connections.ConnectionManager;
import com.ptc.fs.svn.data.def.ChangePackageDef;
import com.ptc.fs.svn.data.def.utils.DefUtils;
import com.ptc.fs.svn.utils.CommonUtils;
import com.ptc.fs.svn.utils.SVNAppTokens;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class VerificationApp
extends BaseApp
{
	static {
		configureLogger("VerifyApp");
	}

	public static void main(String[] args) {
		Session session = null;
		try {
			session = ConnectionManager.connectAPI();
			if (session != null) {
				validateChangePackageDef(session);
			}
		} catch (APIException ex) {
			CommonUtils.logException("ERROR", (Exception)ex);
		} catch (GeneralSecurityException ex) {
			CommonUtils.logException("ERROR", ex);
		} finally {
			if (session != null) {
				session.getIntegrationPoint().release();
			}
		} 
	}

	public static boolean validateChangePackageDef(Session session) throws APIException {
		boolean validDefinition = false;

		ChangePackageDef serverCPDefinition = CommandUtils.getServerCPDefinition(session, SVNAppTokens.SVN_CPTYPE);

		ChangePackageDef expectedCPDefinition = CommonUtils.getExpectedCPDef();

		validDefinition = expectedCPDefinition.equals(serverCPDefinition);

		CommonUtils.outputAndLogMessage("GENERAL", "Do server and integration definitions match? " + (validDefinition ? "Yes" : "No"));

		exportCPDefinitions(serverCPDefinition, expectedCPDefinition);
		return validDefinition;
	}

	private static void exportCPDefinitions(ChangePackageDef serverCPDefinition, ChangePackageDef expectedCPDefinition) {
		try {
			String actualDefExportFile = commonProperties.getProperty("svn.im.cp.definition.actual");
			if (null == actualDefExportFile) {
				actualDefExportFile = CommonUtils.getLogDir() + CommonUtils.fileSeparator + "actualServerDefinition.txt";
			}

			if (null != actualDefExportFile) {
				CommonUtils.outputAndLogMessage("GENERAL", "Exporting \"Actual Server CP Definition\" to : " + actualDefExportFile);
				DefUtils.exportAsTextFile(serverCPDefinition, actualDefExportFile);
			} 

			String expectedDefExportFile = commonProperties.getProperty("svn.im.cp.definition.expected");
			if (null == expectedDefExportFile) {
				CommonUtils.outputAndLogMessage("GENERAL", "Exporting \"Integration-expected CP Definition\" to : " + actualDefExportFile);
				actualDefExportFile = CommonUtils.getLogDir() + CommonUtils.fileSeparator + "expectedClientDefinition.txt";
			} 

			if (null != expectedDefExportFile) {
				DefUtils.exportAsTextFile(expectedCPDefinition, expectedDefExportFile);
			}
		}
		catch (IOException e) {
			CommonUtils.logException("ERROR", e);
		} 
	}
}