package com.evolveum.midpoint.eclipse.ui.prefs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import com.evolveum.midpoint.eclipse.runtime.api.ConnectionParameters;
import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class PluginPreferences {

	public static final String PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.midPoint";
	public static final String ACTIONS_PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.actions";

	public static ConnectionParameters getConnectionParameters() {
		IPreferenceStore store = store();
		String url = store.getString(MidPointPreferencePage.MIDPOINT_URL);
		String login = store.getString(MidPointPreferencePage.MIDPOINT_LOGIN);
		String password = store.getString(MidPointPreferencePage.MIDPOINT_PASSWORD);
		return new ConnectionParameters(url, login, password);
	}
	
	public static String getActionFile(String number) {
		IPreferenceStore store = store();
		return store.getString(ActionsPreferencePage.ACTION_FILE_PREFIX + number);
	}
	
	public static String getActionOpenAfter(String number) {
		IPreferenceStore store = store();
		return store.getString(ActionsPreferencePage.ACTION_OPEN_AFTER_PREFIX + number);
	}

	public static String getActionOpenAfterOther() {
		IPreferenceStore store = store();
		return store.getString(ActionsPreferencePage.ACTION_OPEN_AFTER_OTHER);
	}


	public static String getActionAfterUpload() {
		IPreferenceStore store = store();
		return store.getString(ActionsPreferencePage.ACTION_AFTER_UPLOAD);
	}

	public static String getLogfile() {
		IPreferenceStore store = store();
		return store.getString(MidPointPreferencePage.MIDPOINT_LOGFILE);
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
	

	public static String getDownloadedFileNamePattern() {
		return store().getString(DownloadPreferencePage.DOWNLOADED_FILE_NAME_PATTERN);
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

	private static IPreferenceStore store() {
		return EclipseActivator.getInstance().getPreferenceStore();
	}
	
	public static String getString(String key) {
		return store().getString(key);
	}

}
