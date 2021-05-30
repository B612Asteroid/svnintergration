package com.ptc.fs.svn.utils;

import com.mks.api.util.MKSLogger;
import java.io.File;
import java.util.Properties;

public class SVNLogger
{
	static MKSLogger apiLogger;

	public static void configureLogger(Properties properties, File logFile) {
		apiLogger = new MKSLogger(logFile);
		apiLogger.configure(properties, SVNAppTokens.SVN_LOGGING_CATEGORY_PREFIX);
	}

	public static void logMessage(String category, String message) {
		if (apiLogger == null) {
			return;
		}
		apiLogger.message(category, message);
	}

	public static void logMessage(String category, int level, String message) {
		if (apiLogger == null) {
			return;
		}
		apiLogger.message(category, level, message);
	}

	public static void logException(String category, Throwable exception) {
		if (apiLogger == null)
			return; 
		apiLogger.exception(category, 0, exception);
	}

	public static void stop() {
		apiLogger.stop();
		apiLogger = null;
	}
}
