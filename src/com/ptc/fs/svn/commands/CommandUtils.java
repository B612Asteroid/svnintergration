package com.ptc.fs.svn.commands;
import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.mks.api.response.Field;
import com.mks.api.response.Item;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.ptc.fs.svn.data.def.AttributeDef;
import com.ptc.fs.svn.data.def.ChangePackageDef;
import com.ptc.fs.svn.utils.CommonUtils;
import com.ptc.fs.svn.utils.SVNLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class CommandUtils {
	private static Set<String> ootbCPAttrs = new HashSet<String>(6);
	static {
		ootbCPAttrs.add("id");
		ootbCPAttrs.add("summary");
		ootbCPAttrs.add("status");
		ootbCPAttrs.add("createddate");
		ootbCPAttrs.add("createdby");
		ootbCPAttrs.add("entrycount");
		ootbCPAttrs.add("type");
	}

	private static Object readFieldValueForType(Field currentField, String workItemId, String fieldInternalName) throws APIException {
		Object fieldValue = null;
		String fieldDataType = currentField.getDataType();
		if (null != fieldDataType) {
			if (!"com.mks.api.response.ItemList".equals(fieldDataType))
			{
				if ("com.mks.api.response.Item".equals(fieldDataType)) {
					Item item = currentField.getItem();
					fieldValue = item;
				}
				else if ("java.lang.Integer".equals(fieldDataType)) {
					fieldValue = currentField.getInteger();
				}
				else if ("java.lang.Boolean".equals(fieldDataType)) {
					fieldValue = currentField.getBoolean();
				}
				else if ("java.lang.Double".equals(fieldDataType)) {
					fieldValue = currentField.getDouble();
				}
				else if ("java.lang.Float".equals(fieldDataType)) {
					fieldValue = currentField.getFloat();
				}
				else if ("java.lang.Long".equals(fieldDataType)) {
					fieldValue = currentField.getLong();
				}
				else if ("java.util.Date".equals(fieldDataType)) {
					fieldValue = currentField.getDateTime();
				}
				else if ("java.lang.String".equals(fieldDataType)) {
					fieldValue = currentField.getString();
				}
				else if ("[B".equals(fieldDataType)) {
					try {
						fieldValue = currentField.getBytes();
					} catch (IOException e) {
						throw new APIException(e);
					} 
				} 
			}
		} else {
			StringBuffer sbuf = new StringBuffer();
			Formatter formatter = new Formatter(sbuf);
			formatter.format("*** Field %s is not defined for type %s. ***", new Object[] { fieldInternalName, workItemId });
			CommonUtils.outputAndLogMessage("WARNING", formatter.toString());
			formatter.close();
		} 
		return fieldValue;
	}

	private static boolean isCPAttribute(Item item) {
		return "im.ChangePackage.Attribute".equals(item.getModelType());
	}

	private static boolean isCPEntryAttribute(Item item) {
		return "im.ChangePackage.Entry.Attribute".equals(item.getModelType());
	}

	private static boolean isKey(Item item) {
		return "im.ChangePackage.Entry.Attribute".equals(item.getModelType());
	}

	public static ChangePackageDef getServerCPDefinition(Session session, String cpTypeName) {
		SVNLogger.logMessage("GENERAL", "Reading server changepackage definition for type - " + cpTypeName);

		ChangePackageDef changePackageDef = null;

		try {
			CmdRunner cmdRunner = session.createCmdRunner();
			List<String> selections = new ArrayList<>();
			selections.add(cpTypeName);
			Command command = CommandsFactory.viewCPTypeCommand(null, selections);

			Response response = cmdRunner.execute(command);
			List<ChangePackageDef> list = readDetailedChangePackageTypeResponse(response);
			changePackageDef = list.get(0);
		} catch (APIException e) {
			e.printStackTrace();
		} 
		return changePackageDef;
	}

	private static List<ChangePackageDef> readDetailedChangePackageTypeResponse(Response response) throws APIException {
		SVNLogger.logMessage("GENERAL", "Reading API response.");
		List<ChangePackageDef> cpList = new ArrayList<>();
		WorkItemIterator workItr = response.getWorkItems();
		while (workItr.hasNext()) {
			WorkItem workItem = workItr.next();
			String workItemId = workItem.getId();
			String workItemDisplayId = workItem.getDisplayId();
			ChangePackageDef serverChangePackageType = new ChangePackageDef();
			serverChangePackageType.setName(workItemId);
			serverChangePackageType.setDisplayName(workItemDisplayId);
			cpList.add(serverChangePackageType);
			Iterator<Field> fields = workItem.getFields();
			while (fields.hasNext()) {
				Field currentField = fields.next();
				String fieldInternalName = currentField.getName();
				Object fieldValue = null;
				if ("name".equals(fieldInternalName)) {
					fieldValue = readFieldValueForType(currentField, workItemId, fieldInternalName);

					serverChangePackageType.setName((String)fieldValue); continue;
				} 
				if ("displayName".equals(fieldInternalName)) {
					fieldValue = readFieldValueForType(currentField, workItemId, fieldInternalName);

					serverChangePackageType.setDisplayName((String)fieldValue); continue;
				} 
				if ("attributes".equals(fieldInternalName)) {
					List<AttributeDef> attributes = readChangePackageAttributes(currentField);
					serverChangePackageType.setAttributeDefs(attributes); continue;
				} 
				if ("entryAttributes".equals(fieldInternalName)) {
					List<AttributeDef> entryattributes = readChangePackageAttributes(currentField);
					if (null != entryattributes) {
						for (AttributeDef attr : entryattributes) {
							attr.setEntryAttribute(true);
						}
					}
					serverChangePackageType
					.setEntryAttributeDefs(entryattributes); continue;
				} 
				if ("entryKey".equals(fieldInternalName)) {
					serverChangePackageType.setEntrykeys(readEntryKeys(currentField));
				}
			} 
		} 
		return cpList;
	}

	private static List<AttributeDef> readChangePackageAttributes(Field currentField) {
		List<AttributeDef> attributes = new ArrayList<>();
		List list = currentField.getList();
		for (Object obj : list) {
			Item item = (Item)obj;
			if (isCPAttribute(item) || isCPEntryAttribute(item)) {
				AttributeDef attribute = new AttributeDef();
				attribute.setName(item.getId());
				Iterator<Field> itr = item.getFields();
				while (itr.hasNext()) {
					Field field = itr.next();
					String fieldName = field.getName();
					if ("mandatory".equals(fieldName)) {
						attribute.setMandatory(((Boolean)field.getValue()).booleanValue()); continue;
					} 
					if ("display".equals(fieldName)) {
						attribute.setDisplayName((String)field.getValue()); continue;
					} 
					if ("type".equals(fieldName)) {
						attribute.setDataType((String)field.getValue());
					}
				} 

				if (!ootbCPAttrs.contains(attribute.getName())) {
					attributes.add(attribute);
				}
			} 
		} 
		Collections.sort(attributes, (Comparator<? super AttributeDef>)new AttributeDef.AttributeDefComparator());
		if (0 == attributes.size()) {
			return null;
		}
		return attributes;
	}

	private static List<String> readEntryKeys(Field currentField) {
		List<String> retlist = new ArrayList<>();
		List list = currentField.getList();
		for (Object obj : list) {
			Item item = (Item)obj;
			if (isKey(item)) {
				retlist.add(item.getId());
			}
		} 
		return retlist;
	}
}
