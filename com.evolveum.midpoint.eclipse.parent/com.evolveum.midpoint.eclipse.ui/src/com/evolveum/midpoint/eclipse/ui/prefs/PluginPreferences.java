package com.evolveum.midpoint.eclipse.ui.prefs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.handlers.TestConnectionHandler;
import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class PluginPreferences {

	public static final String PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.midPoint";
	public static final String ACTIONS_PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.actions";
	public static final String DONWLOAD_PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.download";
	public static final String COMPARE_PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.compare";
	public static final String MISC_PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.misc";

	public static final String GEN_COUNTER = "generationCounter";
	public static final String EXEC_COUNTER = "executionCounter";
	
	public static ConnectionParameters getConnectionParameters() {
		ServerInfo s = getSelectedServer();
		if (s == null) {
			return new ConnectionParameters("", "", "", "");			// TODO...
		} else {
			return new ConnectionParameters(s.getName(), s.getUrl(), s.getLogin(), s.getPassword());
		}
	}
	
	public static ServerInfo getSelectedServer() {
		List<ServerInfo> servers = getServers();
		for (ServerInfo server : servers) {
			if (server.isSelected()) {
				return server;
			}
		}
		return null;
	}

	public static List<ServerInfo> getServers() {
		return ServersCache.getInstance().getServers();
	}
	
	public static void setServers(List<ServerInfo> servers) {
		store().setValue(MidPointPreferencePage.SERVERS, ServerInfo.toXml(servers));
	}

	public static String getActionFile(int number) {
		IPreferenceStore store = store();
		return store.getString(ActionsPreferencePage.ACTION_FILE_PREFIX + number);
	}
	
	public static String getActionOpenAfter(int number) {
		IPreferenceStore store = store();
		return store.getString(ActionsPreferencePage.ACTION_OPEN_AFTER_PREFIX + number);
	}

	public static String getActionOpenAfterOther() {
		IPreferenceStore store = store();
		return store.getString(ActionsPreferencePage.ACTION_OPEN_AFTER_OTHER);
	}

	public static int getActionAfterUpload() {
		return store().getInt(ActionsPreferencePage.ACTION_AFTER_UPLOAD);
	}

	public static String getLogfile() {
		ServerInfo s = getSelectedServer();
		return s != null ? s.getLogFile() : null;
	}
	
	public static boolean isUseMidPointLogViewer() {
		return store().getBoolean(ActionsPreferencePage.USE_MIDPOINT_LOG_VIEWER);
	}
	
	public static String getActionOutputFileNamePattern() {
		return store().getString(ActionsPreferencePage.ACTION_OUTPUT_FILE_NAME_PATTERN);
	}
	
	public static String getActionOutputRootDirectory() {
		return store().getString(ActionsPreferencePage.ACTION_OUTPUT_ROOT_DIRECTORY);
	}

	public static String getOutputFileNamePatternNoSource() {
		return store().getString(ActionsPreferencePage.ACTION_OUTPUT_FILE_NAME_PATTERN_NO_SOURCE);
	}
	
	public static String getShowUploadOrExecuteResultMessageBox() {
		return store().getString(MidPointPreferencePage.SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX);
	}

	public static String getShowComparisonResultMessageBox() {
		return store().getString(MidPointPreferencePage.SHOW_COMPARISON_RESULT_MESSAGE_BOX);
	}

	public static String getGeneratedFileNamePattern() {
		return store().getString(MiscPreferencePage.GENERATED_FILE_NAME_PATTERN);
	}

	public static String getDownloadedFileNamePattern() {
		return store().getString(DownloadPreferencePage.DOWNLOADED_FILE_NAME_PATTERN);
	}
	
	public static String getDownloadedFilesRootDirectory() {
		return store().getString(DownloadPreferencePage.DOWNLOADED_FILES_ROOT_DIRECTORY);
	}

	public static int getDownloadedObjectsLimit() {
		return store().getInt(DownloadPreferencePage.DOWNLOADED_OBJECTS_LIMIT);
	}
	
	public static List<String> getIncludeInDownload() {
		return split(store().getString(DownloadPreferencePage.INCLUDE_IN_DOWNLOAD));
	}
	
	public static List<String> getExcludeFromDownload() {
		return split(store().getString(DownloadPreferencePage.EXCLUDE_FROM_DOWNLOAD));
	}
	
	public static String getOverwriteWhenDownloading() {
		return store().getString(DownloadPreferencePage.OVERWRITE_WHEN_DOWNLOADING);
	}

	public static String getCompareResultFileNamePattern() {
		return store().getString(ComparePreferencePage.COMPARE_RESULT_FILE_NAME_PATTERN);
	}

	public static String getCompareResultRootDirectory() {
		return store().getString(ComparePreferencePage.COMPARE_RESULT_ROOT_DIRECTORY);
	}

	public static boolean getCompareShowLocalToRemote() {
		return store().getBoolean(ComparePreferencePage.COMPARE_SHOW_LOCAL_TO_REMOTE);
	}

	public static boolean getCompareShowRemoteToLocal() {
		return store().getBoolean(ComparePreferencePage.COMPARE_SHOW_REMOTE_TO_LOCAL);
	}

	public static boolean getCompareShowLocalNormalized() {
		return store().getBoolean(ComparePreferencePage.COMPARE_SHOW_LOCAL_NORMALIZED);
	}
	
	public static boolean getCompareShowRemote() {
		return store().getBoolean(ComparePreferencePage.COMPARE_SHOW_REMOTE);
	}

	public static List<String> getCompareIgnoreItems() {
		String aggregated = store().getString(ComparePreferencePage.COMPARE_OTHER_ITEMS_TO_IGNORE);
		List<String> rv = split(aggregated);
		if (store().getBoolean(ComparePreferencePage.COMPARE_IGNORE_METADATA)) {
			rv.add("metadata");
		}
		return rv;
	}

	private static List<String> split(String aggregated) {
		String[] parts = StringUtils.split(aggregated, ",");
		List<String> rv = new ArrayList<>();
		for (String part : parts) {
			part = part.trim();
			rv.add(part);
		}
		return rv;
	}

	public static IPreferenceStore store() {
		return EclipseActivator.getInstance().getPreferenceStore();
	}
	
	public static String getString(String key) {
		return store().getString(key);
	}
	
	
	public static void testConnection(String name, String url, String login, String password) {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		
		try {
			Command command = commandService.getCommand(PluginConstants.CMD_TEST_CONNECTION);
			Parameterization[] params = new Parameterization[] { 
					new Parameterization(command.getParameter(TestConnectionHandler.PARAM_SERVER_NAME), name),				
					new Parameterization(command.getParameter(TestConnectionHandler.PARAM_SERVER_URL), url),
					new Parameterization(command.getParameter(TestConnectionHandler.PARAM_LOGIN), login),
					new Parameterization(command.getParameter(TestConnectionHandler.PARAM_PASSWORD), password),				
					};
			ParameterizedCommand parametrizedCommand = new ParameterizedCommand(command, params);
			handlerService.executeCommand(parametrizedCommand, null);
		} catch (CommandException e) {
			Util.showAndLogError("Error", "Couldn't execute Test command: " + e);
		}
	}
	
	public static boolean isServerSelected() {
		return getSelectedServer() != null;
	}

	public static String getSelectedServerName() {
		ServerInfo s = getSelectedServer();
		if (s == null) {
			return null;
		} else if (StringUtils.isNotBlank(s.getName())) {
			return s.getName();
		} else if (StringUtils.isNotBlank(s.getUrl())) {
			return s.getUrl();
		} else {
			return "(unnamed)";
		}
	}

	public static String getSelectedServerShortName() {
		ServerInfo s = getSelectedServer();
		if (s == null) {
			return null;
		} else if (StringUtils.isNotBlank(s.getShortName())) {
			return s.getShortName();
		} else if (StringUtils.isNotBlank(s.getName())) {
			return s.getName();
		} else {
			return "unnamed";
		}
	}

	public static void setActionFile(int actionNumber, String path) {
		store().setValue(ActionsPreferencePage.ACTION_FILE_PREFIX + actionNumber, path);
	}

	public static int getAndIncrementGenCounter() {
		int c = store().getInt(GEN_COUNTER);
		store().setValue(GEN_COUNTER, c+1);
		return c;
	}

	public static int getAndIncrementExecCounter() {
		int c = store().getInt(EXEC_COUNTER);
		store().setValue(EXEC_COUNTER, c+1);
		return c;
	}
	
	public static int getExecCounter() {
		return store().getInt(EXEC_COUNTER);
	}

	public static void setExecCounter(int value) {
		store().setValue(EXEC_COUNTER, value);
	}

}
