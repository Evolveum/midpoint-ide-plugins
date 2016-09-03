package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IMarkSelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.ExecuteActionServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.ServerAction;
import com.evolveum.midpoint.eclipse.runtime.api.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.ServerResponse;
import com.evolveum.midpoint.eclipse.ui.prefs.MidPointPreferencePage;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class UploadOrExecuteHandler extends AbstractHandler {
	
	public static final String OUTPUT_TYPE_LOG = "log";
	public static final String OUTPUT_TYPE_DATA = "data";
	public static final String OUTPUT_TYPE_CONSOLE = "console";
	public static final String OUTPUT_TYPE_RESULT = "result";
	public static final String CMD_UPLOAD_OR_EXECUTE = "com.evolveum.midpoint.eclipse.ui.command.uploadOrExecute";
	public static final String CMD_EXECUTE_ACTION = "com.evolveum.midpoint.eclipse.ui.command.executeAction";
	public static final String CMD_COMPUTE_DIFFERENCE = "com.evolveum.midpoint.eclipse.ui.command.computeDifference";

	public static final String PARAM_WITH_ACTION = "com.evolveum.midpoint.eclipse.ui.commandParameter.withAction";
	public static final String PARAM_ACTION_NUMBER = "com.evolveum.midpoint.eclipse.ui.commandParameter.actionNumber";
	
	public static final String SERVER_LOG = "Server log"; 
	public static final String DATA_OUTPUT = "Data output"; 
	public static final String CONSOLE_OUTPUT = "Console output"; 
	public static final String OP_RESULT = "Operation result"; 
	public static final String CONSOLE_REFERENCE_TEXT = "[see Server log; Data output; Console output; Operation result]";
	
	enum RequestedAction { UPLOAD_OR_EXECUTE, UPLOAD_OR_EXECUTE_WITH_ACTION, EXECUTE_ACTION };
	
	private int responseCounter = 1;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		RequestedAction action;
		
		Command cmd = event.getCommand();
		if (CMD_UPLOAD_OR_EXECUTE.equals(cmd.getId())) {
			if ("true".equals(event.getParameter(PARAM_WITH_ACTION))) {
				action = RequestedAction.UPLOAD_OR_EXECUTE_WITH_ACTION;
			} else {
				action = RequestedAction.UPLOAD_OR_EXECUTE;
			}
		} else if (CMD_EXECUTE_ACTION.equals(cmd.getId())) {
			action = RequestedAction.EXECUTE_ACTION;
		} else if (CMD_COMPUTE_DIFFERENCE.equals(cmd.getId())) {
			Util.showAndLogWarning("Not implemented yet", "Difference computation is not yet implemented.");
			return null;
		} else {
			throw new IllegalArgumentException("Unsupported command id: " + cmd.getId());
		}
		
		ServerRequestPack requestPack;
		switch (action) {
		case EXECUTE_ACTION:
			requestPack = createRequestForPredefinedAction(event.getParameter(PARAM_ACTION_NUMBER));
			break;
		case UPLOAD_OR_EXECUTE:
			requestPack = createRequestPackFromSelection(event, action);
			break;
		case UPLOAD_OR_EXECUTE_WITH_ACTION:
			requestPack = createRequestPackFromSelection(event, action);
			if (requestPack.isEmpty()) {
				return null;
			}
			String actionAfterUpload = PluginPreferences.getActionAfterUpload();
			if (actionAfterUpload != null && !actionAfterUpload.isEmpty()) {
				requestPack.add(createRequestForPredefinedAction(actionAfterUpload).getItems());
			}
			break;
		default:
			throw new AssertionError();
		}
		
		System.out.println("Server request pack: " + requestPack);
		if (!requestPack.isEmpty()) {
			executeServerRequest(action, requestPack);
		}
		return null;
	}

	private ServerRequestPack createRequestForPredefinedAction(String actionNumber) {
		String fileName = PluginPreferences.getActionFile(actionNumber);
		if (fileName == null || fileName.isEmpty()) {
			Util.showAndLogWarning("No file for action", "Action #" + actionNumber + " has no file defined.");
			return ServerRequestPack.EMPTY;
		} else if (!new File(fileName).exists()) {
			Util.showAndLogWarning("No file for action", "File for action #" + actionNumber + "(" + fileName + ") does not exist or is not readable.");
			return ServerRequestPack.EMPTY;
		} else {
			return new ServerRequestPack(new ServerRequestItem(ServerAction.EXECUTE, new PhysicalFileServerRequestSource(fileName), actionNumber));
		}
	}

	private ServerRequestPack createRequestPackFromSelection(ExecutionEvent event, RequestedAction action) {
		ISelectionService selectionService = HandlerUtil.getActiveWorkbenchWindow(event).getSelectionService();
		ISelection selection = selectionService.getSelection();
		System.out.println("Current selection: " + selection.getClass());
		
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
			List<IFile> files = getXmlFiles((IStructuredSelection) selection);
			if (files.isEmpty()) {
				MessageDialog.openWarning(null, "No files to upload/execute", "There are no XML files to be uploaded or executed.");
				return ServerRequestPack.EMPTY;
			}
			return ServerRequestPack.fromWorkspaceFiles(files);
		} else {
			MessageDialog.openWarning(null, "No selection", "You have not selected any items to be uploaded or executed.");
			return ServerRequestPack.EMPTY;
		}

		//System.out.println("Selected text=[" + selectedText + "]");
		if (selectedText == null || selectedText.isEmpty()) {		// note "no trim" here!
			IEditorPart editor = HandlerUtil.getActiveEditor(event);
			IEditorInput editorInput = editor.getEditorInput();
			IDocument doc = (IDocument)editor.getAdapter(IDocument.class);
			if (doc != null) {
            	selectedText = doc.get();
            	IFile file = editorInput instanceof FileEditorInput ? ((FileEditorInput) editorInput).getFile() : null;
            	if (selectedText != null && !selectedText.trim().isEmpty()) {
            		IPath path = file != null ? file.getFullPath() : null;			// TODO what for files that are not in the workspace?
            		return ServerRequestPack.fromTextFragment(selectedText, path);
            	}
			}
		}
		if (selectedText == null || selectedText.trim().isEmpty()) {
			MessageDialog.openWarning(null, "No selection", "There is no content to be uploaded or executed.");
			return ServerRequestPack.EMPTY;
		}
		
		return ServerRequestPack.fromTextFragment(selectedText, null);
	}

	private void executeServerRequest(final RequestedAction requestedAction, ServerRequestPack uploadRequest) {
		
		String logfilename = PluginPreferences.getLogfile();
		
		Job job = new Job("Uploading/executing") {					// TODO more specific title
			protected IStatus run(IProgressMonitor monitor) {
				
				final int itemCount = uploadRequest.getItemCount();
				if (itemCount == 0) {
					// shouldn't get here
					return null;
				}
				
				ConnectionParameters connectionParameters = PluginPreferences.getConnectionParameters();

				int failCounter = 0, successCounter = 0;
				ServerRequestItem lastItem = null;
				ServerResponse lastErrorResponse = null;
				
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				monitor.beginTask("Processing", itemCount);
				for (ServerRequestItem item : uploadRequest.getItems()) {
					if (monitor.isCanceled()) {
						break;
					}
					lastItem = item;
					if (item.getDisplayName() != null) {
						monitor.subTask(item.getDisplayName());
					}
					
					long logPosition = getLogPosition(logfilename);
					
					ServerRequest request = new ServerRequest(item.getServerAction(), item.getContent());
					ServerResponse response = runtime.executeServerRequest(request, connectionParameters);

					IFile opResultFile = null; 
					IFile consoleFile = null; 
					IFile dataFile = null;
					IFile logFile = null;

					if (response instanceof ExecuteActionServerResponse) {
						ExecuteActionServerResponse actionResponse = (ExecuteActionServerResponse) response;
						
//						Console.log("Operation result status: " + actionResponse.getOperationResultStatus());
//						Console.log("Operation result message: " + actionResponse.getOperationResultMessage());
//						Console.log("Operation result: " + actionResponse.getOperationResult());
//						Console.log("Console output: " + actionResponse.getConsoleOutput());
//						Console.log("Data output: " + actionResponse.getDataOutput());
						
						// TODO check if $n is present (otherwise there is no point in iterating)
						
						boolean ok = false;
						for (int i = 0; i < 1000; i++) {
							opResultFile = prepareOutputFileForCreation(item, responseCounter, OUTPUT_TYPE_RESULT);
							consoleFile = prepareOutputFileForCreation(item, responseCounter, OUTPUT_TYPE_CONSOLE);
							dataFile = prepareOutputFileForCreation(item, responseCounter, OUTPUT_TYPE_DATA);
							logFile = prepareOutputFileForCreation(item, responseCounter, OUTPUT_TYPE_LOG);
							if (!opResultFile.exists() && !consoleFile.exists() && !dataFile.exists() && !logFile.exists()) {
								ok = true;
								break;
							}
							responseCounter++;
						}
						if (!ok) {
							throw new IllegalStateException("No free file name even after 1000 iterations");	// TODO
						}
							
						opResultFile = createOutputFile(opResultFile, actionResponse.getOperationResult());
						consoleFile = createOutputFile(consoleFile, actionResponse.getConsoleOutput());
						dataFile = createOutputFile(dataFile, actionResponse.getDataOutput());
						logFile = createOutputFile(logFile, getLogFileFragment(logfilename, logPosition));

						String predefinedActionNumber = item.getPredefinedActionNumber();
						String openAfter;
						if (StringUtils.isNotBlank(predefinedActionNumber)) {
							openAfter = PluginPreferences.getActionOpenAfter(predefinedActionNumber);
						} else {
							openAfter = PluginPreferences.getActionOpenAfterOther();
						}
						System.out.println("Open after = " + openAfter);
						
						final IFile openFile;
						switch (openAfter) {
						case OUTPUT_TYPE_RESULT:
							openFile = opResultFile; break;
						case OUTPUT_TYPE_CONSOLE:
							openFile = consoleFile; break;
						case OUTPUT_TYPE_DATA:
							openFile = dataFile; break;
						case OUTPUT_TYPE_LOG:
							openFile = logFile; break;
						default:
							if (StringUtils.isNotBlank(openAfter)) {
								Console.logWarning("Unknown value for 'open after' parameter: " + openAfter);
								openAfter = null;
							}
							openFile = null;
						}
						final boolean isLogFile = OUTPUT_TYPE_LOG.equals(openAfter);

						if (openFile != null) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
									try {
										if (isLogFile) {
											IDE.openEditor(page, openFile, getLogViewerEditorId());											
										} else {
											IDE.openEditor(page, openFile);
										}
									} catch (PartInitException e) {
										e.printStackTrace();
									}
								}
							});
						}

						responseCounter++;

					}

					String lineStart;
					if (response.isSuccess()) {
						String ue = response instanceof ExecuteActionServerResponse ? "executed" : "uploaded";
						lineStart = "Successfully "+ue+ " " + (item.getDisplayName() != null ? item.getDisplayName() : "the item");
					} else {
						String ue = response instanceof ExecuteActionServerResponse ? "execute" : "upload";
						lineStart = "Failed to " + ue + " " + (item.getDisplayName() != null ? item.getDisplayName() : "the item");
					}

					String refText;
					if (response instanceof ExecuteActionServerResponse) {
						refText = " " + CONSOLE_REFERENCE_TEXT;
						Console.registerHyperlink(lineStart + refText, logFile, dataFile, consoleFile, opResultFile);
					} else {
						refText = "";
					}
					if (response.isSuccess()) {
						Console.log(lineStart + refText);
						successCounter++;
					} else {
						Console.logError(lineStart + refText, response.getException());
						if (response.getStatusCode() != 0 && 
								!(response instanceof ExecuteActionServerResponse && ((ExecuteActionServerResponse) response).wasParsed())) {
							// parsed action execution responses are written to files
							Console.logError("Status: " + response.getStatusCode() + " " + response.getReasonPhrase());
							if (response.getRawResponseBody() != null) {
								Console.logWarning("Server response body:");
								Console.logWarning(response.getRawResponseBody().trim());
								Console.logWarning("-----------------------------");
							}
						}
						failCounter++;
						lastErrorResponse = response;
					}
					
					
					monitor.worked(1);
				}
				monitor.done();
				
				String showBox = PluginPreferences.getShowUploadOrExecuteResultMessageBox();
				
				if (itemCount == 1) {
					String ofWhat = lastItem.getDisplayName() != null ? "of " + lastItem.getDisplayName() + " " : "";
					if (successCounter > 0) {
						if (MidPointPreferencePage.VALUE_ALWAYS.equals(showBox)) {
							Util.showInformation("Success", "Upload/execution " + ofWhat + "finished successfully.");
						}
					} else {
						if (!MidPointPreferencePage.VALUE_NEVER.equals(showBox)) {
							Util.showError("Problem", "Upload/execution " + ofWhat + "failed: " + lastErrorResponse.getErrorDescription());
						}
					}
				} else {
					if (failCounter == 0) {
						String message = "Upload/execution of all " + successCounter + " items was successful.";
						Console.log(message);
						if (MidPointPreferencePage.VALUE_ALWAYS.equals(showBox)) {
							Util.showInformation("Success", message);
						}
					} else if (successCounter == 0) {
						String message = "Upload/execution of all " + failCounter + " items failed.";
						Console.logError(message);
						if (!MidPointPreferencePage.VALUE_NEVER.equals(showBox)) {
							Util.showError("Failure", message);
						}
					} else {
						String message = "Upload/execution successful for " + successCounter + " item(s), failed for " + failCounter + " one(s)";
						Console.logWarning(message);
						if (!MidPointPreferencePage.VALUE_NEVER.equals(showBox)) {
							Util.showWarning("Partial failure", message);
						}
					}
				}
				return Status.OK_STATUS;
			}

			private IFile prepareOutputFileForCreation(ServerRequestItem item, int responseCounter, String outputType) {
				IPath path = computeFilePath(item, responseCounter, outputType);
				System.out.println("Path = " + path);
				if (path == null) {
					return null;
				}
				return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			}

			private IFile createOutputFile(IFile file, byte[] content) {
				if (file == null || content == null) {
					return null;
				}
				try {
					InputStream source = new ByteArrayInputStream(content);
					prepare(file.getParent());
					file.create(source, true, null);
					return file;
				} catch (CoreException e) {
					Util.showAndLogError("Error creating output file", "Output file " + file.getFullPath() + "couldn't be created: " + e, e);
					return null;
				}
			}
			
			public void prepare(IContainer container) throws CoreException {
				if (!(container instanceof IFolder)) {
					return;
				}
				IFolder folder = (IFolder) container;
			    if (!folder.exists()) {
			        prepare(folder.getParent());
			        folder.create(true, true, null);
			    }
			}

			private IPath computeFilePath(ServerRequestItem item, int responseCounter, String outputType) {
				IPath prefix = null;
				String pattern;
				String sourceName;
				
				if (item.getSourcePath() != null) {
					pattern = PluginPreferences.getOutputFileNamePattern();
					sourceName = item.getSourcePath().lastSegment();
				} else {
					pattern = PluginPreferences.getOutputFileNamePatternNoSource();
					sourceName = "";
				}
				
				if (pattern == null || pattern.trim().isEmpty()) {
					return null;
				}
				
				String patternResolved = resolvePattern(pattern, sourceName, String.format("%05d", responseCounter), outputType);
				System.out.println("pattern = " + pattern + ", sourceName = " + sourceName + ", resolvedPattern = " + patternResolved);
				IPath resolvedPath = new Path(patternResolved);
				System.out.println("Resolved path = " + resolvedPath);
				if (item.getSourcePath() != null && !resolvedPath.isAbsolute()) {
					prefix = item.getSourcePath().removeLastSegments(1);
					resolvedPath = prefix.append(resolvedPath);
				}
				System.out.println("Final result = " + resolvedPath);
				return resolvedPath;
			}

			private String resolvePattern(String pattern, String sourceName, String counter, String outputType) {
				return pattern.replace("$f", sourceName).replace("$n", counter).replace("$t", outputType);
			}

			private IFile createOutputFile(IFile file, String content) {
				return createOutputFile(file, content != null ? content.getBytes() : null);
			}

			private byte[] getLogFileFragment(String logfilename, long logPosition) {
				if (StringUtils.isBlank(logfilename)) {
					return "Path to midPoint server log file (idm.log) is not specified. You can use Preferences page to enter it.".getBytes(); 
				}
				try {
					RandomAccessFile log = new RandomAccessFile(logfilename, "r");
					log.seek(logPosition);
					byte[] buffer = new byte[10240];
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int n;
					while ((n=log.read(buffer)) >= 0) {
						baos.write(buffer, 0, n);
					}
					log.close();
					baos.close();
					return baos.toByteArray();
				} catch (IOException e) {
					Console.logError("Couldn't read from midPoint log", e);
					return null;
				}
			}

		};
		job.schedule();
		
	}

	protected long getLogPosition(String logfilename) {
		File file = new File(logfilename);
		return file.length();
		
	}

	private List<IFile> getXmlFiles(IStructuredSelection selection) {
		List<IFile> files = new ArrayList<>();
		for (Object item : selection.toList()) {
			if (item instanceof IResource) {
				addXmlFiles(files, (IResource) item);
			}
		}
		return files;
	}

	private void addXmlFiles(List<IFile> files, IResource resource) {
		if (resource instanceof IContainer) {
			try {
				for (IResource member : ((IContainer) resource).members()) {
					addXmlFiles(files, member);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			if ("XML".equalsIgnoreCase(file.getFileExtension())) {
				files.add(file);
			}
		} else {
			// TODO issue warning
		}
	}
	
	public static String getLogViewerEditorId() {
		if (PluginPreferences.isUseMidPointLogViewer()) {
			return "com.evolveum.logviewer.editor.LogViewerEditor";
		} else {
			return "org.eclipse.ui.DefaultTextEditor";
		}
	}

	public static String getTextEditorId() {
		return "org.eclipse.ui.DefaultTextEditor";
	}

//	private String analyze(Object item) {
//		if (item instanceof IResource) {
//			IResource res = (IResource) item;
//			return getContent(res, 1);
//		} else {
//			return getContent(item, 1);
//		}
//	}
//
//	private String getContent(Object item, int level) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(indent(level) + item.getClass() + ": " + item.toString() + "\n");
//		if (!(item instanceof IParent)) {
//			return sb.toString();
//		}
//		
//		IParent parent = (IParent) item;
//		try {
//			for (IJavaElement child : parent.getChildren()) {
//				sb.append(getContent(child, level+1));
//			}
//		} catch (JavaModelException e) {
//			e.printStackTrace();
//		}
//		return sb.toString();
//	}
//
//	private String indent(int level) {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < level; i++) {
//			sb.append("  ");
//		}
//		sb.append("- ");
//		return sb.toString();
//	}
//
//	private String getContent(IResource item, int level) {
//		StringBuilder sb = new StringBuilder();
//		sb.append(indent(level) + item.getClass() + ": " + item.toString() + "\n");
//		if (!(item instanceof IContainer)) {
//			return sb.toString();
//		}
//		IContainer container = (IContainer) item;
//		try {
//			for (IResource child : container.members(true)) {
//				sb.append(getContent(child, level+1));
//			}
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//		return sb.toString();
//	}

}
