package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;

import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.CompareServerResponse;
import com.evolveum.midpoint.eclipse.ui.handlers.ResourceUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.MidPointPreferencePage;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.HyperlinksRegistry;

public class CompareServerResponseItem extends ServerResponseItem<CompareServerResponse> {
	
	public static final String OUTPUT_TYPE_LOCAL_TO_REMOTE = "local-to-remote.xml";
	public static final String OUTPUT_TYPE_REMOTE_TO_LOCAL = "remote-to-local.xml";
	public static final String OUTPUT_TYPE_LOCAL = "local.xml";
	public static final String OUTPUT_TYPE_REMOTE = "remote.xml";
	
	private IFile localToRemoteFile;
	private IFile remoteToLocalFile;
	private IFile localFile;
	private IFile remoteFile;

	public CompareServerResponseItem(ServerRequestItem item, ServerRequest request, CompareServerResponse response) {
		super(item, request, response);
	}
	
	@Override
	public void prepareFileNames(int responseCounter) {
		localToRemoteFile = prepareOutputFileForCreation(responseCounter, OUTPUT_TYPE_LOCAL_TO_REMOTE);
		remoteToLocalFile = prepareOutputFileForCreation(responseCounter, OUTPUT_TYPE_REMOTE_TO_LOCAL);
		localFile = prepareOutputFileForCreation(responseCounter, OUTPUT_TYPE_LOCAL);
		remoteFile = prepareOutputFileForCreation(responseCounter, OUTPUT_TYPE_REMOTE);
	}

	@Override
	protected String getFileNamePattern() {
		return PluginPreferences.getCompareResultFileNamePattern();
	}
	
	@Override
	protected String getFileNamePatternNoSource() {
		return null;
	}

	@Override
	protected String getRootSpecification() {
		return PluginPreferences.getCompareResultRootDirectory();
	}

	@Override
	protected Collection<IFile> getFiles() {
		return Arrays.asList(localToRemoteFile, remoteToLocalFile, localFile, remoteFile);
	}

	@Override
	public void createFiles() {
		if (response.getLocalToRemote() != null) {
			ResourceUtils.createOutputFile(localToRemoteFile, response.getLocalToRemote());
		}
		if (response.getRemoteToLocal() != null) {
			ResourceUtils.createOutputFile(remoteToLocalFile, response.getRemoteToLocal());
		}
		if (response.getLocal() != null) {
			ResourceUtils.createOutputFile(localFile, response.getLocal());
		}
		if (response.getRemote() != null) {
			ResourceUtils.createOutputFile(remoteFile, response.getRemote());
		}
	}
	
	@Override
	public String getConsoleLogLine(int responseCounter) {
		String prefix = getResultLine();
		List<String> labels = new ArrayList<>();
		List<IFile> files = new ArrayList<>();
		List<String> editorIds = new ArrayList<>();
		if (response.getLocalToRemote() != null) {
			labels.add("local-to-remote");
			files.add(localToRemoteFile);
			//editorIds.add(FileRequestHandler.getTextEditorId());
			editorIds.add(null);
		}
		if (response.getRemoteToLocal() != null) {
			labels.add("remote-to-local");
			files.add(remoteToLocalFile);
			//editorIds.add(FileRequestHandler.getTextEditorId());
			editorIds.add(null);
		}
		if (response.getLocal() != null) {
			labels.add("local version (normalized)");
			files.add(localFile);
			//editorIds.add(FileRequestHandler.getTextEditorId());
			editorIds.add(null);
		}
		if (response.getRemote() != null) {
			labels.add("remote version");
			files.add(remoteFile);
			//editorIds.add(FileRequestHandler.getTextEditorId());
			editorIds.add(null);
		}
		
		String counterString = ResourceUtils.formatActionCounter(responseCounter);
		
		String seeText;
		if (!labels.isEmpty()) {
			seeText = " [see " + StringUtils.join(labels, "; ") + "]";
			HyperlinksRegistry.getInstance().registerEntry(counterString, labels, files, editorIds);
		} else {
			seeText = "";
		}
		
		return prefix + seeText + " (#" + counterString + ")";
	}

	@Override
	public String getResultLine() {
		String itemName = requestItem.getDisplayName() != null ? requestItem.getDisplayName() : "the item";
		String prefix;
		if (response.isSuccess()) {
			prefix = "Successfully compared " + itemName; 			// TODO comparison result
			if (response.getRemoteExists() != null) {
				if (!response.getRemoteExists()) {
					prefix += " (remote does not exist)";
				} else if (response.getItemDifferencesCount() != null) {
					if (response.getItemDifferencesCount() > 0) {
						prefix += " (differences: " + response.getItemDifferencesCount() + ")";
					} else {
						prefix += " (equal)";
					}
				}
			}
		} else {
			prefix = "Failed to compare " + itemName + ": " + response.getErrorDescription();
		}
		return prefix;
	}
	
	@Override
	public boolean showResultLine(String when) {
		if (MidPointPreferencePage.VALUE_ALWAYS.equals(when)) {
			return true;
		} else if (MidPointPreferencePage.VALUE_NEVER.equals(when)) {
			return false;
		} else if (MidPointPreferencePage.VALUE_WHEN_ERRORS.equals(when)) {
			return !isSuccess();
		} else if (MidPointPreferencePage.VALUE_WHEN_DIFFERENCES_OR_ERRORS.equals(when)) {
			return !isSuccess() || !response.noDifferences();
		} else {
			return false;		// shouldn't occur
		}
	}
	
	@Override
	public void logResult(int responseCounter) {
		String logLine = getConsoleLogLine(responseCounter);
		if (response.isSuccess()) {
			if (response.noDifferences()) {
				Console.logMinor(logLine);
			} else {
				Console.log(logLine);
			}
		} else {
			Console.logError(logLine, response.getException());
		}
		if (!response.isSuccess() && response.getStatusCode() != 0 && !response.wasParsed()) {
			logRawErrorDetails();
		}
	}


}
