package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerResponse;
import com.evolveum.midpoint.eclipse.ui.handlers.ResourceUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.MidPointPreferencePage;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;

// TODO find a better name
public abstract class ServerResponseItem<SR extends ServerResponse> {

	private static final String COUNTER_SYMBOL = "$n";
	protected ServerRequestItem requestItem;
	protected ServerRequest request;
	protected SR response;
	
	public ServerResponseItem(ServerRequestItem requestItem, ServerRequest request, SR response) {
		super();
		this.requestItem = requestItem;
		this.request = request;
		this.response = response;
	}

	public ServerRequestItem getRequestItem() {
		return requestItem;
	}

	public ServerRequest getRequest() {
		return request;
	}

	public SR getResponse() {
		return response;
	}

	public abstract void prepareFileNames(int responseCounter);
	
	protected IFile prepareOutputFileForCreation(int responseCounter, String outputType) {
		IPath path = computeFilePath(responseCounter, outputType);
		System.out.println("Path = " + path);
		if (path == null) {
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}
	
	private IPath computeFilePath(int responseCounter, String outputType) {
		IPath source = requestItem.getSourcePath();
		IPath root, rootToSource;
		String sourceName;
		String pattern;
		if (source != null) {
			root = determineRoot(source);
			rootToSource = source.makeRelativeTo(root);
			sourceName = source.lastSegment();
			pattern = getFileNamePattern();
		} else {
			root = rootToSource = null;
			sourceName = "";
			pattern = getFileNamePatternNoSource();
		}
		System.out.println("source="+source+", root="+root+", rootToSource="+rootToSource+", sourceName="+sourceName+", pattern="+pattern);
		
		if (pattern == null || pattern.trim().isEmpty()) {
			return null;
		}
		
		String patternResolved = pattern
				.replace("$f", sourceName)
				.replace("$F", rootToSource.toPortableString())
				.replace(COUNTER_SYMBOL, formatActionCounter(responseCounter))
				.replace("$t", DownloadHandler.fixComponent(outputType))
				.replace("$s", DownloadHandler.fixComponent(PluginPreferences.getSelectedServerShortName())); 

		System.out.println("patternResolved = " + patternResolved);
		IPath resolvedPath = new Path(patternResolved);
		if (root != null && !resolvedPath.isAbsolute()) {
			resolvedPath = root.append(resolvedPath);
		}
		System.out.println("Final result = " + resolvedPath);
		return resolvedPath;
	}
	
	public static String formatActionCounter(int counter) {
		return String.format("%05d", counter);
	}

	private IPath determineRoot(IPath source) {
		String rootSpec = getRootSpecification();
		return ResourceUtils.determineRoot(source, rootSpec);
	}

	protected abstract String getFileNamePattern();
	protected abstract String getFileNamePatternNoSource();
	protected abstract String getRootSpecification();


	public boolean fileConflictsPresent() {
		String pattern = getFileNamePattern();
		if (pattern == null || !pattern.contains(COUNTER_SYMBOL)) {
			removeFiles();
			return false;
		} else {
			return conflictingFileExists();
		}
	}

	private boolean conflictingFileExists() {
		for (IFile file : getFiles()) {
			if (file != null && file.exists()) {
				return true;
			}
		}
		return false;
	}

	private void removeFiles() {
		for (IFile file : getFiles()) {
			if (file.exists()) {
				try {
					file.delete(true, null);
				} catch (CoreException e) {
					Console.logWarning("Couldn't delete file " + file, e);
				}
			}
		}
	}

	protected abstract Collection<IFile> getFiles();

	public abstract void createFiles();

	public void openFileIfNeeded() {
	}

	public abstract String getConsoleLogLine(int responseCounter);
	
	public abstract String getResultLine();
	
	public void logResult(int responseCounter) {
		String logLine = getConsoleLogLine(responseCounter);
		if (response.isSuccess()) {
			Console.log(logLine);
		} else {
			Console.logError(logLine, response.getException());
		}
	}
	
	protected void logRawErrorDetails() {
		Console.logError("Status: " + response.getStatusCode() + " " + response.getReasonPhrase());
		if (response.getRawResponseBody() != null) {
			Console.logWarning("Server response body:");
			Console.logWarning(response.getRawResponseBody().trim());
			Console.logWarning("-----------------------------");
		}
	}

	public boolean isSuccess() {
		return response.isSuccess();
	}

	public boolean showResultLine(String when) {
		if (MidPointPreferencePage.VALUE_ALWAYS.equals(when)) {
			return true;
		} else if (MidPointPreferencePage.VALUE_NEVER.equals(when)) {
			return false;
		} else {
			return !isSuccess();
		}
	}

}
