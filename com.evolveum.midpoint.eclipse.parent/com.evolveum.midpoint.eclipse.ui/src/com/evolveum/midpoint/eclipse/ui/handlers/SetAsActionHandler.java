package com.evolveum.midpoint.eclipse.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class SetAsActionHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection selection = SelectionUtils.getSelection(event);
		List<IFile> files = SelectionUtils.getSelectedXmlFiles(selection);
		if (files.size() != 1) {
			Util.showAndLogWarning("No files selected", "This action requires that exactly one file is selected.");
			return null;
		}
		// TODO check validity of the file
		int actionNumber = Integer.valueOf(event.getParameter(PluginConstants.PARAM_ACTION_NUMBER));
		PluginPreferences.setActionFile(actionNumber, files.get(0).getLocation().toFile().getAbsolutePath());
		return null;
	}

}
