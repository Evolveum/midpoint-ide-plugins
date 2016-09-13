package com.evolveum.midpoint.eclipse.ui.handlers.sources;

import org.eclipse.core.runtime.IPath;

public class SourceObject {

	private String content;						// XML content
	private boolean uploadable;
	private boolean executable;
	private IPath resourcePath;					// used to derive output file names
	private int objectIndex;					// object number in the resource (if applicable)
	private String displayName;					// how to identify object in messages, logs, etc.
	private boolean isRoot;						// is this a root element in the file?
	
	public SourceObject(String content, boolean uploadable, boolean executable) {
		this.content = content;
		this.uploadable = uploadable;
		this.executable = executable;
	}
	
	public IPath getResourcePath() {
		return resourcePath;
	}
	public void setResourcePath(IPath resourcePath) {
		this.resourcePath = resourcePath;
	}
	public int getObjectIndex() {
		return objectIndex;
	}
	public void setObjectIndex(int objectIndex) {
		this.objectIndex = objectIndex;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public boolean isUploadable() {
		return uploadable;
	}

	public void setUploadable(boolean uploadable) {
		this.uploadable = uploadable;
	}

	public boolean isExecutable() {
		return executable;
	}

	public void setExecutable(boolean executable) {
		this.executable = executable;
	}
	
	
	
}
