package com.ptc.fs.svn.apps;

import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.mks.api.response.Item;
import com.mks.api.response.Response;
import com.mks.api.response.Result;
import com.ptc.fs.svn.commands.CommandsFactory;
import com.ptc.fs.svn.connections.ConnectionManager;
import com.ptc.fs.svn.data.ChangePackage;
import com.ptc.fs.svn.data.ChangePackageEntry;
import com.ptc.fs.svn.data.ChangePackageEntry.OPTYPE;
import com.ptc.fs.svn.utils.CommonUtils;
import com.ptc.fs.svn.utils.SVNAppTokens;
import java.io.File;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.svn.cli.svnlook.SVNLookCommandEnvironment;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.admin.ISVNChangeEntryHandler;
import org.tmatesoft.svn.core.wc.admin.SVNChangeEntry;
import org.tmatesoft.svn.core.wc.admin.SVNLookClient;

public class CreateCPApp extends BaseApp {
	public static class SVNChangeEntryCollect implements ISVNChangeEntryHandler {
		private List<SVNChangeEntry> entries;

		public SVNChangeEntryCollect(List<SVNChangeEntry> entries) {
			this.entries = entries;
		}
		
		public List<SVNChangeEntry> getEntries() {
			return this.entries;
		}

		@Override
		public void handleEntry(SVNChangeEntry entry) throws SVNException {
			entries.add(entry);
		}
	}
	
	static final String repositoryurl;
	static String APP_NAME = "CreateCPApp";
	static ExecutorService pool = Executors.newFixedThreadPool(20);
	static {
		configureLogger(APP_NAME);
		repositoryurl = System.getenv("SVN_REPOSITOTY_URL");
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
	
	private static String getSummary(String text) {
		String[] lines = text.split("\\r?\\n");
		if(lines == null || lines.length < 2){
			return null;
		}
		return lines[1];
	}
	
	private static String getDescription(String text) {
		String[] lines = text.split("\\r?\\n");
		if(lines == null || lines.length < 3){
			return null;
		}
		String delim = "";
		String result = "";
		for(int i= 2; i < lines.length; i++) {
			result = result + delim + lines[i];
			delim = "\n";
		}
		return result;
	}
	
	
	private static ChangePackage getCP(String repo, SVNRevision txn) {

		try {
			SVNLookCommandEnvironment environment = new SVNLookCommandEnvironment(getProgramName(), System.out, System.err, System.in);
			environment.initClientManager();
			SVNLookClient client = environment.getClientManager().getLookClient();
			SVNLogEntry logEntry = null;
			File fRepo = new File(repo);

			List<SVNChangeEntry> entries = new ArrayList<SVNChangeEntry>();
			SVNChangeEntryCollect entryCollect = new SVNChangeEntryCollect(entries);

			logEntry = client.doGetInfo(fRepo, txn);
			String issueId = getIssueId(logEntry.getMessage());
			if(null == issueId){
				System.err.println("Ther first line of comment is #ALM_ID.");
				return null;
			}

			client.doGetChanged(fRepo, txn, entryCollect, false);

			String author = logEntry.getAuthor();
			Date date = logEntry.getDate();
			String log = logEntry.getMessage(); 

			SimpleDateFormat df = new SimpleDateFormat("MMM d, yyyy h:mm:ss a", Locale.ENGLISH);
			ChangePackage cp = new ChangePackage();

			cp.setIssueId(issueId);
			
			cp.setCreatedby(author);
			cp.setCreateddate(df.format(date));
			cp.setDescription(getDescription(log));
			cp.setRepository(repositoryurl);
			cp.setRevision(txn.toString());
			cp.setSummary(getSummary(log));

			for(SVNChangeEntry entry : entryCollect.getEntries()) {
				ChangePackageEntry cpEntry = new ChangePackageEntry();

				cpEntry.setPath(entry.getPath());
				cpEntry.setCopyfrompath(entry.getCopyFromPath());
				cpEntry.setCopyfromrevision(entry.getCopyFromRevision());
				
				switch(entry.getType()) {
					case 'A': cpEntry.setAction(OPTYPE.A); break;
					case 'U': cpEntry.setAction(OPTYPE.U); break;
					case 'D': cpEntry.setAction(OPTYPE.D); break;
				}
				cp.getCPEntries().add(cpEntry);
			}
			//entryCollect.

			return cp;
		} catch (SVNException e) {
			e.printStackTrace();
			return null;
		} 
	}

	//repo, revi
	public static void main(String[] args) throws UnknownHostException {
		
		CommonUtils.outputAndLogMessage("GENERAL", APP_NAME + "'s argument list.");
		for(int i = 0; i < args.length; i++) {
			CommonUtils.outputAndLogMessage("GENERAL", "args["+i+"] = " + args[i]);
		}
	
		Session session = null;
		int nargs = args.length;
		if (nargs != 2) {
			CommonUtils.outputAndLogMessage("ERROR", APP_NAME + " - filename is not provided in the argument list.");
			System.exit(99);
		}
		String repo = args[0];
		SVNRevision revi = SVNRevision.parse(args[1]);
		
		int retVal = 1;
		String cpId = null;
		try {
			session = ConnectionManager.connectAPI();
			if (session != null) {
				ChangePackage currCP = getCP(repo, revi);//cpItr.next();
				cpId = createCP(session, currCP);
				retVal = 0;
				createCPEntries(session, cpId, currCP.getCPEntries());
			} 
		} catch (APIException ex) {
			CommonUtils.logException("ERROR", (Exception)ex);
			System.err.println(ex.getResponse().getCommandString());
			retVal = 1;
		} catch (GeneralSecurityException ex) {
			CommonUtils.logException("ERROR", ex);
			retVal = 1;
		} finally {
			try {
				if (retVal != 0 && null != cpId)
					deleteCP(session, cpId); 
			} catch (APIException ex) {
				CommonUtils.logException("ERROR", (Exception)ex);
			} 
			if (session != null)
				session.getIntegrationPoint().release(); 
		} 
		if (retVal != 0)
			CommonUtils.createErrorFile(logFile); 
		pool.shutdown();
		System.exit(retVal);
	}

	private static String createCP(Session session, ChangePackage cp) throws APIException {
		Response response;
		CmdRunner cmdRunner = session.createCmdRunner();
		List<Option> optionList = new ArrayList<>();
		
		optionList.add(new Option("issueID", cp.getIssueId()));
		optionList.add(new Option("type", SVNAppTokens.SVN_CPTYPE));

		optionList.add(new Option("attribute", "status=" + SVNAppTokens.SVN_CPSTATE_COMMITTED));
		optionList.add(new Option("attribute", "createdby=" + cp.getCreatedby()));
		optionList.add(new Option("attribute", "createddate=" + cp.getCreateddate()));
		optionList.add(new Option("attribute", "repository="+cp.getRepository()));
		optionList.add(new Option("attribute", "revision="+cp.getRevision()));
		if(null != cp.getSummary()) {
			optionList.add(new Option("attribute", "summary="+cp.getSummary()));
		}
		if(null != cp.getDescription()) {
			optionList.add(new Option("attribute", "description=" + cp.getDescription()));
		}
		Command command = CommandsFactory.createCPCommand(optionList);

		try {
			response = cmdRunner.execute(command);
		} finally {
			cmdRunner.release();
		} 
		String CreatedCPid = readCreateChangePackageResponse(response);
		return CreatedCPid;
	}

	private static String readCreateChangePackageResponse(Response response) throws APIException {
		String cpID = null;
		Result result = response.getResult();
		Item it = result.getPrimaryValue();
		cpID = it.getId();
		CommonUtils.outputAndLogMessage("GENERAL", "Change Package created : " + cpID);
		return cpID;
	}

	private static void createCPEntry(Session session, String CPID, ChangePackageEntry entry) throws APIException {
		CmdRunner cmdRunner = session.createCmdRunner();
		synchronized (cmdRunner) {

			List<Option> optionList = new ArrayList<>();
			optionList.add(new Option("changePackageID", CPID));
			optionList.add(new Option("attribute", "action=" + entry.getAction().getDescription()));
			optionList.add(new Option("attribute", "path=" + entry.getPath()));
			if(null != entry.getCopyfrompath()) {
				optionList.add(new Option("attribute", "copyfrompath=" + entry.getCopyfrompath()));
			}
			if(entry.getCopyfromrevision() > 0) {
				optionList.add(new Option("attribute", "revision=" + entry.getCopyfromrevision()));
			}
	        			
			Command command = CommandsFactory.createCPEntryCommand(optionList);
			try {
				cmdRunner.execute(command);
			} finally {
				cmdRunner.release();
			} 
		} 
	}

	private static void createCPEntries(Session session, String CPID, List<ChangePackageEntry> entries) throws APIException {
		int no_entries = entries.size();
		List<Future<Boolean>> list = new ArrayList<>();
		for (int i = 0; i < no_entries; i++) {
			Callable<Boolean> callable = new CreateCpEntryCallable(session, CPID, entries.get(i));
			Future<Boolean> future = pool.submit(callable);
			list.add(future);
		} 
		for (Future<Boolean> future : list) {
			try {
				future.get();
			} catch (InterruptedException e) {
				throw new APIException(e);
			}
			catch (ExecutionException e) {
				throw new APIException(e);
			} 
		} 
	}

	private static void deleteCP(Session session, String cpId) throws APIException {
		CmdRunner cmdRunner = session.createCmdRunner();
		HashMap<String, String> parameters = new HashMap<>();
		parameters.put("noconfirm", null);
		parameters.put("deleteEntries", null);
		List<String> selectionList = new ArrayList<>();
		selectionList.add(cpId);
		Command command = CommandsFactory.deleteCPCommand(parameters, selectionList);
		cmdRunner.execute(command);
		CommonUtils.outputAndLogMessage("GENERAL", "Change Package deleted : " + cpId);
	}

	private static class CreateCpEntryCallable implements Callable<Boolean> {
		private ChangePackageEntry entry;
		private Session session;
		private String CPID;

		public CreateCpEntryCallable(Session session, String CPID, ChangePackageEntry entry) {
			this.entry = entry;
			this.session = session;
			this.CPID = CPID;
		}

		public Boolean call() throws APIException {
			CreateCPApp.createCPEntry(this.session, this.CPID, this.entry);
			return Boolean.TRUE;
		}
	}
}