package com.evolveum.midpoint.eclipse.ui.menus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.handlers.server.DownloadHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;

public class MenuUtil {

	public static void addUploadOrExecute(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, FileRequestHandler.CMD_UPLOAD_OR_EXECUTE, null, 
						null, null, null, 
						"Upload/execute", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addUploadOrExecuteWithAction(List<IContributionItem> items, IServiceLocator serviceLocator) {
		int actionNumber = PluginPreferences.getActionAfterUpload();
		if (actionNumber != 0 && StringUtils.isNotBlank(PluginPreferences.getActionFile(actionNumber))) {
			Map<String,String> parameters = new HashMap<>();
			parameters.put(FileRequestHandler.PARAM_WITH_ACTION, "true");
			items.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
							serviceLocator, null, FileRequestHandler.CMD_UPLOAD_OR_EXECUTE, parameters, 
							null, null, null, 
							"Upload/execute with action", 
							null, null, CommandContributionItem.STYLE_PUSH, null, true)));
		}
	}

	public static void addDownload(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, DownloadHandler.CMD_DOWNLOAD, null, 
						null, null, null, 
						"Download", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addComputeDifferences(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, FileRequestHandler.CMD_COMPUTE_DIFFERENCE, null, 
						null, null, null, 
						"Compute differences", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addServerNameLabel(List<IContributionItem> items, IServiceLocator serviceLocator) {
		String serverName = PluginPreferences.getSelectedServerName();
		CommandContributionItem dummy = new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, FileRequestHandler.CMD_NOOP, null, 
						null, null, null, 
						"Selected midPoint server: " + serverName, 
						null, null, CommandContributionItem.STYLE_PUSH, null, false)) {
	
							@Override
							public boolean isEnabled() {
								return false;
							}
			
		};
		items.add(dummy);
	}
	
	public static void addExecuteAction(List<IContributionItem> items, IServiceLocator serviceLocator, int actionNumber) {
		if (StringUtils.isNotBlank(PluginPreferences.getActionFile(actionNumber))) {
			Map<String,String> parameters = new HashMap<>();
			parameters.put(FileRequestHandler.PARAM_ACTION_NUMBER, String.valueOf(actionNumber));
			items.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
							serviceLocator, null, FileRequestHandler.CMD_EXECUTE_ACTION, parameters, 
							null, null, null, 
							"Execute predefined action " + actionNumber, 
							null, null, CommandContributionItem.STYLE_PUSH, null, true)));
		}
	}

	public static void addTestConnections(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_TEST_CONNECTIONS, null, 
						null, null, null, 
						"Test connections to servers", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addShowConsole(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_CONSOLE, null, 
						null, null, null, 
						"Show plugin console", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addEditPreferences(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_EDIT_PREFERENCES, null, 
						null, null, null, 
						"Preferences...", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}


}
