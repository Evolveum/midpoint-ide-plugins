package com.evolveum.midpoint.eclipse.ui.handlers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerAction;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ExecuteActionServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.NotApplicableServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.UploadServerResponse;
import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.components.browser.BulkActionGenerator;
import com.evolveum.midpoint.eclipse.ui.components.browser.GeneratorOptions;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Expander;
import com.evolveum.midpoint.eclipse.ui.util.Severity;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class ServerActionHandler extends AbstractHandler {
	
	public enum Action {
		
		RECOMPUTE(BulkActionGenerator.Action.RECOMPUTE, "recomputed"), 
		TEST_RESOURCE(BulkActionGenerator.Action.TEST_RESOURCE, "tested"), 
		DELETE(BulkActionGenerator.Action.DELETE, "deleted");
		
		private final BulkActionGenerator.Action action; 
		private String pastTense; 
		
		Action(BulkActionGenerator.Action action, String pastTense) {
			this.action = action;
			this.pastTense = pastTense;
		}

		public static Action fromCommand(String cmd) {
			switch (cmd) {
			case PluginConstants.CMD_RECOMPUTE_ON_SERVER: return RECOMPUTE;
			case PluginConstants.CMD_TEST_RESOURCE: return TEST_RESOURCE;
			case PluginConstants.CMD_DELETE_ON_SERVER: return DELETE;
			default: throw new IllegalStateException("Unknown cmd: " + cmd);
			}
		}
		
	
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		executeServerRequest(event);
		return null;
	}
	
	private void executeServerRequest(ExecutionEvent event) {
		
		String jobTitle = "Processing objects";
		
		ServerInfo selectedServer = PluginPreferences.getSelectedServer();
		if (selectedServer == null) {
			return;		// shouldn't occur
		}
		
		if (!Expander.checkPropertiesFile(selectedServer)) {
			return;		// message was logged
		}
		
		ISelection selection = SelectionUtils.getSelection(event);
		
		Job job = new Job(jobTitle) {
			protected IStatus run(IProgressMonitor monitor) {
				
				List<SourceObject> objects = FileRequestHandler.getServerObjectsFromSelection(event, selection);
				String cmd = event.getCommand().getId();
				
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				Action action = Action.fromCommand(cmd);
				
				List<SourceObject> objectsFiltered = new ArrayList<>();
				for (SourceObject o : objects) {
					switch (action) {
					case TEST_RESOURCE: 
						if (o.getType() != ObjectTypes.RESOURCE) {
							continue;
						}
						break;
					case RECOMPUTE:
						if (!ObjectTypes.FOCUS.isAssignableFrom(o.getType())) {
							continue;
						}
						break;
					case DELETE:
						if (o.getType() == null) {
							continue;
						}
					}
					objectsFiltered.add(o);
				}
				if (objectsFiltered.isEmpty()) {
					Util.showWarning("No objects to process", "There are no applicable objects to be " + action.pastTense + ".");
					return Status.OK_STATUS;
				}
				
				GeneratorOptions genOptions = new GeneratorOptions();
				if ("true".equals(event.getParameter(PluginConstants.PARAM_RAW))) {
					genOptions.setRaw(true);
				}
				
				int ok = 0, fail = 0;
				
				monitor.beginTask("Processing", objectsFiltered.size());
				for (SourceObject object : objectsFiltered) {
					if (monitor.isCanceled()) {
						break;
					}
					
					if ("true".equals(event.getParameter(PluginConstants.PARAM_UPLOAD_FIRST))) {
						ServerRequest uploadRequest = new ServerRequest(ServerAction.UPLOAD, object.getContent());
						ServerResponse uploadResponse = runtime.executeServerRequest(uploadRequest, PluginPreferences.getConnectionParameters());
						if (!(uploadResponse instanceof UploadServerResponse)) {
							Console.logError("Couldn't upload object " + object.getDisplayName() + ": unexpected response: " + uploadResponse.getClass());
							fail++;
							continue;
						}
						UploadServerResponse usr = (UploadServerResponse) uploadResponse;
						if (usr.isSuccess()) {
							Console.logMinor("Object " + object.getDisplayName() + " uploaded OK");
						} else {
							Console.logError("Object " + object.getDisplayName() + " couldn't be uploaded; skipping action execution. Reason: " + usr.getErrorDescription(), usr.getException());
							fail++;
							continue;
						}
					}
					
					BulkActionGenerator gen = new BulkActionGenerator(action.action);
					String requestString = gen.generateFromSourceObject(object, genOptions);
					System.out.println("Executing: " + requestString);

					ServerRequest request = new ServerRequest(ServerAction.EXECUTE, requestString);
					ServerResponse response = runtime.executeServerRequest(request, PluginPreferences.getConnectionParameters());
					
					if (response instanceof NotApplicableServerResponse) {
						// shouldn't occur
						Console.logWarning("Item " + object.getDisplayName() + " was not applicable for this operation; skipping it: " + ((NotApplicableServerResponse) response).getMessage());
					} else {
						ExecuteActionServerResponse easr = (ExecuteActionServerResponse) response;
						if (easr.isSuccess()) {
							if (easr.getConsoleOutput() != null && easr.getConsoleOutput().startsWith("Warning: no matching")) {
								fail++;
								Console.logWarning("Object " + object.getDisplayName() + " doesn't exist on server; name = " + object.getName() + ", oid = " + object.getOid());
							} else {
								ok++;
								Console.logMinor("Object " + object.getDisplayName() + " " + action.pastTense + " OK");
							}
						} else {
							fail++;
							Console.logError("Error processing object " + object.getDisplayName() + ": " + easr.getErrorDescription(), easr.getException());
						}
					}
					
					if (action == Action.DELETE && "true".equals(event.getParameter(PluginConstants.PARAM_ALSO_LOCALLY))) {
						if (object.isLast()) {
							if (object.getFile() != null && object.isWholeFile()) {
								try {
									object.getFile().delete(true, monitor);
									Console.logMinor("File " + object.getFile().getFullPath() + " deleted locally.");
								} catch (CoreException e) {
									Console.logError("Couldn't delete local file " + object.getFile().getFullPath(), e);
									fail++;
								}
							} else {
								Console.logWarning("Object " + object.getDisplayName() + " cannot be deleted locally, because not the whole file was covered by deleted objects.");
								fail++;
							}
						}
					}
							
					monitor.worked(1);
				}
				monitor.done();
				Console.log(fail > 0 ? Severity.ERROR : Severity.INFO, null, StringUtils.capitaliseAllWords(action.pastTense) + " " + (ok+fail) + " objects: OK: " + ok + ", failure: " + fail);

				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}


}
