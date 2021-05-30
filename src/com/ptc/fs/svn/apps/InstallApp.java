package com.ptc.fs.svn.apps;

import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.mks.api.response.ItemAlreadyExistsException;
import com.mks.api.response.Response;
import com.ptc.fs.svn.commands.CommandUtils;
import com.ptc.fs.svn.commands.CommandsFactory;
import com.ptc.fs.svn.connections.ConnectionManager;
import com.ptc.fs.svn.data.def.AttributeDef;
import com.ptc.fs.svn.data.def.ChangePackageDef;
import com.ptc.fs.svn.utils.CommonUtils;
import com.ptc.fs.svn.utils.SVNAppTokens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InstallApp
extends BaseApp
{
	static {
		configureLogger("InstallApp");
	}

	public static void main(String[] args) {
		Session session = null;
		try {
			session = ConnectionManager.connectAPI();
			if (session != null) {
				createCompleteCPDef(session);
			}
		} catch (Exception ex) {
			CommonUtils.logException("ERROR", ex);
		} finally {
			if (session != null) {
				session.getIntegrationPoint().release();
			}
		} 
	}

	private static boolean validateCPAttributes(Session session, ChangePackageDef integrationDefinition, ChangePackageDef serverCPDefinition) {
		boolean unexpectedMandatoryFound = false;
		List<AttributeDef> alienAttributes = new ArrayList<>();

		List<AttributeDef> integrationCPAttrs = integrationDefinition.getAttributeDefs();

		List<AttributeDef> serverCPAttrs = serverCPDefinition.getAttributeDefs();
		Iterator<AttributeDef> itr = serverCPAttrs.iterator();
		while (itr.hasNext()) {
			AttributeDef def = itr.next();
			if (!integrationCPAttrs.contains(def)) {
				alienAttributes.add(def);
				if (def.isMandatory()) {
					unexpectedMandatoryFound = true;
					CommonUtils.outputAndLogMessage("ERROR", "Unexpected \"mandatory\" CP attribute found. - " + def.getName());
				} 
			} 
		} 
		return unexpectedMandatoryFound;
	}

	private static boolean validateCPEntryAttributes(Session session, ChangePackageDef integrationDefinition, ChangePackageDef serverCPDefinition) {
		boolean unexpectedMandatoryFound = false;
		List<AttributeDef> alienCPEntryAttributes = new ArrayList<>();


		List<AttributeDef> integrationCPEntryAttrs = integrationDefinition.getEntryAttributeDefs();

		List<AttributeDef> serverCPEntryAttrs = serverCPDefinition.getEntryAttributeDefs();

		Iterator<AttributeDef> itr = serverCPEntryAttrs.iterator();
		while (itr.hasNext()) {
			AttributeDef def = itr.next();
			if (!integrationCPEntryAttrs.contains(def)) {
				alienCPEntryAttributes.add(def);
				if (def.isMandatory()) {
					unexpectedMandatoryFound = true;
					CommonUtils.outputAndLogMessage("ERROR", "Unexpected \"mandatory\" CPEntry attribute found. - " + def.getName());
				} 
			} 
		} 
		return unexpectedMandatoryFound;
	}

	private static void performValidation(Session session, ChangePackageDef expectedDefinition) throws Exception {
		ChangePackageDef serverCPDefinition = CommandUtils.getServerCPDefinition(session, SVNAppTokens.SVN_CPTYPE);
		boolean mandatoryCPAttrFound = validateCPAttributes(session, expectedDefinition, serverCPDefinition);

		boolean mandatoryCPEntryAttrFound = validateCPEntryAttributes(session, expectedDefinition, serverCPDefinition);


		if (mandatoryCPAttrFound || mandatoryCPEntryAttrFound) {
			throw new Exception("Unexpected \"mandatory\" Attribute found. Unsupported scenario. Aborting installation.");
		}
	}

	private static void setStatusAttribute(Session session) throws APIException {
		if (null == session) {
			throw new NullPointerException();
		}
		CmdRunner cmdRunner = session.createCmdRunner();
		Map<String, String> attributesMap = new HashMap<>();
		attributesMap.put("cpType", SVNAppTokens.SVN_CPTYPE);
		attributesMap.put("strings", SVNAppTokens.SVN_STATUS_VALUES);

		String status = "status";
		List<String> selectionList = new ArrayList<>();
		selectionList.add(status);

		Command editCPAttrCommand = CommandsFactory.editCPAttributeCommand(attributesMap, selectionList);

		Response response = cmdRunner.execute(editCPAttrCommand);
		response.getExitCode();

		CommonUtils.outputAndLogMessage("GENERAL", "Existing Attribute updated : status");
	}

	private static void createCompleteCPDef(Session session) throws Exception {
		ChangePackageDef toBeCreatedDefinition = CommonUtils.getExpectedCPDef();

		try {
			createCPType(session, toBeCreatedDefinition.getName(), toBeCreatedDefinition
					.getDisplayName(), toBeCreatedDefinition.getDescription());
		} catch (ItemAlreadyExistsException e) {
			performValidation(session, toBeCreatedDefinition);
		} 

		setStatusAttribute(session);

		List<AttributeDef> attributeDefs = toBeCreatedDefinition.getAttributeDefs();
		if (null != attributeDefs) {
			for (AttributeDef attrDef : attributeDefs) {
				try {
					CommonUtils.outputAndLogMessage("GENERAL", "Creating :: Change Package Attribute : " + attrDef.getName());
					createAttributeDef(session, attrDef.getName(), attrDef
							.getDisplayName(), attrDef.getDataType(), attrDef
							.getChangePackageTypeName(), attrDef
							.getPosition(), Boolean.valueOf(attrDef.isMandatory()), Boolean.FALSE);
				}
				catch (APIException e) {
					CommonUtils.outputAndLogMessage("WARNING", e.getMessage());
				} 
			} 
		}

		List<AttributeDef> entryAttributeDefs = toBeCreatedDefinition.getEntryAttributeDefs();
		if (null != entryAttributeDefs) {
			for (AttributeDef attrDef : entryAttributeDefs) {
				try {
					CommonUtils.outputAndLogMessage("GENERAL", "Creating :: Change Package Entry Attribute : " + attrDef.getName());
					createAttributeDef(session, attrDef.getName(), attrDef
							.getDisplayName(), attrDef.getDataType(), attrDef
							.getChangePackageTypeName(), attrDef
							.getPosition(), Boolean.valueOf(attrDef.isMandatory()), Boolean.TRUE);
				}
				catch (APIException e) {
					CommonUtils.logException("WARNING", (Exception)e);
				} 
			} 
		}

		List<String> entryAttributeKeys = toBeCreatedDefinition.getEntrykeys();
		createEntryAttributeKeys(session, entryAttributeKeys);
		createCapability(session, capabilityName);
	}

	private static boolean createCPType(Session session, String typeName, String displayName, String description) throws APIException, ItemAlreadyExistsException {
		boolean created = false;
		if (null == session)
			throw new NullPointerException(); 
		CmdRunner cmdRunner = session.createCmdRunner();
		Map<String, String> attributesMap = new HashMap<>();
		attributesMap.put("name", typeName);
		attributesMap.put("displayName", displayName);
		attributesMap.put("description", description);
		Command createCPCommand = CommandsFactory.createCPTypeCommand(attributesMap, null);

		createCPCommand.addOption(new Option("permittedGroups", "everyone:modify"));
		createCPCommand.addOption(new Option("permittedAdministrators", "g=admins"));

		try {
			Response response = cmdRunner.execute(createCPCommand);
			response.getExitCode();
			created = true;
			CommonUtils.outputAndLogMessage("GENERAL", "Change Package Type Created : " + typeName);
		}
		catch (ItemAlreadyExistsException e) {

			CommonUtils.logException("WARNING", (Exception)e);
			throw e;
		} 
		return created;
	}

	private static boolean createAttributeDef(Session session, String attrName, String attrDisplayName, String dataType, String parentCPType, Integer position, Boolean isMandatory, Boolean isEntryAttr) throws APIException {
		boolean created = false;
		if (null == session)
			throw new NullPointerException(); 
		CmdRunner cmdRunner = session.createCmdRunner();
		Map<String, String> attributesMap = new HashMap<>();
		attributesMap.put("name", attrName);
		attributesMap.put("displayName", attrDisplayName);
		attributesMap.put("dataType", dataType);
		attributesMap.put("cpType", parentCPType);
		if (isMandatory.booleanValue())
			attributesMap.put("mandatory", null); 
		attributesMap.put("position", position.toString());

		try {
			Command attrCommand = null;
			if (isEntryAttr.booleanValue()) {
				attrCommand = CommandsFactory.createCPEntryAttributeCommand(attributesMap, null);
			} else {

				attrCommand = CommandsFactory.createCPAttributeCommand(attributesMap, null);
			} 
			Response response = cmdRunner.execute(attrCommand);
			response.getExitCode();

			created = true;
			String message = "Attribute Created :" + attrName + ", pos = " + position;
			if (isEntryAttr.booleanValue()) {
				message = "CPEntry Attribute Created :" + attrName + ", pos = " + position;
			}
			CommonUtils.outputAndLogMessage("GENERAL", message);
		} catch (ItemAlreadyExistsException e) {

			CommonUtils.outputAndLogMessage("WARNING", "Attribute already existed : " + attrName);
		} 

		return created;
	}

	private static boolean createEntryAttributeKeys(Session session, List<String> entryAttrKeysList) throws APIException {
		if (null == entryAttrKeysList)
			return false; 
		if (null == session)
			throw new NullPointerException(); 
		boolean created = false;
		CmdRunner cmdRunner = session.createCmdRunner();
		Map<String, List<String>> entryAttributeKeysMap = new HashMap<>();
		entryAttributeKeysMap.put("entryKey", entryAttrKeysList);

		List<String> selectionList = new ArrayList<>();
		selectionList.add(SVNAppTokens.SVN_CPTYPE);

		Command createCPEntryKeysCommand = CommandsFactory.createCPEntryKeysCommand(entryAttributeKeysMap, selectionList);

		try {
			Response response = cmdRunner.execute(createCPEntryKeysCommand);
			response.getExitCode();
			created = true;
		} catch (ItemAlreadyExistsException e) {
			CommonUtils.logException("WARNING", (Exception)e);
		} 
		return created;
	}

	private static boolean createCapability(Session session, String capabilityName) throws APIException {
		CommonUtils.outputAndLogMessage("GENERAL", "Creating :: Capability : " + capabilityName);

		if (null == session)
			throw new NullPointerException(); 
		boolean created = false;
		CmdRunner cmdRunner = session.createCmdRunner();
		Map<String, String> attributesMap = new HashMap<>();
		attributesMap.put("application", SVNAppTokens.SVN_CPTYPE);

		attributesMap.put("name", capabilityName);
		attributesMap.put("description", capabilityDescription);


		Command createCapabilityCommand = CommandsFactory.createCapabilityCommand(attributesMap);
		try {
			Response response = cmdRunner.execute(createCapabilityCommand);
			response.getExitCode();
			CommonUtils.outputAndLogMessage("GENERAL", "Capability created : " + capabilityName);

			created = true;
		} catch (ItemAlreadyExistsException e) {
			CommonUtils.outputAndLogMessage("WARNING", "Capability already existed : " + capabilityName);
		} 

		return created;
	}
}