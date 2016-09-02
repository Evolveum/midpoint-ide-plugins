package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.IPreferenceStore;

import com.evolveum.midpoint.eclipse.runtime.api.ConnectionParameters;
import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class PluginPreferences {

	public static final String PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.midPoint";
	public static final String ACTIONS_PREFERENCES_ID = "com.evolveum.midpoint.eclipse.ui.preference.actions";

	public static ConnectionParameters getConnectionParameters() {
		IPreferenceStore store = EclipseActivator.getInstance().getPreferenceStore();
		String url = store.getString(MidPointPreferencePage.MIDPOINT_URL);
		String login = store.getString(MidPointPreferencePage.MIDPOINT_LOGIN);
		String password = store.getString(MidPointPreferencePage.MIDPOINT_PASSWORD);
		return new ConnectionParameters(url, login, password);
	}
	
	public static String getActionFile(String number) {
		IPreferenceStore store = EclipseActivator.getInstance().getPreferenceStore();
		return store.getString(ActionsPreferencePage.ACTION_FILE_PREFIX + number);
	}
	
	public static String getActionOpenAfter(String number) {
		IPreferenceStore store = EclipseActivator.getInstance().getPreferenceStore();
		return store.getString(ActionsPreferencePage.ACTION_OPEN_AFTER_PREFIX + number);
	}

	public static String getActionOpenAfterOther() {
		IPreferenceStore store = EclipseActivator.getInstance().getPreferenceStore();
		return store.getString(ActionsPreferencePage.ACTION_OPEN_AFTER_OTHER);
	}


	public static String getActionAfterUpload() {
		IPreferenceStore store = EclipseActivator.getInstance().getPreferenceStore();
		return store.getString(ActionsPreferencePage.ACTION_AFTER_UPLOAD);
	}

	public static String getLogfile() {
		IPreferenceStore store = EclipseActivator.getInstance().getPreferenceStore();
		return store.getString(MidPointPreferencePage.MIDPOINT_LOGFILE);
	}
	
	public static boolean isUseMidPointLogViewer() {
		return EclipseActivator.getInstance().getPreferenceStore().getBoolean(ActionsPreferencePage.USE_MIDPOINT_LOG_VIEWER);
	}
	
	public static boolean isOutputFileNameRelative() {
		return EclipseActivator.getInstance().getPreferenceStore().getBoolean(ActionsPreferencePage.OUTPUT_FILE_NAME_RELATIVE);
	}
	
	public static String getOutputFileNamePattern() {
		return EclipseActivator.getInstance().getPreferenceStore().getString(ActionsPreferencePage.OUTPUT_FILE_NAME_PATTERN);
	}

	public static String getOutputFileNamePatternNoSource() {
		return EclipseActivator.getInstance().getPreferenceStore().getString(ActionsPreferencePage.OUTPUT_FILE_NAME_PATTERN_NO_SOURCE);
	}
	
	public static String getShowUploadOrExecuteResultMessageBox() {
		return EclipseActivator.getInstance().getPreferenceStore().getString(MidPointPreferencePage.SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX);
	}

	
	public static String getString(String key) {
		return EclipseActivator.getInstance().getPreferenceStore().getString(key);
	}

}
