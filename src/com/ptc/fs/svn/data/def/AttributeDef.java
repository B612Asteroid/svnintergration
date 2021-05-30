package com.ptc.fs.svn.data.def;

import java.util.Comparator;

public class AttributeDef
{
	private String name = null;
	private String displayName = null;
	private String dataType = null;
	private boolean isReadOnly = false;
	private boolean isMandatory = false;
	private Integer position = Integer.valueOf(-1);
	private boolean isEntryAttribute = false;
	private String changePackageTypeName = null;

	public AttributeDef() {}

	public AttributeDef(String changePackageTypeName, String name, String displayName, String dataType, int position, boolean isReadOnly, boolean isMandatory, boolean isEntryAttribute) {
		this.changePackageTypeName = changePackageTypeName;
		this.name = name;
		this.displayName = displayName;
		this.dataType = dataType;
		this.position = Integer.valueOf(position);
		this.isReadOnly = isReadOnly;
		this.isMandatory = isMandatory;
		this.isEntryAttribute = isEntryAttribute;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true; 
		if (null == obj)
			return false; 
		if (getClass() != obj.getClass())
			return false; 
		AttributeDef other = (AttributeDef)obj;
		boolean equal = false;

		equal = (areDataTypesEqual(this.dataType, other.dataType) && areStringsEqual(this.displayName, other.displayName) && areStringsEqual(this.name, other.name) && this.isEntryAttribute == other.isEntryAttribute && this.isMandatory == other.isMandatory && this.isReadOnly == other.isReadOnly);

		return equal;
	}

	public String getChangePackageTypeName() {
		return this.changePackageTypeName;
	}

	public void setChangePackageTypeName(String changePackageTypeName) {
		this.changePackageTypeName = changePackageTypeName;
	}

	private boolean areDataTypesEqual(String dataTypeOne, String dataTypeTwo) {
		boolean equal = dataTypeOne.equalsIgnoreCase(dataTypeTwo);
		if (false == equal)
		{
			if (dataTypeOne.equals("ciuser") && dataTypeTwo.equals("user")) {
				equal = true;
			}
			else if (dataTypeOne.equals("user") && dataTypeTwo.equals("ciuser")) {
				equal = true;
			}
			else if (dataTypeOne.equals("changepackageid") && dataTypeTwo
					.equals("cpid")) {
				equal = true;
			}
			else if (dataTypeOne.equals("cpid") && dataTypeTwo
					.equals("changepackageid")) {
				equal = true;
			} 
		}
		return equal;
	}

	private boolean areStringsEqual(String current, String other) {
		boolean bRet = false;
		if (null != current && current.equals(other))
			bRet = true; 
		if (null == current && null == other)
			bRet = true; 
		return bRet;
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;

		result = prime * result + ((this.dataType == null) ? 0 : this.dataType.toLowerCase().hashCode());
		result = prime * result + ((this.displayName == null) ? 0 : this.displayName.hashCode());
		result = prime * result + (this.isEntryAttribute ? 1231 : 1237);
		result = prime * result + (this.isMandatory ? 1231 : 1237);
		result = prime * result + (this.isReadOnly ? 1231 : 1237);
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}

	public Integer getPosition() {
		return this.position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDataType() {
		return this.dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public boolean isReadOnly() {
		return this.isReadOnly;
	}

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	public boolean isMandatory() {
		return this.isMandatory;
	}

	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	public boolean isEntryAttribute() {
		return this.isEntryAttribute;
	}

	public void setEntryAttribute(boolean isEntryAttribute) {
		this.isEntryAttribute = isEntryAttribute;
	}

	public String toString() {
		StringBuffer sBuf = new StringBuffer(1024);
		sBuf.append("Attribute Name : ").append(this.name).append("\n");
		sBuf.append("Attribute Display Name : ").append(this.displayName)
		.append("\n");

		String visibleDataType = this.dataType.toLowerCase();
		if (visibleDataType.equals("ciuser")) {
			visibleDataType = "user";
		}
		else if (visibleDataType.equals("changepackageid")) {
			visibleDataType = "cpid";
		} 
		sBuf.append("Data Type : ").append(visibleDataType).append("\n");
		sBuf.append("Mandatory : ").append(this.isMandatory).append("\n");
		sBuf.append("Read Only : ").append(this.isReadOnly).append("\n");

		sBuf.append("- - - - - - - - - - - - - - - \n");
		return sBuf.toString();
	}

	public static class AttributeDefComparator
	implements Comparator<AttributeDef>
	{
		public int compare(AttributeDef o1, AttributeDef o2) {
			int result = o1.getName().compareTo(o2.getName());
			if (0 == result) {
				result = o1.getDisplayName().compareTo(o2.getDisplayName());
			}
			return result;
		}
	}
}
