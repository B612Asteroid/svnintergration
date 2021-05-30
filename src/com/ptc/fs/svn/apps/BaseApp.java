package com.ptc.fs.svn.apps;

import com.ptc.fs.svn.utils.CommonUtils;
import com.ptc.fs.svn.utils.SVNLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class BaseApp
{
	protected static Properties commonProperties = null;

	protected static final String userDir;
	protected static final String userHome;
	protected static final String userName;
	protected static final String installDir;
	protected static final String capabilityName;
	protected static final String capabilityDescription;
	protected static final boolean isCapabilityMandatory;
	protected static File logFile = null;

	static {
		userDir = System.getProperty("user.dir");
		userHome = System.getProperty("user.home");
		userName = System.getProperty("user.name");
		commonProperties = CommonUtils.getCommonAppProperties();
		installDir = CommonUtils.getInstallDir();

		capabilityName = commonProperties.getProperty("svn.im.capability.name");
		capabilityDescription = commonProperties.getProperty("svn.im.capability.description");
		isCapabilityMandatory = Boolean.valueOf(commonProperties.getProperty("svn.im.capability.mandatory")).booleanValue();
	}

	protected static void configureLogger(String appName) {
		SimpleDateFormat sdfDate = new SimpleDateFormat("MMM-dd-yyyy__HH-mm-ss-SSS");

		String date = sdfDate.format(new Date());

		try {
			String filename = appName + "__" + date + ".log";
			logFile = new File(CommonUtils.getLogDir(), filename);

			Properties loggerProperties = CommonUtils.getLoggerProperties();

			SVNLogger.configureLogger(loggerProperties, logFile);
		} catch (Throwable throwable) {}
	}
}