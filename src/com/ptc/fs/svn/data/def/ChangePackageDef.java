package com.ptc.fs.svn.data.def;

import com.ptc.fs.svn.utils.CommonUtils;
import java.util.Iterator;
import java.util.List;

public class ChangePackageDef
{
	private String name;
	private String displayName;
	private String description;
	private List<AttributeDef> attributeDefs = null;
	private List<AttributeDef> entryAttributeDefs = null;
	private List<String> entrykeys = null;

	public ChangePackageDef() {}

	public ChangePackageDef(String name, String displayName, String description, List<AttributeDef> attributes, List<AttributeDef> entryAttributes, List<String> entrykeys) {
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.attributeDefs = attributes;
		this.entryAttributeDefs = entryAttributes;
		this.entrykeys = entrykeys;
	}

	public List<String> getEntrykeys() {
		return this.entrykeys;
	}

	public void setEntrykeys(List<String> entrykeys) {
		this.entrykeys = entrykeys;
	}

	public void addEntryKey(String entrykey) {
		this.entrykeys.add(entrykey);
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

	public List<AttributeDef> getAttributeDefs() {
		return this.attributeDefs;
	}

	public void setAttributeDefs(List<AttributeDef> attributes) {
		this.attributeDefs = attributes;
	}

	public void addCPAttributeDef(AttributeDef cpAttribute) {
		this.attributeDefs.add(cpAttribute);
	}

	public List<AttributeDef> getEntryAttributeDefs() {
		return this.entryAttributeDefs;
	}

	public void setEntryAttributeDefs(List<AttributeDef> entryAttributes) {
		this.entryAttributeDefs = entryAttributes;
	}

	public void addEntryAttributeDef(AttributeDef entryAttribute) {
		this.entryAttributeDefs.add(entryAttribute);
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		StringBuffer sBuf = new StringBuffer(4096);
		sBuf.append("Change Package Name : ").append(this.name).append("\n");
		sBuf.append("Change Package Display Name : ").append(this.displayName)
		.append("\n");
		sBuf.append("Change Package Description : ").append(this.description)
		.append("\n");
		sBuf.append("Attribute Definitions are : \n");
		sBuf.append("- - - - - - - - - - - - - - - - - - - - - - - - \n");
		if (null != this.attributeDefs) {
			for (AttributeDef attribute : this.attributeDefs) {
				String attributeAsString = attribute.toString();
				sBuf.append(attributeAsString);
			} 
		}
		sBuf.append("CP Entry Attribute Definitions are : \n");
		sBuf.append("- - - - - - - - - - - - - - - - - - - - - - - - \n");
		if (null != this.entryAttributeDefs) {
			for (AttributeDef attribute : this.entryAttributeDefs) {
				String attributeAsString = attribute.toString();
				sBuf.append(attributeAsString);
			} 
		}
		return sBuf.toString();
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());

		result = prime * result + ((this.displayName == null) ? 0 : this.displayName.hashCode());

		result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());

		result = prime * result + ((this.attributeDefs == null) ? 0 : this.attributeDefs.hashCode());



		result = prime * result + ((this.entryAttributeDefs == null) ? 0 : this.entryAttributeDefs.hashCode());

//		result = prime * result + ((this.entrykeys == null) ? 0 : this.entrykeys.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true; 
		if (obj == null)
			return false; 
		if (getClass() != obj.getClass())
			return false; 
		ChangePackageDef other = (ChangePackageDef)obj;

		boolean equal = (CommonUtils.areStringsEqual(this.name, other.name)
				&& CommonUtils.areStringsEqual(this.displayName, other.displayName)
				&& CommonUtils.areStringsEqual(this.description, other.description)
				&& areAttributeDefListsEqual(this.attributeDefs, other.attributeDefs)
				&& areAttributeDefListsEqual(this.entryAttributeDefs, other.entryAttributeDefs)
//				&& CommonUtils.areStringListsEqual(this.entrykeys, other.entrykeys)
				);
		return equal;
	}

	private boolean areAttributeDefListsEqual(List<AttributeDef> currentAttrs, List<AttributeDef> otherAttrs) {
		boolean bRet = false;
		if (null == currentAttrs && null == otherAttrs) {
			bRet = true;
		}
		else if (null != otherAttrs && null != currentAttrs && 
				otherAttrs.size() == currentAttrs.size()) {
			boolean stillEqual = true;
			Iterator<AttributeDef> itr = otherAttrs.iterator();
			while (itr.hasNext()) {
				AttributeDef otherAttr = itr.next();
				if (!currentAttrs.contains(otherAttr)) {
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
}
