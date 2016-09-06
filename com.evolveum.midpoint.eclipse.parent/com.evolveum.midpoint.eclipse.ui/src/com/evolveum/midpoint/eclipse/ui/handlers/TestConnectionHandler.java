package com.evolveum.midpoint.eclipse.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.TestConnectionResponse;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class TestConnectionHandler extends AbstractHandler {

	public static final String PARAM_SERVER_NAME = "com.evolveum.midpoint.eclipse.ui.commandParameter.serverName";
	public static final String PARAM_SERVER_URL = "com.evolveum.midpoint.eclipse.ui.commandParameter.serverUrl";
	public static final String PARAM_LOGIN = "com.evolveum.midpoint.eclipse.ui.commandParameter.login";
	public static final String PARAM_PASSWORD = "com.evolveum.midpoint.eclipse.ui.commandParameter.password";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Test connection") {
			protected IStatus run(IProgressMonitor monitor) {
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				ConnectionParameters connectionParameters = PluginPreferences.getConnectionParameters();
				if (event.getParameter(PARAM_SERVER_URL) != null) {
					connectionParameters.setUrl(event.getParameter(PARAM_SERVER_URL));
				}
				if (event.getParameter(PARAM_LOGIN) != null) {
					connectionParameters.setLogin(event.getParameter(PARAM_LOGIN));
				}
				if (event.getParameter(PARAM_PASSWORD) != null) {
					connectionParameters.setPassword(event.getParameter(PARAM_PASSWORD));
				}
				if (event.getParameter(PARAM_SERVER_NAME) != null) {
					connectionParameters.setName(event.getParameter(PARAM_SERVER_NAME));
				}

				TestConnectionResponse response = runtime.testConnection(connectionParameters);
				String serverName = connectionParameters.getName();
				if (response.isSuccess()) {
					Util.showAndLogInformation("Test connection success [" + serverName + "]", "Connection to the server '" + serverName + "' is OK.");
				} else {
					Util.showAndLogError("Test connection error [" + serverName + "]", "Connection to the server '" + serverName + "' failed: " + response.getFailureDescription(), response.getException());
				}
				 
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

}
