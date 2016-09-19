package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ExecuteActionServerResponse;
import com.evolveum.midpoint.eclipse.ui.handlers.ResourceUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.ServerLogUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.ServerLogUtils.LogFileFragment;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.HyperlinksRegistry;

public class ExecuteActionResponseItem extends ServerResponseItem<ExecuteActionServerResponse> {

	public static final String OUTPUT_TYPE_LOG = "log";
	public static final String OUTPUT_TYPE_DATA = "data.xml";
	public static final String OUTPUT_TYPE_CONSOLE = "console.log";
	public static final String OUTPUT_TYPE_RESULT = "result.xml";

	private String logfilename;
	private Long logPosition;
	
	private IFile opResultFile = null; 
	private IFile consoleFile = null; 
	private IFile dataFile = null;
	private IFile logFile = null;
	
	private byte[] logFileFragment = null;

	public ExecuteActionResponseItem(ServerRequestItem item, ServerRequest request, ExecuteActionServerResponse response, String logfilename, Long logPosition) {
		super(item, request, response);
		this.logfilename = logfilename;
		this.logPosition = logPosition;
	}

	public IFile getOpResultFile() {
		return opResultFile;
	}

	public IFile getConsoleFile() {
		return consoleFile;
	}

	public IFile getDataFile() {
		return dataFile;
	}

	public IFile getLogFile() {
		return logFile;
	}

	@Override
	public void prepareFileNames(int responseCounter) {
		opResultFile = prepareOutputFileForCreation(responseCounter, OUTPUT_TYPE_RESULT);
		consoleFile = prepareOutputFileForCreation(responseCounter, OUTPUT_TYPE_CONSOLE);
		dataFile = prepareOutputFileForCreation(responseCounter, OUTPUT_TYPE_DATA);
		logFile = prepareOutputFileForCreation(responseCounter, OUTPUT_TYPE_LOG);
	}

	@Override
	protected String getFileNamePattern() {
		return PluginPreferences.getActionOutputFileNamePattern();
	}
	
	@Override
	protected String getFileNamePatternNoSource() {
		return PluginPreferences.getOutputFileNamePatternNoSource();
	}

	@Override
	protected String getRootSpecification() {
		return PluginPreferences.getActionOutputRootDirectory();
	}

	@Override
	protected Collection<IFile> getFiles() {
		return Arrays.asList(opResultFile, consoleFile, dataFile, logFile);
	}

	@Override
	public void createFiles() {
		ResourceUtils.createOutputFile(opResultFile, response.getOperationResult());
		ResourceUtils.createOutputFile(consoleFile, response.getConsoleOutput());
		ResourceUtils.createOutputFile(dataFile, response.getDataOutput());
		LogFileFragment lff = ServerLogUtils.getLogFileFragment(logfilename, logPosition, false);
		logFileFragment = lff != null ? lff.content : null;
		ResourceUtils.createOutputFile(logFile, logFileFragment);
	}
	
	@Override
	public void openFileIfNeeded() {
		int predefinedActionNumber = requestItem.getPredefinedActionNumber();
		String openAfter;
		if (predefinedActionNumber != 0) {
			openAfter = PluginPreferences.getActionOpenAfter(predefinedActionNumber);
		} else {
			openAfter = PluginPreferences.getActionOpenAfterOther();
		}
		System.out.println("Open after = " + openAfter);
		
		final IFile openFile;
		switch (openAfter) {
		case OUTPUT_TYPE_RESULT:
			openFile = getOpResultFile(); break;
		case OUTPUT_TYPE_CONSOLE:
			openFile = getConsoleFile(); break;
		case OUTPUT_TYPE_DATA:
			openFile = getDataFile(); break;
		case OUTPUT_TYPE_LOG:
			openFile = getLogFile(); break;
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
							IDE.openEditor(page, openFile, FileRequestHandler.getLogViewerEditorId(logFileFragment));											
						} else {
							IDE.openEditor(page, openFile);
						}
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			});
		}

	}

	@Override
	public String getConsoleLogLine(int responseCounter) {
		List<String> labels = Arrays.asList("Server log", "Data output", "Console output", "Operation result");
		List<IFile> files = Arrays.asList(logFile, dataFile, consoleFile, opResultFile);
		List<String> editorIds = Arrays.asList(FileRequestHandler.getLogViewerEditorId(logFileFragment), null, null, null);
		String counterString = ResourceUtils.formatActionCounter(responseCounter);

		HyperlinksRegistry.getInstance().registerEntry(counterString, labels, files, editorIds);
		return getResultLine() + " [see " + StringUtils.join(labels, "; ") + "] (#" + counterString + ")";
	}
	
	@Override
	public void logResult(int responseCounter) {
		super.logResult(responseCounter);
		if (!response.isSuccess() && response.getStatusCode() != 0 && !response.wasParsed()) {
			logRawErrorDetails();
		}
	}

	@Override
	public String getResultLine() {
		String itemName = requestItem.getDisplayName() != null ? requestItem.getDisplayName() : "the item";
		if (response.isSuccess()) {
			return "Successfully executed " + itemName; 
		} else {
			return "Failed to execute " + itemName + ": " + response.getErrorDescription();
		}
	}


}
