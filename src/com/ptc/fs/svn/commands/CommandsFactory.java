package com.ptc.fs.svn.commands;

import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandsFactory
{
	public static Command createCPTypeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.CREATECPTYPE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command editCPTypeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.EDITCPTYPE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command deleteCPTypeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.DELETECPTYPE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command viewCPTypeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.VIEWCPTYPE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command createCPAttributeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.CREATECPATTRIBUTE);

		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command editCPAttributeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.EDITCPATTRIBUTE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command deleteCPAttributeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.DELETECPATTRIBUTE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command viewCPAttributeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.VIEWCPATTRIBUTE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command createCPEntryAttributeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.CREATECPENTRYATTRIBUTE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command editCPEntryAttributeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.EDITCPENTRYATTRIBUTE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command deleteCPEntryAttributeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.DELETECPENTRYATTRIBUTE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command viewCPEntryAttributeCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.VIEWCPENTRYATTRIBUTE);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command createCPCommand(List<Option> commandOptions) {
		Command command = new Command("im", CommandTokens.CREATECP);
		addCommandOptions(command, commandOptions);
		return command;
	}

	public static Command createCPEntryCommand(List<Option> commandOptions) {
		Command command = new Command("im", CommandTokens.CREATECPENTRY);
		addCommandOptions(command, commandOptions);
		return command;
	}

	public static Command editCPCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.EDITCP);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command deleteCPCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.DELETECP);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command viewCPCommand(List<Option> commandOptions, List<String> selection) {
		Command command = new Command("im", CommandTokens.VIEWCP);
		addCommandOptions(command, commandOptions);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command cpsViewCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.CPSVIEW);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command cpTypesCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.CPTYPES);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command cpAttributesCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.CPATTRIBUTES);
		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command cpEntryAttributesCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.CPENTRYATTRIBUTES);

		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	private static void addCommandParameters(Command command, Map<String, String> attributesMap) {
		if (null == command || null == attributesMap)
			return; 
		Iterator<String> itr = attributesMap.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String value = attributesMap.get(key);
			if (null != value) {
				command.addOption(new Option(key, value));
				continue;
			} 
			command.addOption(new Option(key));
		} 
	}

	private static void addCommandSelections(Command command, List<String> selections) {
		if (null == command || null == selections)
			return; 
		Iterator<String> itr = selections.iterator();
		while (itr.hasNext()) {
			String currentSelection = itr.next();
			command.addSelection(currentSelection);
		} 
	}

	private static void addCommandOptions(Command command, List<Option> commandOptions) {
		if (null == command || null == commandOptions)
			return; 
		Iterator<Option> itr = commandOptions.iterator();
		while (itr.hasNext()) {
			Option currentOption = itr.next();
			command.addOption(currentOption);
		} 
	}

	public static Command createCPEntryKeysCommand(Map<String, List<String>> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.EDITCPTYPE);
		Iterator<String> attributesMapItr = attributesMap.keySet().iterator();
		while (attributesMapItr.hasNext()) {
			String key = attributesMapItr.next();
			List<String> value = attributesMap.get(key);
			MultiValue entryKeysMultiValue = new MultiValue(",");
			Iterator<String> entryKeysListItr = value.iterator();
			while (entryKeysListItr.hasNext()) {
				String currentSelection = entryKeysListItr.next();
				entryKeysMultiValue.add(currentSelection);
			} 
			command.addOption(new Option(key, entryKeysMultiValue));
		} 
		addCommandSelections(command, selection);
		return command;
	}

	public static Command hasCapabilityCommand(Map<String, String> attributesMap, List<String> selection) {
		Command command = new Command("im", CommandTokens.HASCAPABILITY);

		addCommandParameters(command, attributesMap);
		addCommandSelections(command, selection);
		return command;
	}

	public static Command createCapabilityCommand(Map<String, String> attributesMap) {
		Command command = new Command("im", CommandTokens.CREATECAPABILITY);

		addCommandParameters(command, attributesMap);
		return command;
	}
}