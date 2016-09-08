package com.evolveum.midpoint.eclipse.ui.menus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.handlers.server.DownloadHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;

public class MenuUtil {

	public static void addUploadOrExecute(List<IContributionItem> items, IServiceLocator serviceLocator) {
		items.add(new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_UPLOAD_OR_EXECUTE, null, 
						null, null, null, 
						"Upload/execute", 
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

	public static void addReloadFromServer(List<IContributionItem> items, IServiceLocator serviceLocator) {
		List<IFile> files = getSelectedXmlFiles();
		if (files == null || files.size() == 0) {
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

	public static void addServerNameLabel(List<IContributionItem> items, IServiceLocator serviceLocator) {
		String serverName = PluginPreferences.getSelectedServerName();
		CommandContributionItem dummy = new CommandContributionItem( 
				new CommandContributionItemParameter(
						serviceLocator, null, PluginConstants.CMD_NOOP, null, 
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
		List<IFile> files = getSelectedXmlFiles();
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

	public static List<IFile> getSelectedXmlFiles() {
		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb == null) {
			return null;
		}
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win == null) {
			return null;
		}
		IWorkbenchPage page = win.getActivePage();
		if (page == null) {
			return null;
		}
		ISelection selection = page.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection ss = (IStructuredSelection) selection;
		if (ss.size() != 1) {
			return null;
		}
		List<IFile> files = SelectionUtils.getXmlFiles(ss);
		return files;
	}

}
