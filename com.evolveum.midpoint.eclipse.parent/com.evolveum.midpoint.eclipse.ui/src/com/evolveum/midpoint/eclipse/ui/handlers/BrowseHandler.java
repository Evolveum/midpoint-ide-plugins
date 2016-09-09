package com.evolveum.midpoint.eclipse.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

import com.evolveum.midpoint.eclipse.ui.components.browser.BrowserDialog;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;

public class BrowseHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection selection = SelectionUtils.getSelection(event);
		BrowserDialog dialog = new BrowserDialog(HandlerUtil.getActiveShell(event), selection);
		dialog.create();
		if (dialog.open() == Window.OK) {
		} 


		return null;
	}

}
