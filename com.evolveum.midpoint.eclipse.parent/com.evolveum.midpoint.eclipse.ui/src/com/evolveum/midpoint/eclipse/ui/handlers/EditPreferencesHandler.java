package com.evolveum.midpoint.eclipse.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;

public class EditPreferencesHandler extends AbstractHandler {

	public static final String CMD_EDIT_ACTIONS_PREFERENCES = "com.evolveum.midpoint.eclipse.ui.command.editActionsPreferences";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		String id = 
				CMD_EDIT_ACTIONS_PREFERENCES.equals(event.getCommand().getId()) ?
						PluginPreferences.ACTIONS_PREFERENCES_ID : PluginPreferences.PREFERENCES_ID; 

		PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(HandlerUtil.getActiveShell(event), id, null, null);
		if (pref != null) {
			pref.open();
		}
		return null;
	}

}
