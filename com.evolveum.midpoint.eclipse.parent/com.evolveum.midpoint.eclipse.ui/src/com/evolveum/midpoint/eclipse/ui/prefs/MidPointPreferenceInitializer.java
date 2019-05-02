package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class MidPointPreferenceInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = EclipseActivator.getInstance().getPreferenceStore();

		store.setDefault(UploadPreferencePage.SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX, MidPointPreferencePage.VALUE_ALWAYS);
		store.setDefault(ComparePreferencePage.SHOW_COMPARISON_RESULT_MESSAGE_BOX, MidPointPreferencePage.VALUE_ALWAYS);
		
		store.setDefault(ActionsPreferencePage.ACTION_OUTPUT_FILE_NAME_PATTERN, "scratch/runs/$f.$n.$t");
		store.setDefault(ActionsPreferencePage.ACTION_OUTPUT_ROOT_DIRECTORY, MidPointPreferencePage.VALUE_CURRENT_PROJECT);
		
		store.setDefault(DownloadPreferencePage.DOWNLOADED_FILE_NAME_PATTERN, "objects/$T/$n.xml");
		store.setDefault(DownloadPreferencePage.DOWNLOADED_FILES_ROOT_DIRECTORY, MidPointPreferencePage.VALUE_CURRENT_PROJECT);
		store.setDefault(DownloadPreferencePage.DOWNLOADED_OBJECTS_LIMIT, "100");
		store.setDefault(DownloadPreferencePage.EXCLUDE_FROM_DOWNLOAD, "users,shadows,cases,reportOutputs,connectors,accessCertificationCampaigns,nodes");
		store.setDefault(DownloadPreferencePage.OVERWRITE_WHEN_DOWNLOADING, DownloadPreferencePage.VALUE_ASK);
		
		store.setDefault(ComparePreferencePage.COMPARE_RESULT_FILE_NAME_PATTERN, "scratch/diff/$F.$t");
		store.setDefault(ComparePreferencePage.COMPARE_RESULT_ROOT_DIRECTORY, MidPointPreferencePage.VALUE_CURRENT_PROJECT);
		store.setDefault(ComparePreferencePage.COMPARE_SHOW_LOCAL_TO_REMOTE, "true");
		store.setDefault(ComparePreferencePage.COMPARE_SHOW_REMOTE_TO_LOCAL, "true");
		store.setDefault(ComparePreferencePage.COMPARE_SHOW_LOCAL_NORMALIZED, "true");
		store.setDefault(ComparePreferencePage.COMPARE_SHOW_REMOTE, "true");
		store.setDefault(ComparePreferencePage.COMPARE_IGNORE_OPERATIONAL_DATA, "true");
		store.setDefault(ComparePreferencePage.COMPARE_OTHER_ITEMS_TO_IGNORE, "");
		
		store.setDefault(MidPointPreferencePage.SERVERS, ServerInfo.createDefaultXml());
		
		store.setDefault(LogPreferencePage.LOG_CONSOLE_REFRESH_INTERVAL, "1");
		store.setDefault(LogPreferencePage.LOG_GO_BACK_N, "100");
		store.setDefault(LogPreferencePage.LOG_FILE_NAME_PATTERN, "scratch/log/$n.log");
		store.setDefault(LogPreferencePage.LOG_FILE_DEFAULT_PROJECT, "*");
		store.setDefault(LogPreferencePage.USE_MIDPOINT_LOG_VIEWER, MidPointPreferencePage.VALUE_ONLY_IF_COMPLEX);
		
		store.setDefault(UploadPreferencePage.VALIDATE_AFTER_UPLOAD, "true");
		
		store.setDefault(MiscPreferencePage.GENERATED_FILE_NAME_PATTERN, "scratch/gen/$n.xml");
	}
	
}
