package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMarkSelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.CompareServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ExecuteActionServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.NotApplicableServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerResponse;
import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.handlers.ServerLogUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
import com.evolveum.midpoint.eclipse.ui.prefs.MidPointPreferencePage;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Expander;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class FileRequestHandler extends AbstractHandler {
	
	private static final String LOGVIEWER_ID = "com.evolveum.midpoint.eclipse.logviewer.editor.LogViewerEditor";

	public static final String DEFAULT_EDITOR_ID = "org.eclipse.ui.DefaultTextEditor";

	private static final int MAX_ITERATIONS = 1000;
	
	private static final int LOG_FRAGMENT_COMPLEXITY_THRESHOLD = 100000;
	
	public enum RequestedAction { UPLOAD_OR_EXECUTE, UPLOAD_OR_EXECUTE_WITH_ACTION, EXECUTE_ACTION, COMPARE };
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		RequestedAction action;
		
		Command cmd = event.getCommand();
		if (PluginConstants.CMD_UPLOAD_OR_EXECUTE.equals(cmd.getId())) {
			if ("true".equals(event.getParameter(PluginConstants.PARAM_WITH_ACTION))) {
				action = RequestedAction.UPLOAD_OR_EXECUTE_WITH_ACTION;
			} else {
				action = RequestedAction.UPLOAD_OR_EXECUTE;
			}
		} else if (PluginConstants.CMD_EXECUTE_ACTION.equals(cmd.getId())) {
			action = RequestedAction.EXECUTE_ACTION;
		} else if (PluginConstants.CMD_COMPUTE_DIFFERENCE.equals(cmd.getId())) {
			action = RequestedAction.COMPARE;
		} else {
			throw new IllegalArgumentException("Unsupported command id: " + cmd.getId());
		}
		
		executeServerRequest(event, action);
		return null;
	}

	private static ServerRequestPack createRequestForPredefinedAction(int actionNumber) {
		String fileName = PluginPreferences.getActionFile(actionNumber);
		if (fileName == null || fileName.isEmpty()) {
			Util.showAndLogWarning("No file for action", "Action #" + actionNumber + " has no file defined.");
			return ServerRequestPack.EMPTY;
		} else if (!new File(fileName).exists()) {
			Util.showAndLogWarning("No file for action", "File for action #" + actionNumber + " (" + fileName + ") does not exist or is not readable.");
			return ServerRequestPack.EMPTY;
		} else {
			ServerRequestPack pack = ServerRequestPack.fromPhysicalActionFile(fileName, actionNumber);
			if (pack.isEmpty()) {
				Util.showAndLogWarning("No executable content", "No executable content in file for action #" + actionNumber + " (" + fileName + ")");
			}
			return pack;
		}
	}

	public static void executeServerRequest(ExecutionEvent event, RequestedAction requestedAction) {
		
		String jobTitle;
		switch (requestedAction) {
		case COMPARE: 
			jobTitle = "Comparing objects"; break;
		case EXECUTE_ACTION:
			jobTitle = "Executing"; break;
		default:
			jobTitle = "Uploading/executing";
		}
		
		boolean stopOnError = "true".equals(event.getParameter(PluginConstants.PARAM_STOP_ON_ERROR));
		
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
				
				ServerRequestPack requestPack;
				switch (requestedAction) {
				case EXECUTE_ACTION:
					requestPack = createRequestForPredefinedAction(Integer.valueOf(event.getParameter(PluginConstants.PARAM_ACTION_NUMBER)));
					break;
				case COMPARE:
					requestPack = FileRequestHandler.createRequestPackFromSelection(event, requestedAction, selection);
					break;
				case UPLOAD_OR_EXECUTE:
					requestPack = FileRequestHandler.createRequestPackFromSelection(event, requestedAction, selection);
					break;
				case UPLOAD_OR_EXECUTE_WITH_ACTION:
					requestPack = FileRequestHandler.createRequestPackFromSelection(event, requestedAction, selection);
					if (requestPack.isEmpty()) {
						return Status.OK_STATUS;
					}
					int actionAfterUpload = PluginPreferences.getActionAfterUpload();
					if (actionAfterUpload != 0) {
						requestPack.add(createRequestForPredefinedAction(actionAfterUpload).getItems());
					}
					break;
				default:
					throw new AssertionError();
				}
				
				executePack(requestPack, requestedAction, stopOnError, monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}

	public static void executePack(ServerRequestPack requestPack, RequestedAction requestedAction, boolean stopOnError, IProgressMonitor monitor) {
		System.out.println("Server request pack: " + requestPack);

		String logfilename = PluginPreferences.getLogfile();

		final int itemCount = requestPack.getItemCount();
		if (itemCount == 0) {
			// shouldn't get here
			return;
		}

		ConnectionParameters connectionParameters = PluginPreferences.getConnectionParameters();

		List<ServerResponseItem<?>> responseItems = new ArrayList<>();

		int skipped = 0;
		int skippedAloud = 0;

		RuntimeService runtime = RuntimeActivator.getRuntimeService();
		monitor.beginTask("Processing", itemCount);
		for (ServerRequestItem item : requestPack.getItems()) {
			if (monitor.isCanceled()) {
				break;
			}
			if (item.getDisplayName() != null) {
				monitor.subTask(item.getDisplayName());
			}

			Long logPosition = ServerLogUtils.getLogPosition();

			ServerRequest request = item.createServerRequest();
			if (request == null) {
				skippedAloud++;		// hack
				skipped++;
			} else {
				ServerResponse response = runtime.executeServerRequest(request, connectionParameters);

				if (response instanceof NotApplicableServerResponse) {
					Console.logWarning("Item " + item.getDisplayName() + " was not applicable for this operation; skipping it: " + ((NotApplicableServerResponse) response).getMessage());
					skipped++;
				} else {
					ServerResponseItem<?> responseItem;
					if (response instanceof ExecuteActionServerResponse) {
						responseItem = new ExecuteActionResponseItem(item, request, (ExecuteActionServerResponse) response, logfilename, logPosition);
					} else if (response instanceof CompareServerResponse) {
						responseItem = new CompareServerResponseItem(item, request, (CompareServerResponse) response);
					} else {
						responseItem = new UploadServerResponseItem(item, request, response);
					}
					responseItems.add(responseItem);

					int execCounter = PluginPreferences.getExecCounter();
					if (execCounter == 0) {
						execCounter = 1;
					}

					boolean ok = false;
					for (int i = 0; i < MAX_ITERATIONS; i++) {
						responseItem.prepareFileNames(execCounter);
						if (!responseItem.fileConflictsPresent()) {
							ok = true;
							break;
						}
						execCounter++;
					}
					if (!ok) {
						throw new IllegalStateException("No free file name even after "+MAX_ITERATIONS+" iterations");	// TODO
					}
					responseItem.createFiles();
					responseItem.openFileIfNeeded();

					responseItem.logResult(execCounter);

					if (response instanceof ExecuteActionServerResponse || response instanceof CompareServerResponse) {
						execCounter++;
						PluginPreferences.setExecCounter(execCounter);
					}

					if (stopOnError && !responseItem.isSuccess()) {
						Console.logWarning("Stopping on error (as requested).");
						break;
					}
				}
			}

			monitor.worked(1);
		}
		monitor.done();

		String showBoxCondition = 
				requestedAction == RequestedAction.COMPARE ? 
						PluginPreferences.getShowComparisonResultMessageBox() : PluginPreferences.getShowUploadOrExecuteResultMessageBox();

						{
							boolean showBox = false;
							int uploadOk = 0, uploadWarn = 0, uploadFail = 0, execOk = 0, execWarn = 0, execFail = 0, diffFail = 0, diffMissing = 0, diffModified = 0, diffSame = 0;
							for (ServerResponseItem<?> responseItem : responseItems) {
								if (responseItem.showResultLine(showBoxCondition)) {
									showBox = true;
								}
								if (responseItem instanceof UploadServerResponseItem) {
									if (responseItem.isSuccess()) {
										uploadOk++; 
									} else if (responseItem.isWarning()) {
										uploadWarn++;
									} else {
										uploadFail++;
									}
								} else if (responseItem instanceof ExecuteActionResponseItem) {
									if (responseItem.isSuccess()) {
										execOk++; 
									} else if (responseItem.isWarning()) {
										execWarn++;
									} else {
										execFail++;
									}
								} else {
									CompareServerResponseItem csri = (CompareServerResponseItem) responseItem;
									CompareServerResponse csr = csri.getResponse();
									if (!csr.isSuccess() || csr.getRemoteExists() == null) {
										diffFail++;
									} else if (!csr.getRemoteExists()) {
										diffMissing++;
									} else if (!csr.noDifferences()) {
										diffModified++;
									} else {
										diffSame++;
									}
								}
							}

							boolean noItems = false;
							String message;
							if (requestedAction == RequestedAction.COMPARE) {
								if (diffSame == 0 && diffModified == 0 && diffMissing == 0 && diffFail == 0) {
									message = "No items compared.";
									noItems = true;
								} else {
									StringBuilder sb = new StringBuilder();
									sb.append("No differences: ").append(diffSame).append(", modified: ").append(diffModified).append(", not on server: ").append(diffMissing).append(", failures: ").append(diffFail).append(". ");
									message = sb.toString();
								}
							} else {
								StringBuilder sb = new StringBuilder();
								boolean uploaded = false, executed = false;
								if (uploadOk > 0 || uploadWarn > 0 || uploadFail > 0) {
									sb.append("Uploaded OK: ").append(uploadOk).append(", warn: ").append(uploadWarn).append(", fail: ").append(uploadFail).append(". ");
									uploaded = true;
								}
								if (execOk > 0 || execFail > 0 || execWarn > 0) {
									sb.append("Executed OK: ").append(execOk).append(", warn: ").append(execWarn).append(", fail: ").append(execFail).append(". ");
									executed = true;
								}
								if (!uploaded && !executed) {
									sb.append("No items uploaded or executed");
									noItems = true;
								}
								message = sb.toString();
							}
							if (skipped > 0) {
								message += "Skipped: " + skipped + ".";
							}
							if (noItems) {
								if (skippedAloud > 0) {
									Console.logWarning("There were no items to be processed.");
								} else {
									Util.showAndLogWarning("No items", "There were no items to be processed.");
								}
							} else {
								boolean someFailure = diffFail > 0 || uploadFail > 0 || execFail > 0;
								boolean someWarning = execWarn > 0;
								if (someFailure) {
									Console.logError(message);
									if (showBox) {
										Util.showError("Failure", message);
									}
								} else if (someWarning) {
									Console.logWarning(message);
									if (showBox) {
										Util.showInformation("Warning", message);
									}
								} else {
									Console.log(message);
									if (showBox) {
										Util.showInformation("Success", message);
									}
								}
							}
						}
	}

	public static ServerRequestPack createRequestPackFromSelection(ExecutionEvent event, RequestedAction action, ISelection selection) {
		
		String selectedText;
		if (selection instanceof ITextSelection) {
			ITextSelection ts  = (ITextSelection) selection;
			selectedText = ts.getText();
		} else if (selection instanceof IMarkSelection) {
			IMarkSelection ms = (IMarkSelection) selection;
			try {
			    selectedText = ms.getDocument().get(ms.getOffset(), ms.getLength());
			} catch (BadLocationException e) { 
				Util.processUnexpectedException(e);
				return ServerRequestPack.EMPTY;
			}
		} else if (selection instanceof IStructuredSelection) {
			List<IFile> files = SelectionUtils.getXmlFiles((IStructuredSelection) selection);
			if (files.isEmpty()) {
				Util.showWarning("No files", "There are no XML files to be processed.");
				return ServerRequestPack.EMPTY;
			}
			return ServerRequestPack.fromWorkspaceFiles(files, action);
		} else {
			Util.showWarning("No selection", "You have not selected any items to be processed.");
			return ServerRequestPack.EMPTY;
		}
	
		//System.out.println("Selected text=[" + selectedText + "]");
		if (selectedText == null || selectedText.isEmpty()) {		// note "no trim" here!
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			if (editor != null) {
				IEditorInput editorInput = editor.getEditorInput();
				IDocument doc = (IDocument)editor.getAdapter(IDocument.class);
				if (doc != null) {
					selectedText = doc.get();
					IFile file = editorInput instanceof FileEditorInput ? ((FileEditorInput) editorInput).getFile() : null;
					if (selectedText != null && !selectedText.trim().isEmpty()) {
						if (file == null) {
							if (action == RequestedAction.COMPARE) {
								Util.showWarning("No file", "Text selection is not supported for the 'compare' action. Please select one or more files.");
								return ServerRequestPack.EMPTY;
							}
						}
						return ServerRequestPack.fromTextFragment(selectedText, file, true, action);
					}
				}
			}
		}
		if (selectedText == null || selectedText.trim().isEmpty()) {
			Util.showWarning("No selection", "There is no content to be uploaded or executed.");
			return ServerRequestPack.EMPTY;
		}
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		if (editor != null) {
			IEditorInput editorInput = editor.getEditorInput();
			IFile file = editorInput instanceof FileEditorInput ? ((FileEditorInput) editorInput).getFile() : null;
			IDocument doc = (IDocument)editor.getAdapter(IDocument.class);
			if (doc != null && file != null) {
				String fileText = doc.get();
				if (selectedText.equals(fileText)) {
					return ServerRequestPack.fromTextFragment(selectedText, file, true, action);
				}
			}
		}
				
		if (action == RequestedAction.COMPARE) {
			Util.showWarning("No file", "Text selection is not supported for the 'compare' action. Please select one or more files.");
			return ServerRequestPack.EMPTY;
		}
		return ServerRequestPack.fromTextFragment(selectedText, null, false, action);
	}

	// TODO move somewhere
	public static String getLogViewerEditorId(byte[] logFileFragment) {
		String useLogViewer = PluginPreferences.getUseMidPointLogViewer();
		switch(useLogViewer) {
		case MidPointPreferencePage.VALUE_ONLY_IF_COMPLEX:
			if (logFileFragment == null) {
				return DEFAULT_EDITOR_ID;
			}
			if (logFileFragment.length >= LOG_FRAGMENT_COMPLEXITY_THRESHOLD) {
				return LOGVIEWER_ID;
			}
			String logtext;
			try {
				logtext = new String(logFileFragment, "utf-8");
			} catch (UnsupportedEncodingException e) {
				Console.logError("Couldn't retrieve text of log file fragment", e);
				return DEFAULT_EDITOR_ID;
			}
			if (logtext.contains("---[") || logtext.contains("===[")) {
				return LOGVIEWER_ID;
			}
			return DEFAULT_EDITOR_ID;
		case MidPointPreferencePage.VALUE_ALWAYS:
			return LOGVIEWER_ID;
		default:
			return DEFAULT_EDITOR_ID;
		}
	}

	public static String getTextEditorId() {
		return DEFAULT_EDITOR_ID;
	}

	public static List<SourceObject> getServerObjectsFromSelection(ExecutionEvent event, ISelection selection) {
		List<SourceObject> rv = new ArrayList<>();
		String selectedText;
		if (selection instanceof ITextSelection) {
			ITextSelection ts  = (ITextSelection) selection;
			selectedText = ts.getText();
		} else if (selection instanceof IMarkSelection) {
			IMarkSelection ms = (IMarkSelection) selection;
			try {
			    selectedText = ms.getDocument().get(ms.getOffset(), ms.getLength());
			} catch (BadLocationException e) { 
				Util.processUnexpectedException(e);
				return rv;
			}
		} else if (selection instanceof IStructuredSelection) {
			List<IFile> files = SelectionUtils.getXmlFiles((IStructuredSelection) selection);
			if (files.isEmpty()) {
				Util.showWarning("No files", "There are no XML files to be processed.");
				return rv;
			}
			return ServerRequestPack.fromWorkspaceFiles(files);			// TODO message if empty
		} else {
			Util.showWarning("No selection", "You have not selected any items to be processed.");
			return rv;
		}
	
		//System.out.println("Selected text=[" + selectedText + "]");
		if (selectedText == null || selectedText.isEmpty()) {		// note "no trim" here!
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			if (editor != null) {
				IEditorInput editorInput = editor.getEditorInput();
				IDocument doc = (IDocument)editor.getAdapter(IDocument.class);
				if (doc != null) {
					selectedText = doc.get();
					IFile file = editorInput instanceof FileEditorInput ? ((FileEditorInput) editorInput).getFile() : null;
					if (selectedText != null && !selectedText.trim().isEmpty()) {
						return ServerRequestPack.fromTextFragment(selectedText, file, true);
					}
				}
			}
		}
		if (selectedText == null || selectedText.trim().isEmpty()) {
			return rv;
		}
		return ServerRequestPack.fromTextFragment(selectedText, null, false);		// TODO wholeFile	
	}

}
