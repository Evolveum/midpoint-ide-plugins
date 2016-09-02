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
		
		store.setDefault(ActionsPreferencePage.OUTPUT_FILE_NAME_PATTERN, "runs/$f.$n.$t");
		store.setDefault(ActionsPreferencePage.OUTPUT_FILE_NAME_RELATIVE, "true");
	}
}
