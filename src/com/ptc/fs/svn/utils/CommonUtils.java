package com.ptc.fs.svn.utils;

import com.ptc.fs.svn.data.def.AttributeDef;
import com.ptc.fs.svn.data.def.ChangePackageDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class CommonUtils {

	private static Properties commonProperties = null;
	private static Properties loggerProperties = null;

	public static final String fileSeparator = System.getProperty("file.separator");
	public static final String lineSeparator = System.getProperty("line.separator");

	private static final String installDir = System.getenv("SVN_IM_INSTALL_DIR");
	private static final String configDir = installDir + fileSeparator + "config";
	private static final String logDir = installDir + fileSeparator + "logs";

	public static ChangePackageDef getExpectedCPDef() {
		ChangePackageDef expectedChangePackage = createCPType();
		expectedChangePackage.setAttributeDefs(getCPAttributes());
		expectedChangePackage.setEntryAttributeDefs(getCPEntryAttributes());
		expectedChangePackage.setEntrykeys(getEntryKeys());
		return expectedChangePackage;
	}

	private static ChangePackageDef createCPType() {
		ChangePackageDef expectedChangePackage = new ChangePackageDef();
		expectedChangePackage.setName(SVNAppTokens.SVN_CPTYPE);
		expectedChangePackage.setDisplayName(SVNAppTokens.SVN_CPTYPE_DISPLAYNAME);
		expectedChangePackage.setDescription(SVNAppTokens.SVN_CPTYPE_DESCRIPTION);
		return expectedChangePackage;
	}

	private static List<AttributeDef> getCPAttributes() {
		List<AttributeDef> listAttr = new ArrayList<>();
		int pos = 1;

		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, "id", "ID", "cpid", pos++, true, false, false));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, SVNAppTokens.SVN_REPO, SVNAppTokens.SVN_REPO_DISPLAYNAME, "string", pos++, false, true, false));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, SVNAppTokens.SVN_REV, SVNAppTokens.SVN_REV_DISPLAYNAME, "integer", pos++, false, true, false));
		pos++;
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, "summary", "Summary", "string", pos++, false, false, false));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, "status", "State", "stringlist", pos++, false, true, false));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, "entrycount", "Entry Count", "integer", pos++, true, false, false));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, "createdby", "Created By", "user", pos++, false, true, false));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, "createddate", "Created Date", "date", pos++, false, true, false));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, SVNAppTokens.SVN_DESCRIPTION, SVNAppTokens.SVN_DESCRIPTION_DISPLAYNAME, "string", pos++, false, false, false));

		Collections.sort(listAttr, (Comparator<? super AttributeDef>)new AttributeDef.AttributeDefComparator());
		return listAttr;
	}


	private static List<AttributeDef> getCPEntryAttributes() {
		List<AttributeDef> listAttr = new ArrayList<>();
		int pos = 1;

		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, SVNAppTokens.SVN_CPENTRY_PATH, SVNAppTokens.SVN_CPENTRY_PATH_DISPLAYNAME, "string", pos++, false, true, true));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, SVNAppTokens.SVN_CPENTRY_ACTION, SVNAppTokens.SVN_CPENTRY_ACTION_DISPLAYNAME, "string", pos++, false, true, true));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, SVNAppTokens.SVN_CPENTRY_COPY_FROM_PATH, SVNAppTokens.SVN_CPENTRY_COPY_FROM_PATH_DISPLAYNAME, "string", pos++, false, false, true));
		listAttr.add(new AttributeDef(SVNAppTokens.SVN_CPTYPE, SVNAppTokens.SVN_CPENTRY_REV, SVNAppTokens.SVN_CPENTRY_REV_DISPLAYNAME, "integer", pos++, false, false, true));

		Collections.sort(listAttr, (Comparator<? super AttributeDef>)new AttributeDef.AttributeDefComparator());
		return listAttr;
	}
	
	private static List<String> getEntryKeys() {
		List<String> listEntryKeys = new ArrayList<>();
		
		listEntryKeys.add(SVNAppTokens.SVN_CPENTRY_ACTION);
		listEntryKeys.add(SVNAppTokens.SVN_CPENTRY_PATH);

		return listEntryKeys;
	}

	public static boolean areStringListsEqual(List<String> currentStringList, List<String> otherStringList) {
		boolean bRet = false;
		if (null == currentStringList && null == otherStringList) {
			bRet = true;
		} else if (null != otherStringList && null != currentStringList && 
				otherStringList.size() == currentStringList.size()) {
			boolean stillEqual = true;
			Iterator<String> itr = otherStringList.iterator();
			while (itr.hasNext()) {
				String otherStr = itr.next();
				if (!currentStringList.contains(otherStr)) {
					stillEqual = false;
					break;
				} 
			} 
			if (stillEqual) {
				bRet = true;
			}
		} 

		return bRet;
	}

	public static boolean areStringsEqual(String current, String other) {
		boolean bRet = false;
		if (null != current && current.equals(other))
			bRet = true; 
		if (null == current && null == other)
			bRet = true; 
		return bRet;
	}

	public static Properties getCommonAppProperties() {
		if (null == commonProperties) {
			String propFileName = getAppConfigFileName();
			commonProperties = new Properties();
			InputStream input = null;
			try {
				input = new FileInputStream(propFileName);
				commonProperties.load(input);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
			} 
		} 
		return commonProperties;
	}

	public static Properties getLoggerProperties() {
		if (null == loggerProperties) {

			File loggerPropertiesFile = new File(getAppLogConfigFileName());

			if (loggerPropertiesFile.exists()) {
				loggerProperties = new Properties();
				InputStream iStream = null;
				try {
					iStream = new FileInputStream(loggerPropertiesFile);
					loggerProperties.load(iStream);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (null != iStream) {
						try {
							iStream.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
				} 
			} else {

				loggerProperties = loadDefaultLoggerProperties();
			} 
		} 
		return loggerProperties;
	}

	public static String getAppConfigFileName() {
		String propFileName = installDir + fileSeparator + "config" + fileSeparator + "SVNIntegration.properties";

		return propFileName;
	}

	public static String getAppLogConfigFileName() {
		String propFileName = configDir + fileSeparator + "Logger.properties";
		return propFileName;
	}

	public static String getInstallDir() {
		return installDir;
	}


	public static String getConfigDir() {
		return configDir;
	}

	public static String getLogDir() {
		return logDir;
	}

	public static String getTempDir() {
		String path = installDir + fileSeparator + "tmp";
		File tmpdir = new File(path);
		if (!tmpdir.exists() || !tmpdir.isDirectory()) {
			path = System.getProperty("java.io.tmpdir");
		}
		return path;
	}

	public static Properties loadDefaultLoggerProperties() {
		Properties loggerProperties = new Properties();

		loggerProperties.put("svn.im.logging.exception.includeCategory.GENERAL", "10");
		loggerProperties.put("svn.im.logging.exception.includeCategory.ERROR", "10");
		loggerProperties.put("svn.im.logging.exception.includeCategory.WARNING", "10");
		loggerProperties.put("svn.im.logging.exception.includeCategory.DEBUG", "10");
		loggerProperties.put("svn.im.logging.exception.includeCategory.SVN", "10");
		loggerProperties.put("svn.im.logging.exception.defaultFormat", "{2} {4} {7,date,yyyy-MM-dd H:m:s,S}: {6}" + lineSeparator);
		loggerProperties.put("svn.im.logging.message.includeCategory.GENERAL", "10");
		loggerProperties.put("svn.im.logging.message.includeCategory.ERROR", "10");
		loggerProperties.put("svn.im.logging.message.includeCategory.WARNING", "10");
		loggerProperties.put("svn.im.logging.message.includeCategory.DEBUG", "10");
		loggerProperties.put("svn.im.logging.message.includeCategory.SVN", "10");
		loggerProperties.put("svn.im.logging.message.defaultFormat", "{2}({3}) {5,date,yyyy-MM-dd H:m:s,S}: {4}" + lineSeparator);

		return loggerProperties;
	}

	public static void outputAndLogMessage(String category, String message) {
		System.out.println(message);
		SVNLogger.logMessage(category, message);
	}

	public static void logException(String category, Exception ex) {
		outputAndLogMessage(category, ex.getMessage());
		SVNLogger.logException(category, ex);
	}

	public static void createErrorFile(File origFile) {
		SVNLogger.stop();
		String fileName = origFile.getPath() + "_error";
		File dest = new File(fileName);
		origFile.renameTo(dest);
	}
}
