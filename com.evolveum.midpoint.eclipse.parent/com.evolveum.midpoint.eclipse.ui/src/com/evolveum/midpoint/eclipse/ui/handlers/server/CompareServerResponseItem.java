package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;

import com.evolveum.midpoint.eclipse.runtime.api.CompareServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.ServerRequest;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.HyperlinksRegistry;

public class CompareServerResponseItem extends ServerResponseItem<CompareServerResponse> {
	
	public static final String OUTPUT_TYPE_LOCAL_TO_REMOTE = "local-to-remote";
	public static final String OUTPUT_TYPE_REMOTE_TO_LOCAL = "remote-to-local";
	public static final String OUTPUT_TYPE_LOCAL = "local";
	public static final String OUTPUT_TYPE_REMOTE = "remote";
	
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
			createOutputFile(localToRemoteFile, response.getLocalToRemote());
		}
		if (response.getRemoteToLocal() != null) {
			createOutputFile(remoteToLocalFile, response.getRemoteToLocal());
		}
		if (response.getLocal() != null) {
			createOutputFile(localFile, response.getLocal());
		}
		if (response.getRemote() != null) {
			createOutputFile(remoteFile, response.getRemote());
		}
	}

	@Override
	public String getConsoleLogLine(int responseCounter) {
		String itemName = requestItem.getDisplayName() != null ? requestItem.getDisplayName() : "the item";
		String prefix;
		if (response.isSuccess()) {
			prefix = "Successfully compared " + itemName; 			// TODO comparison result
		} else {
			prefix = "Failed to compare " + itemName;
		}
		List<String> labels = new ArrayList<>();
		List<IFile> files = new ArrayList<>();
		List<String> editorIds = new ArrayList<>();
		if (response.getLocalToRemote() != null) {
			labels.add("local-to-remote");
			files.add(localToRemoteFile);
			editorIds.add(FileRequestHandler.getTextEditorId());
		}
		if (response.getRemoteToLocal() != null) {
			labels.add("remote-to-local");
			files.add(remoteToLocalFile);
			editorIds.add(FileRequestHandler.getTextEditorId());
		}
		if (response.getLocal() != null) {
			labels.add("local version (normalized)");
			files.add(localFile);
			editorIds.add(FileRequestHandler.getTextEditorId());
		}
		if (response.getRemote() != null) {
			labels.add("remote version");
			files.add(remoteFile);
			editorIds.add(FileRequestHandler.getTextEditorId());
		}
		
		String counterString = formatResponseCounter(responseCounter);
		
		HyperlinksRegistry.getInstance().registerEntry(counterString, labels, files, editorIds);
		return prefix + " [see " + StringUtils.join(labels, "; ") + "] (#" + counterString + ")";
	}

}
