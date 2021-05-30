package com.ptc.fs.svn.data.def.utils;

import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.ptc.fs.svn.commands.CommandsFactory;
import com.ptc.fs.svn.utils.SVNAppTokens;
import java.util.ArrayList;
import java.util.List;

public class UpdateOutputDef
{
	String commitId;
	String cpId;
	boolean isCP;

	public UpdateOutputDef(String commitId, String issueId, boolean isCP) {
		this.commitId = commitId;
		this.cpId = issueId;
		this.isCP = isCP;
	}

	public String getCommitId() {
		return this.commitId;
	}

	public String getCpId() {
		return this.cpId;
	}

	public boolean IsCP() {
		return this.isCP;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

	public void setCpId(String issueId) {
		this.cpId = issueId;
	}

	public void setIsCP(boolean isCP) {
		this.isCP = isCP;
	}

	public String getCpIdFromIssue(Session session) throws APIException {
		String CPId = null;
		if (this.isCP)
			return this.cpId; 
		CmdRunner cmdRunner = session.createCmdRunner();
		List<String> selectionList = new ArrayList<>();
		selectionList.add(this.cpId);
		List<Option> optionList = new ArrayList<>();
		optionList.add(new Option("noshowEntries", (String)null));
		optionList.add(new Option("filter", "type:" + SVNAppTokens.SVN_CPTYPE));
		optionList.add(new Option("attributes", "id"));
		optionList.add(new Option("attributes", "commit_id"));
		Command command = CommandsFactory.viewCPCommand(optionList, selectionList);

		try {
			Response response = cmdRunner.execute(command);
			for (WorkItemIterator i = response.getWorkItems(); i.hasNext(); ) {
				WorkItem workItem = null;
				workItem = i.next();
				CPId = workItem.getId();
				String sha = workItem.getField("commit_id").getValueAsString();
				if (sha.contentEquals(this.commitId)) {
					this.cpId = CPId;
					this.isCP = true;
					break;
				} 
			} 
		} finally {
			cmdRunner.release();
		} 
		return CPId;
	}
}
