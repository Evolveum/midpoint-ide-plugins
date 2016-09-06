package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class MidPointPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = EclipseActivator.getInstance().getPreferenceStore();

		store.setDefault(MidPointPreferencePage.MIDPOINT_URL, "http://localhost:8080/midpoint/ws/rest");
		store.setDefault(MidPointPreferencePage.MIDPOINT_LOGIN, "administrator");
		store.setDefault(MidPointPreferencePage.MIDPOINT_PASSWORD, "5ecr3t");
		store.setDefault(MidPointPreferencePage.SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX, MidPointPreferencePage.VALUE_ALWAYS);
		store.setDefault(MidPointPreferencePage.SHOW_COMPARISON_RESULT_MESSAGE_BOX, MidPointPreferencePage.VALUE_ALWAYS);
		
		store.setDefault(ActionsPreferencePage.ACTION_OUTPUT_FILE_NAME_PATTERN, "runs/$f.$n.$t");
		
		store.setDefault(DownloadPreferencePage.DOWNLOADED_FILE_NAME_PATTERN, "$T/$n.xml");
		store.setDefault(DownloadPreferencePage.DOWNLOADED_OBJECTS_LIMIT, "100");
		store.setDefault(DownloadPreferencePage.OVERWRITE_WHEN_DOWNLOADING, DownloadPreferencePage.VALUE_ASK);
		
		store.setDefault(ComparePreferencePage.COMPARE_RESULT_FILE_NAME_PATTERN, "diff/$F.$n.$t");
		store.setDefault(ComparePreferencePage.COMPARE_SHOW_LOCAL_TO_REMOTE, "true");
		store.setDefault(ComparePreferencePage.COMPARE_SHOW_REMOTE_TO_LOCAL, "true");
		store.setDefault(ComparePreferencePage.COMPARE_SHOW_LOCAL_NORMALIZED, "true");
		store.setDefault(ComparePreferencePage.COMPARE_SHOW_REMOTE, "true");
		store.setDefault(ComparePreferencePage.COMPARE_IGNORE_METADATA, "true");
		store.setDefault(ComparePreferencePage.COMPARE_OTHER_ITEMS_TO_IGNORE, "");
	}
	
}
