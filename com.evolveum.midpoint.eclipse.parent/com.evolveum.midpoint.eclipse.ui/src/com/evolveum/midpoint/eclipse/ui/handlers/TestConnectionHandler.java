package com.evolveum.midpoint.eclipse.ui.handlers;

import java.util.List;
import java.util.logging.Level;

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
import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerDataItem;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Severity;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class TestConnectionHandler extends AbstractHandler {

	public static final String PARAM_SERVER_NAME = "com.evolveum.midpoint.eclipse.ui.commandParameter.serverName";
	public static final String PARAM_SERVER_URL = "com.evolveum.midpoint.eclipse.ui.commandParameter.serverUrl";
	public static final String PARAM_LOGIN = "com.evolveum.midpoint.eclipse.ui.commandParameter.login";
	public static final String PARAM_PASSWORD = "com.evolveum.midpoint.eclipse.ui.commandParameter.password";

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		
		Job job = new Job("Test connection(s)") {
			protected IStatus run(IProgressMonitor monitor) {
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				
				ConnectionParameters connectionParameters = null; 
				if (PluginConstants.CMD_TEST_CONNECTION.equals(event.getCommand().getId())) {
					connectionParameters = new ConnectionParameters(
							event.getParameter(PARAM_SERVER_NAME),
							event.getParameter(PARAM_SERVER_URL),
							event.getParameter(PARAM_LOGIN),
							event.getParameter(PARAM_PASSWORD));
				} else {
					List<ServerDataItem> servers = PluginPreferences.getServers();
					if (servers.size() == 0) {
						return Status.OK_STATUS;		// we shouldn't have come here
					} else if (servers.size() == 1) {
						connectionParameters = servers.get(0).getConnectionParameters();
					}
				}
				
				if (connectionParameters != null) {
					TestConnectionResponse response = runtime.testConnection(connectionParameters);
					String serverName = connectionParameters.getDisplayName();
					if (response.isSuccess()) {
						Util.showAndLog(Severity.INFO, Util.NO_SERVER_NAME, "Test connection success [" + serverName + "]", "Connection to the server '" + serverName + "' is OK.");
					} else {
						Util.showAndLog(Severity.ERROR, Util.NO_SERVER_NAME, "Test connection error [" + serverName + "]", "Connection to the server '" + serverName + "' failed: " + response.getFailureDescription(), response.getException());
					}
					return Status.OK_STATUS;
				}

				int countOk = 0, countFail = 0;
				
				List<ServerDataItem> servers = PluginPreferences.getServers();
				monitor.beginTask("Processing", servers.size());
				for (ServerDataItem server : servers) {
					if (monitor.isCanceled()) {
						break;
					}
					monitor.subTask(server.getDisplayName());
					
					TestConnectionResponse response = runtime.testConnection(server.getConnectionParameters());
					String serverName = server.getDisplayName();
					if (response.isSuccess()) {
						Console.log(Severity.INFO, Util.NO_SERVER_NAME, "Connection to the server '"+serverName+"' is OK.");
						countOk++;
					} else {
						Console.log(Severity.ERROR, Util.NO_SERVER_NAME, "Connection to the server '"+serverName+"' failed: " + response.getFailureDescription(), response.getException());
						countFail++;
					}
					monitor.worked(1);
				}
				
				if (!monitor.isCanceled()) {
					if (countFail == 0) {
						Util.showAndLog(Severity.INFO, Util.NO_SERVER_NAME, "Test connection success", "Connection to all " + countOk + " servers is OK.");
					} else {
						Util.showAndLog(Severity.ERROR, Util.NO_SERVER_NAME, "Test connection failure", "Connection to " + countFail + " server(s) failed. " + countOk + " server(s) are OK.");
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

}
