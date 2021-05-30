package com.ptc.fs.svn.data;

import java.util.ArrayList;
import java.util.List;

public class ChangePackage
{
	List<ChangePackageEntry> cpEntries = new ArrayList<>();
	
	private String issueId = null;
	private String createdby = null;
	private String createddate = null;
//	private String hostname = null;
	private String description = null;
	private String repository = null;
	private String revision = null;
	private String summary = null;
	
	public ChangePackage() {}

	public ChangePackage(List<ChangePackageEntry> cpEntries, String issueId, String createdby, String createddate,
//			String hostname,
			String description, String repository, String revision){
		this.cpEntries = cpEntries;
		this.setIssueId(issueId);
		this.setCreatedby(createdby);
		this.setCreateddate(createddate);
//		this.setHostname(hostname);
		this.setDescription(description);
		this.setRepository(repository);
		this.setRevision(revision);
		this.setSummary("SVN R"+revision);
	}

	public List<ChangePackageEntry> getCPEntries() {
		return this.cpEntries;
	}

	public void addCPEntry(ChangePackageEntry cpe) {
		this.cpEntries.add(cpe);
	}

	public void setCpEntries(List<ChangePackageEntry> cpEntries) {
		this.cpEntries = cpEntries;
	}

//	public String getHostname() {
//		return hostname;
//	}
//
//	public void setHostname(String hostname) {
//		this.hostname = hostname;
//	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getIssueId() {
		return issueId;
	}

	public void setIssueId(String issueId) {
		this.issueId = issueId;
	}

	public String getCreatedby() {
		return createdby;
	}

	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}

	public String getCreateddate() {
		return createddate;
	}

	public void setCreateddate(String createddate) {
		this.createddate = createddate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

}
