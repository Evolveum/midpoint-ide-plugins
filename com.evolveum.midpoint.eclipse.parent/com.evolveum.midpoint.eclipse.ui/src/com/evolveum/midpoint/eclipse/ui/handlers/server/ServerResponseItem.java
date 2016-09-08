package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.evolveum.midpoint.eclipse.runtime.api.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.ServerResponse;
import com.evolveum.midpoint.eclipse.ui.prefs.MidPointPreferencePage;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Util;

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
				.replace(COUNTER_SYMBOL, formatResponseCounter(responseCounter))
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
	
	

	protected String formatResponseCounter(int responseCounter) {
		return String.format("%05d", responseCounter);
	}

	private IPath determineRoot(IPath source) {
		String rootSpec = getRootSpecification();
		if (StringUtils.isBlank(rootSpec) || MidPointPreferencePage.VALUE_CURRENT_DIRECTORY.equals(rootSpec)) {
			return goUp(source, 1);
		} else if (MidPointPreferencePage.VALUE_CURRENT_DIRECTORY_PLUS_1.equals(rootSpec)) {
			return goUp(source, 2);
		} else if (MidPointPreferencePage.VALUE_CURRENT_DIRECTORY_PLUS_2.equals(rootSpec)) {
			return goUp(source, 3);
		} else if (MidPointPreferencePage.VALUE_CURRENT_DIRECTORY_PLUS_3.equals(rootSpec)) {
			return goUp(source, 4);
		} 
		int keep;
		if (MidPointPreferencePage.VALUE_CURRENT_PROJECT.equals(rootSpec)) {
			keep = 1;
		} else if (MidPointPreferencePage.VALUE_CURRENT_PROJECT_MINUS_1.equals(rootSpec)) {
			keep = 2;
		} else if (MidPointPreferencePage.VALUE_CURRENT_PROJECT_MINUS_2.equals(rootSpec)) {
			keep = 3;
		} else if (MidPointPreferencePage.VALUE_CURRENT_PROJECT_MINUS_3.equals(rootSpec)) {
			keep = 4;
		} else {
			throw new IllegalStateException("Invalid root specification: " + rootSpec);
		}
		if (source.segmentCount() <= keep) {
			keep = source.segmentCount()-1;
		}
		return source.uptoSegment(keep);
	}

	private IPath goUp(IPath source, int levels) {
		while (source.segmentCount() > 1 && levels > 0) {
			source = source.removeLastSegments(1);
			levels--;
		}
		return source;
	}

	protected abstract String getFileNamePattern();
	protected abstract String getFileNamePatternNoSource();
	protected abstract String getRootSpecification();


	protected IFile createOutputFile(IFile file, byte[] content) {
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


	protected IFile createOutputFile(IFile file, String content) {
		return createOutputFile(file, content != null ? content.getBytes() : null);
	}

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
