package com.ptc.fs.svn.data;

public class ChangePackageEntry
{
	public enum OPTYPE
	{
		A("Added"), D("Deleted"), U("Updated");
		private String description = null;

		OPTYPE(String description) {
			this.description = description;
		}

		public String getDescription() {
			return this.description;
		}
	}

	private OPTYPE action = null;
	private String path = null;
	private String copyfrompath = null;
	private long copyfromrevision = 0;

	public ChangePackageEntry() {}

	public ChangePackageEntry(OPTYPE action, String path, String copyfrompath, long copyfromrevision) {
		this.setAction(action);
		this.setPath(path);
		this.setCopyfrompath(copyfrompath);
		this.setCopyfromrevision(copyfromrevision);
	}

	public OPTYPE getAction() {
		return action;
	}

	public void setAction(OPTYPE action) {
		this.action = action;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCopyfrompath() {
		return copyfrompath;
	}

	public void setCopyfrompath(String copyfrompath) {
		this.copyfrompath = copyfrompath;
	}

	public long getCopyfromrevision() {
		return copyfromrevision;
	}

	public void setCopyfromrevision(long copyfromrevision) {
		this.copyfromrevision = copyfromrevision;
	}

}
