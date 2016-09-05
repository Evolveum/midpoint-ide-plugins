package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.resources.IFile;

import com.evolveum.midpoint.eclipse.runtime.api.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.ServerResponse;

public class UploadServerResponseItem extends ServerResponseItem<ServerResponse> {

	public UploadServerResponseItem(ServerRequestItem item, ServerRequest request, ServerResponse response) {
		super(item, request, response);
	}

	@Override
	public void prepareFileNames(int responseCounter) {
	}

	@Override
	protected String getFileNamePattern() {
		return null;
	}

	@Override
	protected String getFileNamePatternNoSource() {
		return null;
	}

	@Override
	protected String getRootSpecification() {
		return null;
	}

	@Override
	protected Collection<IFile> getFiles() {
		return Collections.emptyList();
	}

	@Override
	public void createFiles() {
	}
	
	@Override
	public String getConsoleLogLine(int responseCounter) {
		String itemName = requestItem.getDisplayName() != null ? requestItem.getDisplayName() : "the item";
		if (response.isSuccess()) {
			return "Successfully uploaded " + itemName; 
		} else {
			return "Failed to uploaded " + itemName;
		}
	}


}
