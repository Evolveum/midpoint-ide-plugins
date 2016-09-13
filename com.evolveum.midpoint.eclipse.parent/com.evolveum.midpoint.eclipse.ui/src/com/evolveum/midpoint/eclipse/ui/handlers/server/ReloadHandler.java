package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.resp.NotApplicableServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.SearchObjectsServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerResponse;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.Source;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.WorkspaceFileSource;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Severity;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class ReloadHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		ISelection selection = SelectionUtils.getSelection(event);
		List<IFile> files = SelectionUtils.getSelectedXmlFiles(selection);
		if (files.isEmpty()) {
			Util.showWarning("No files selected", "There are no XML files to be processed.");
			return null;
		}

		ServerInfo selectedServer = PluginPreferences.getSelectedServer();
		if (selectedServer == null) {
			return null;		// shouldn't occur
		}

		MessageDialog dialog = new MessageDialog(
				null, "Confirm action", null, "Are you sure you want to reload " + files.size() + " file(s) from the server '" + selectedServer.getDisplayName() + "'?",
				MessageDialog.QUESTION, new String[] {"Yes", "Cancel"}, 1);
		int answer = dialog.open();
		if (answer != 0) {
			return null;
		}
		
		Job job = new Job("Reloading files from server") {
			protected IStatus run(IProgressMonitor monitor) {
				
				final int itemCount = files.size();
				
				ConnectionParameters connectionParameters = PluginPreferences.getConnectionParameters();

				int skipped = 0;
				int missing = 0;
				int ambiguous = 0;
				int reloaded = 0;
				int failed = 0;
				
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				monitor.beginTask("Processing", itemCount);
				for (IFile file : files) {
					IPath path = file.getFullPath();
					if (monitor.isCanceled()) {
						break;
					}
					monitor.subTask(file.getFullPath().toString());
					
					try {
						Source source = new WorkspaceFileSource(file);
						ServerResponse response = runtime.getCurrentVersionOfObject(source.resolve(), connectionParameters);
					
						if (response instanceof NotApplicableServerResponse) {
							Console.logWarning("File " + path + " was not applicable for this operation; skipping it: " + ((NotApplicableServerResponse) response).getMessage());
							skipped++;
						} else {
							SearchObjectsServerResponse searchResult = (SearchObjectsServerResponse) response;
							if (!searchResult.isSuccess()) {
								Console.logError("File " + path + " couldn't be reloaded: " + searchResult.getErrorDescription(), searchResult.getException());
								failed++;
							} else if (searchResult.getServerObjects().isEmpty()) {
								Console.logError("File " + path + " couldn't be reloaded: There is no such object on the server");
								missing++;
							} else if (searchResult.getServerObjects().size() > 1) {
								Console.logWarning("File " + path + " couldn't be reloaded: There are " + searchResult.getServerObjects().size() + " applicable objects on the server");
								ambiguous++;
							} else {
								String data = searchResult.getServerObjects().get(0).getXml();
								file.setContents(IOUtils.toInputStream(data, "utf-8"), true, false, monitor);
								Console.logMinor("File " + path + " reloaded from server.");
								reloaded++;
							}
						}
					} catch (Throwable t) {
						Console.logError("File " + path + " couldn't be reloaded: " + t.getMessage(), t);
						failed++;
					}
					
					monitor.worked(1);
				}
				monitor.done();
				
				Severity severity;
				if (failed > 0) {
					severity = Severity.ERROR;
				} else if (missing > 0 || ambiguous > 0) {
					severity = Severity.WARN;
				} else {
					severity = Severity.INFO;
				}
				Util.showAndLog(severity, null, "Reload result", "Reloaded files: " + reloaded + ", no longer existing: " + missing + ", ambiguous: " + ambiguous + ", skipped: " + skipped + ", failed: " + failed);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}
}
