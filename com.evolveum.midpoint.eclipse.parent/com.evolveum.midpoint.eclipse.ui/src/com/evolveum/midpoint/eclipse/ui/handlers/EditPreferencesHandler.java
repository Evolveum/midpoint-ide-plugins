package com.evolveum.midpoint.eclipse.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;

public class EditPreferencesHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		String id = 
				PluginConstants.CMD_EDIT_ACTIONS_PREFERENCES.equals(event.getCommand().getId()) ?
						PluginPreferences.ACTIONS_PREFERENCES_ID : PluginPreferences.PREFERENCES_ID; 

		PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(
				HandlerUtil.getActiveShell(event), id, 
				new String[] { PluginPreferences.PREFERENCES_ID, PluginPreferences.ACTIONS_PREFERENCES_ID, PluginPreferences.DONWLOAD_PREFERENCES_ID, PluginPreferences.COMPARE_PREFERENCES_ID, PluginPreferences.MISC_PREFERENCES_ID }, 
				null);
		if (pref != null) {
			pref.open();
		}
		return null;
	}

}
