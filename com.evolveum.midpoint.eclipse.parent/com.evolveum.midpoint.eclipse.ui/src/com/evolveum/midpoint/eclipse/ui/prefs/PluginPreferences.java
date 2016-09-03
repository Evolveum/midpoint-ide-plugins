package com.evolveum.midpoint.eclipse.ui.prefs;

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
	
	public static String getOutputFileNamePattern() {
		return store().getString(ActionsPreferencePage.OUTPUT_FILE_NAME_PATTERN);
	}

	public static String getOutputFileNamePatternNoSource() {
		return store().getString(ActionsPreferencePage.OUTPUT_FILE_NAME_PATTERN_NO_SOURCE);
	}
	
	public static String getShowUploadOrExecuteResultMessageBox() {
		return store().getString(MidPointPreferencePage.SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX);
	}
	
	public static String getDownloadedFileNamePattern() {
		return store().getString(DownloadPreferencePage.DOWNLOADED_FILE_NAME_PATTERN);
	}

	public static int getDownloadedObjectsLimit() {
		return store().getInt(DownloadPreferencePage.DOWNLOADED_OBJECTS_LIMIT);
	}
	
	public static String getIncludeInDownload() {
		return store().getString(DownloadPreferencePage.INCLUDE_IN_DOWNLOAD);
	}
	
	public static String getExcludeFromDownload() {
		return store().getString(DownloadPreferencePage.EXCLUDE_FROM_DOWNLOAD);
	}
	
	public static String getOverwriteWhenDownloading() {
		return store().getString(DownloadPreferencePage.OVERWRITE_WHEN_DOWNLOADING);
	}

	private static IPreferenceStore store() {
		return EclipseActivator.getInstance().getPreferenceStore();
	}
	
	public static String getString(String key) {
		return store().getString(key);
	}

}
