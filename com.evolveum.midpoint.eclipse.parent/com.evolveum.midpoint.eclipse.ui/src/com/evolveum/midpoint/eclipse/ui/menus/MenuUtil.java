package com.evolveum.midpoint.eclipse.ui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.services.IServiceLocator;

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.handlers.server.DownloadHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.server.ServerRequestPack;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;

public class MenuUtil {

	public static void addUploadOrExecuteWithoutAction(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_UPLOAD_OR_EXECUTE, null, 
						null, null, null, 
						"Upload/execute (no after-action)", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addUploadOrExecuteWithAction(List<IContributionItem> items, IServiceLocator serviceLocator) {
		int actionNumber = PluginPreferences.getActionAfterUpload();
		if (actionNumber != 0 && StringUtils.isNotBlank(PluginPreferences.getActionFile(actionNumber))) {
			Map<String,String> parameters = new HashMap<>();
			parameters.put(PluginConstants.PARAM_WITH_ACTION, "true");
			items.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
							serviceLocator, null, PluginConstants.CMD_UPLOAD_OR_EXECUTE, parameters, 
							null, null, null, 
							"Upload/execute", 
							null, null, CommandContributionItem.STYLE_PUSH, null, true)));
		}
	}

	public static void addUploadOrExecuteStopOnError(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_STOP_ON_ERROR, "true");
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_UPLOAD_OR_EXECUTE, parameters, 
						null, null, null, 
						"Upload/execute (stop on error)", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addBrowse(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_BROWSE, null, 
						null, null, null, 
						"Browse server objects", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addDownload(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, DownloadHandler.CMD_DOWNLOAD, null, 
						null, null, null, 
						"Bulk download of predefined objects", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addReloadFromServer(List<IContributionItem> items, IServiceLocator serviceLocator) {
		List<IFile> files = SelectionUtils.getSelectedXmlFiles(SelectionUtils.getWorkbenchSelection());
		if ((files == null || files.size() == 0) ) {
			return;
		}
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_RELOAD_FROM_SERVER, null, 
						null, null, null, 
						"Reload objects from server", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addComputeDifferences(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_COMPUTE_DIFFERENCE, null, 
						null, null, null, 
						"Compute differences", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}
	
//	public static void addEnableOnServer(List<IContributionItem> items, IServiceLocator serviceLocator) {
//		items.add(new CommandContributionItem( 
//				new CommandContributionItemParameter(
//						serviceLocator, null, PluginConstants.CMD_ENABLE_ON_SERVER, null, 
//						null, null, null, 
//						"Enable", 
//						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
//	}
//
//	public static void addDisableOnServer(List<IContributionItem> items, IServiceLocator serviceLocator) {
//		items.add(new CommandContributionItem( 
//				new CommandContributionItemParameter(
//						serviceLocator, null, PluginConstants.CMD_DISABLE_ON_SERVER, null, 
//						null, null, null, 
//						"Disable", 
//						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
//	}
	
	public static void addDeleteOnServerRaw(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_RAW, "true");
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_DELETE_ON_SERVER, parameters, 
						null, null, null, 
						"Delete (raw)", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addDeleteOnServerNonRaw(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_DELETE_ON_SERVER, null, 
						null, null, null, 
						"Delete (non-raw)", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}
	
	public static void addDeleteOnServerRawAndLocally(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_RAW, "true");
		parameters.put(PluginConstants.PARAM_ALSO_LOCALLY, "true");
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_DELETE_ON_SERVER, parameters, 
						null, null, null, 
						"Delete on server (raw) and locally", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addRecomputeOnServer(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_RECOMPUTE_ON_SERVER, parameters, 
						null, null, null, 
						"Recompute", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addRecomputeOnServerWithUpload(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_UPLOAD_FIRST, "true");
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_RECOMPUTE_ON_SERVER, parameters, 
						null, null, null, 
						"Upload and recompute", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}
	
	public static void addTestResourceOnServer(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_TEST_RESOURCE, null, 
						null, null, null, 
						"Test resource", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addTestResourceOnServerWithUpload(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_UPLOAD_FIRST, "true");
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_TEST_RESOURCE, parameters, 
						null, null, null, 
						"Upload and test resource", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addServerNameLabel(List<IContributionItem> items, IServiceLocator serviceLocator) {
		String serverName = PluginPreferences.getSelectedServerName();
		if (serverName == null) {
			serverName = "(none)";
		}
		MenuManager dummy = new MenuManager("Selected midPoint server: " + serverName);
		int index = 0;
		for (ServerInfo serverInfo : PluginPreferences.getServers()) {
			Map<String,String> parameters = new HashMap<>();
			parameters.put(PluginConstants.PARAM_SERVER_NUMBER, String.valueOf(index++));
			dummy.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SELECT_SERVER, parameters, 
						null, null, null, 
						"Select server " + serverInfo.getDisplayName() + (serverInfo.isSelected() ? " (selected)" : ""), 
						null, null, CommandContributionItem.STYLE_PUSH, null, false)) {
							@Override
							public boolean isEnabled() {
								return !serverInfo.isSelected();
							}
			});
		}
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_SERVER_NUMBER, "");
		dummy.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
					serviceLocator, null, PluginConstants.CMD_SELECT_SERVER, null, 
					null, null, null, 
					"Select none", 
					null, null, CommandContributionItem.STYLE_PUSH, null, true)));
		items.add(dummy);
	}

	public static void addTransferMenu(List<IContributionItem> items, IServiceLocator serviceLocator) {
		String serverName = PluginPreferences.getSelectedServerName();
		if (serverName == null) {
			return;
		}
		MenuManager dummy = new MenuManager("Transfer-related actions");
		List<IContributionItem> dummyItems = new ArrayList<>();
		MenuUtil.addUploadOrExecuteWithAction(dummyItems, serviceLocator);
		MenuUtil.addUploadOrExecuteWithoutAction(dummyItems, serviceLocator);
		MenuUtil.addUploadOrExecuteStopOnError(items, serviceLocator);
		MenuUtil.addComputeDifferences(dummyItems, serviceLocator);
		MenuUtil.addReloadFromServer(dummyItems, serviceLocator);
		MenuUtil.addDownload(dummyItems, serviceLocator);
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}

	public static void addServerSideMenu(List<IContributionItem> items, IServiceLocator serviceLocator) {
		String serverName = PluginPreferences.getSelectedServerName();
		if (serverName == null) {
			return;
		}
		MenuManager dummy = new MenuManager("Server-side actions");
		List<IContributionItem> dummyItems = new ArrayList<>();
//		MenuUtil.addEnableOnServer(dummyItems, serviceLocator);
//		MenuUtil.addDisableOnServer(dummyItems, serviceLocator);
		MenuUtil.addRecomputeOnServer(dummyItems, serviceLocator);
		MenuUtil.addRecomputeOnServerWithUpload(dummyItems, serviceLocator);
		MenuUtil.addTestResourceOnServer(dummyItems, serviceLocator);
		MenuUtil.addTestResourceOnServerWithUpload(dummyItems, serviceLocator);
		MenuUtil.addDeleteOnServerRaw(dummyItems, serviceLocator);
		MenuUtil.addDeleteOnServerNonRaw(dummyItems, serviceLocator);
		MenuUtil.addDeleteOnServerRawAndLocally(dummyItems, serviceLocator);
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}

	public static void addExecuteAction(List<IContributionItem> items, IServiceLocator serviceLocator, int actionNumber) {
		if (StringUtils.isNotBlank(PluginPreferences.getActionFile(actionNumber))) {
			Map<String,String> parameters = new HashMap<>();
			parameters.put(PluginConstants.PARAM_ACTION_NUMBER, String.valueOf(actionNumber));
			items.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
							serviceLocator, null, PluginConstants.CMD_EXECUTE_ACTION, parameters, 
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

	public static void addSetAsAction(List<IContributionItem> items, IServiceLocator serviceLocator, int number) {
		List<IFile> files = SelectionUtils.getSelectedXmlFiles(SelectionUtils.getWorkbenchSelection());
		if (files == null || files.size() != 1) {
			return;
		}
		   
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_ACTION_NUMBER, String.valueOf(number));
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SET_AS_ACTION, parameters, 
						null, null, null, 
						"Set as action " + number, 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}
	

}
