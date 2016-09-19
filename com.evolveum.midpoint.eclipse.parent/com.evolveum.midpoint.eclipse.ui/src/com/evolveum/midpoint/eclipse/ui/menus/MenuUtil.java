package com.evolveum.midpoint.eclipse.ui.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
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
import com.evolveum.midpoint.eclipse.ui.handlers.ServerLogHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.server.DownloadHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.server.ServerRequestPack;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;

public class MenuUtil {

	public static void addUploadOrExecuteWithoutAction(List<IContributionItem> items, IServiceLocator serviceLocator) {
		CommandContributionItem cci = new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_UPLOAD_OR_EXECUTE, null, 
						null, null, null, 
						"Upload/execute (no after-action)", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true));
		items.add(cci);
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
							"&Upload/execute", 
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
						"Upload/execute (stop on &error)", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addBrowse(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_BROWSE, null, 
						null, null, null, 
						"&Browse server objects", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addDownload(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, DownloadHandler.CMD_DOWNLOAD, null, 
						null, null, null, 
						"&Bulk download of predefined objects", 
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
						"&Reload objects from server", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addComputeDifferences(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_COMPUTE_DIFFERENCE, null, 
						null, null, null, 
						"Compute &differences", 
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
		MenuUtil.addUploadOrExecuteStopOnError(dummyItems, serviceLocator);
		MenuUtil.addComputeDifferences(dummyItems, serviceLocator);
		MenuUtil.addReloadFromServer(dummyItems, serviceLocator);
		MenuUtil.addDownload(dummyItems, serviceLocator);
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}

	public static void addSetAsActionMenu(List<IContributionItem> items, IServiceLocator serviceLocator) {
		MenuManager dummy = new MenuManager("Set as action");
		List<IContributionItem> dummyItems = new ArrayList<>();
		MenuUtil.addSetAsAction(dummyItems, serviceLocator, 1);
		MenuUtil.addSetAsAction(dummyItems, serviceLocator, 2);
		MenuUtil.addSetAsAction(dummyItems, serviceLocator, 3);
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
		MenuManager dummy = new MenuManager("&Server-side actions");
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

	public static void addMiscMenu(List<IContributionItem> items, IServiceLocator serviceLocator) {
		MenuManager dummy = new MenuManager("&Miscellaneous");
		List<IContributionItem> dummyItems = new ArrayList<>();
		MenuUtil.addGenerateOid(dummyItems, serviceLocator);
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}

	public static void addGenerateOid(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_GENERATE_OID, null, 
						null, null, null, 
						"Generate random &OID", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
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
						"&Test connections to servers", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addShowConsole(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_CONSOLE, null, 
						null, null, null, 
						"Show plugin &console", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addEditPreferences(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_EDIT_PREFERENCES, null, 
						null, null, null, 
						"&Preferences...", 
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
						"Set as action &" + number, 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addServerLogMenu(List<IContributionItem> items, IServiceLocator serviceLocator) {
		if (!PluginPreferences.isServerSelected()) {
			return;
		}
		
		MenuManager dummy = new MenuManager("Server &log");
		List<IContributionItem> dummyItems = new ArrayList<>();
		MenuUtil.addShowLogInConsoleMenu(dummyItems, serviceLocator);
		MenuUtil.addShowLogInViewerMenu(dummyItems, serviceLocator);
		MenuUtil.addMarkCurrentPosition(dummyItems, serviceLocator);
		MenuUtil.addClearServerLog(dummyItems, serviceLocator);
		dummyItems.add(new Separator());
		MenuUtil.addModelLogMenu(dummyItems, serviceLocator);
		MenuUtil.addOtherLogMenu(dummyItems, serviceLocator, PluginConstants.VALUE_PROVISIONING, "Set Provisioning logging");
		MenuUtil.addOtherLogMenu(dummyItems, serviceLocator, PluginConstants.VALUE_REPOSITORY, "Set Repository logging");
		MenuUtil.addOtherLogMenu(dummyItems, serviceLocator, PluginConstants.VALUE_GUI, "Set GUI logging");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_ALL, PluginConstants.VALUE_INFO, "Set all of these to INFO");
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}
	
	public static void addModelLogMenu(List<IContributionItem> items, IServiceLocator serviceLocator) {
		MenuManager dummy = new MenuManager("Set Model logging");
		List<IContributionItem> dummyItems = new ArrayList<>();
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_INFO, "Set to INFO");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_CLOCKWORK_SUMMARY, "Set to 'clockwork summary' (Clockwork=DEBUG)");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_PROJECTOR_SUMMARY, "Set to 'projector summary' (previous + Projector=TRACE)");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_MAPPING_TRACE, "Set to 'mapping trace' (previous + Mapping=TRACE)");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_EXPRESSION_TRACE, "Set to 'expression trace' (previous + Expression=TRACE)");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_PROJECTOR_TRACE, "Set to 'projector trace' (previous + projector.*=TRACE)");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_LENS_TRACE, "Set to 'lens trace' (previous + lens.*=TRACE)");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_DEBUG, "Set to DEBUG (whole module)");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, PluginConstants.VALUE_MODEL, PluginConstants.VALUE_TRACE, "Set to TRACE (whole module)");
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}
	
	public static void addOtherLogMenu(List<IContributionItem> items, IServiceLocator serviceLocator, String module, String label) {
		MenuManager dummy = new MenuManager(label);
		List<IContributionItem> dummyItems = new ArrayList<>();
		MenuUtil.addLogEntry(dummyItems, serviceLocator, module, PluginConstants.VALUE_INFO, "Set to INFO");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, module, PluginConstants.VALUE_DEBUG, "Set to DEBUG");
		MenuUtil.addLogEntry(dummyItems, serviceLocator, module, PluginConstants.VALUE_TRACE, "Set to TRACE");
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}
	
	public static void addLogEntry(List<IContributionItem> items, IServiceLocator serviceLocator, String component, String level, String label) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_COMPONENT, component);
		parameters.put(PluginConstants.PARAM_LEVEL, level);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SET_LOG_LEVEL, parameters, 
						null, null, null, 
						label, 
						null, null, CommandContributionItem.STYLE_PUSH, null, false)));
	}

	public static void addShowLogInConsoleMenu(List<IContributionItem> items, IServiceLocator serviceLocator) {
		MenuManager dummy = new MenuManager("Show log in &console");
		List<IContributionItem> dummyItems = new ArrayList<>();
		MenuUtil.addShowLogInConsoleFromStart(dummyItems, serviceLocator);
		MenuUtil.addShowLogInConsoleBackN(dummyItems, serviceLocator);
		MenuUtil.addShowLogInConsoleFromMark(dummyItems, serviceLocator);
		MenuUtil.addShowLogInConsoleFromNow(dummyItems, serviceLocator);
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}

	public static void addShowLogInViewerMenu(List<IContributionItem> items, IServiceLocator serviceLocator) {
		MenuManager dummy = new MenuManager("Show log in &viewer");
		List<IContributionItem> dummyItems = new ArrayList<>();
		MenuUtil.addShowLogInViewerFromStart(dummyItems, serviceLocator);
		MenuUtil.addShowLogInViewerBackN(dummyItems, serviceLocator);
		MenuUtil.addShowLogInViewerFromMark(dummyItems, serviceLocator);
		MenuUtil.addShowLogInViewerFromConsole(dummyItems, serviceLocator);
		for (IContributionItem item : dummyItems) {
			dummy.add(item);
		}
		items.add(dummy);
	}

	public static void addMarkCurrentPosition(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_MARK_CURRENT_LOG_POSITION, null, 
						null, null, null, 
						"&Mark current log position", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addShowLogInConsoleFromStart(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_FROM, PluginConstants.VALUE_START);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_LOG_IN_CONSOLE, parameters, 
						null, null, null, 
						"Show &whole log", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}
	
	public static void addShowLogInConsoleBackN(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_FROM, PluginConstants.VALUE_BACK_N);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_LOG_IN_CONSOLE, parameters, 
						null, null, null, 
						"Show &last " + PluginPreferences.getLogGoBackN() + " KB", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addShowLogInConsoleFromMark(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_FROM, PluginConstants.VALUE_MARK);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_LOG_IN_CONSOLE, parameters, 
						null, null, null, 
						"Show from &mark", 
						null, null, CommandContributionItem.STYLE_PUSH, null, false)) {
			@Override
			public boolean isEnabled() {
				return ServerLogHandler.getCurrentMark() != null;
			}
		});
	}
	
	public static void addShowLogInConsoleFromNow(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_FROM, PluginConstants.VALUE_NOW);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_LOG_IN_CONSOLE, parameters, 
						null, null, null, 
						"Show from &now", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addShowLogInViewerFromStart(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_FROM, PluginConstants.VALUE_START);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_LOG_IN_EDITOR, parameters, 
						null, null, null, 
						"Show &whole log", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}
	
	public static void addShowLogInViewerBackN(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_FROM, PluginConstants.VALUE_BACK_N);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_LOG_IN_EDITOR, parameters, 
						null, null, null, 
						"Show &last " + PluginPreferences.getLogGoBackN() + " KB", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}


	public static void addShowLogInViewerFromMark(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_FROM, PluginConstants.VALUE_MARK);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_LOG_IN_EDITOR, parameters, 
						null, null, null, 
						"Show from &mark", 
						null, null, CommandContributionItem.STYLE_PUSH, null, false)) {
							@Override
							public boolean isEnabled() {
								return ServerLogHandler.getCurrentMark() != null;
							}
		});
	}
	
	public static void addShowLogInViewerFromConsole(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		parameters.put(PluginConstants.PARAM_FROM, PluginConstants.VALUE_CONSOLE);
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_SHOW_LOG_IN_EDITOR, parameters, 
						null, null, null, 
						"Copy from &console", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

	public static void addClearServerLog(List<IContributionItem> items, IServiceLocator serviceLocator) {
		Map<String,String> parameters = new HashMap<>();
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_CLEAR_SERVER_LOG, parameters, 
						null, null, null, 
						"Cl&ear server log", 
						null, null, CommandContributionItem.STYLE_PUSH, null, true)));
	}

}
