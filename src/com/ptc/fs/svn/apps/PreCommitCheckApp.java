package com.ptc.fs.svn.apps;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.svn.cli.svnlook.SVNLookCommandEnvironment;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

import com.ptc.fs.svn.utils.CommonUtils;

public class PreCommitCheckApp extends BaseApp {
	
	static final String repositoryurl;
	static String APP_NAME = "PreCommitCheckApp";

	static {
		configureLogger(APP_NAME);
		Properties commonProps = CommonUtils.getCommonAppProperties();
		repositoryurl = commonProps.getProperty("svn.server.repository.url");
	}

	protected static String getProgramName() {
		return "jsvnlook";
	}

	private static String getIssueId(String text) {
		String[] lines = text.split("\\r?\\n");
		if(lines == null || lines.length == 0){
			return null;
		}

		Pattern p = Pattern.compile("^#([0-9]+)$");
		Matcher m = p.matcher(lines[0]);
		if (m.find()) {
			return m.group(1);
		} else {
			return null;
		}    	
	}
	
	//repo, revi
	public static void main(String[] args) throws UnknownHostException {
		System.err.println("arguments");
		for(String arg : args) {
			System.err.println(arg);
		}

		int nargs = args.length;
		if (nargs != 2) {
			CommonUtils.outputAndLogMessage("ERROR", APP_NAME + " - filename is not provided in the argument list.");
			System.exit(1);
		}
		String repo = args[0];
		String txn = args[1];
		
		SVNLookCommandEnvironment environment = new SVNLookCommandEnvironment(getProgramName(), System.out, System.err, System.in);
		try {
			environment.initClientManager();
			SVNLookClient client = environment.getClientManager().getLookClient();
			SVNLogEntry logEntry = null;
			File fRepo = new File(repo);

			logEntry = client.doGetInfo(fRepo, txn);
			String issueId = getIssueId(logEntry.getMessage());
			if(null == issueId){
				System.err.println("Ther first line of comment is #ALM_ID.");
				System.exit(2);
			}
		} catch (SVNException e) {
			e.printStackTrace();
			System.exit(3);
		}
	
		System.exit(0);
	}
}