package com.evolveum.midpoint.eclipse.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.evolveum.midpoint.eclipse.ui.util.Console;

public class ShowConsoleHandler extends AbstractHandler {

	public static final String PARAM_SERVER_URL = "com.evolveum.midpoint.eclipse.ui.commandParameter.serverUrl";
	public static final String PARAM_LOGIN = "com.evolveum.midpoint.eclipse.ui.commandParameter.login";
	public static final String PARAM_PASSWORD = "com.evolveum.midpoint.eclipse.ui.commandParameter.password";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		Console.showConsole();
		return null;
	}

}
