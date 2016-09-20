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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;

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
import com.evolveum.midpoint.eclipse.ui.handlers.ServerActionHandler.Action;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Expander;
import com.evolveum.midpoint.eclipse.ui.util.Severity;
import com.evolveum.midpoint.eclipse.ui.util.Util;
import com.evolveum.midpoint.util.Holder;

public class ServerActionHandler extends AbstractHandler {
	
	public enum Action {
		
		RECOMPUTE(BulkActionGenerator.Action.RECOMPUTE, "recomputed"), 
		TEST_RESOURCE(BulkActionGenerator.Action.TEST_RESOURCE, "tested"), 
		VALIDATE(BulkActionGenerator.Action.VALIDATE, "validated"),
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
		
		boolean uploadFirst = "true".equals(event.getParameter(PluginConstants.PARAM_UPLOAD_FIRST));
		if (uploadFirst && !Expander.checkPropertiesFile(selectedServer)) {
			return;		// message was logged
		}
		
		ISelection selection = SelectionUtils.getSelection(event);
		
		Job job = new Job(jobTitle) {
			protected IStatus run(IProgressMonitor monitor) {
				
				boolean alsoLocally = "true".equals(event.getParameter(PluginConstants.PARAM_ALSO_LOCALLY));
				
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
				
				if (action == Action.DELETE) {
					Holder<Integer> responseHolder = new Holder<>();
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog dialog = new MessageDialog(
									null, "Confirm delete", null, 
									"Are you sure to delete " + objectsFiltered.size() + " object(s) on server " + selectedServer.getDisplayName() + 
										(alsoLocally ? " and also locally" : "") + "?",
									MessageDialog.QUESTION,
									new String[] {"Yes", "Cancel"},
									0);
							responseHolder.setValue(dialog.open());
						}
					});
					if (responseHolder.getValue() != 0) {
						return Status.OK_STATUS;
					}
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
					
					if (uploadFirst) {
						String content = object.getContent();
						String expandedContent = Expander.expand(content, object, PluginPreferences.getSelectedServer());
						ServerRequest uploadRequest = new ServerRequest(ServerAction.UPLOAD, expandedContent);
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
					
					boolean success = executeAction(action, object, genOptions);
					if (action == Action.TEST_RESOURCE && lastResponse != null) {
						ResourceUtils.applyTestResult(object, lastResponse);
					}
					
					if (success && action == Action.TEST_RESOURCE && "true".equals(event.getParameter(PluginConstants.PARAM_VALIDATE))) {
						success = executeAction(Action.VALIDATE, object, genOptions);
						if (lastResponse != null) {
							ResourceUtils.applyValidationResult(object, lastResponse.getDataOutput());
						}
					}
					
					if (success) {
						ok++;
					} else {
						fail++;
					}
					
					if (action == Action.DELETE && alsoLocally) {
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
	
	private ExecuteActionServerResponse lastResponse;		// FIXME brutal hack!!!

	protected boolean executeAction(Action action, SourceObject object, GeneratorOptions genOptions) {
		BulkActionGenerator gen = new BulkActionGenerator(action.action);
		String requestString = gen.generateFromSourceObject(object, genOptions);
		System.out.println("Executing: " + requestString);

		ServerRequest request = new ServerRequest(ServerAction.EXECUTE, requestString);
		RuntimeService runtime = RuntimeActivator.getRuntimeService();
		ServerResponse response = runtime.executeServerRequest(request, PluginPreferences.getConnectionParameters());
		
		if (response instanceof NotApplicableServerResponse) {
			// shouldn't occur
			Console.logWarning("Item " + object.getDisplayName() + " was not applicable for this operation; skipping it: " + ((NotApplicableServerResponse) response).getMessage());
			lastResponse = null;
			return false;
		} else {
			ExecuteActionServerResponse easr = (ExecuteActionServerResponse) response;
			lastResponse = easr;
			if (easr.isSuccess()) {
				if (easr.getConsoleOutput() != null && easr.getConsoleOutput().startsWith("Warning: no matching")) {
					Console.logWarning("Object " + object.getDisplayName() + " doesn't exist on server; name = " + object.getName() + ", oid = " + object.getOid());
					return false;
				} else {
					Console.logMinor("Object " + object.getDisplayName() + " " + action.pastTense + " OK");
					return true;
				}
			} else {
				Console.logError("Error processing object " + object.getDisplayName() + ": " + easr.getErrorDescription(), easr.getException());
				return false;
			}
		}
	}


}
