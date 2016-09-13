package com.evolveum.midpoint.eclipse.ui.handlers.sources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;

public class SourceObject {

	private String content;						// XML content
	private ObjectTypes type;
	private boolean executable;
	private IFile file;					// used to derive output file names (and to delete files)
	private int objectIndex;					// object number in the resource (if applicable)
	private String displayName;					// how to identify object in messages, logs, etc.
	private boolean root;						// is this a root element in the file?
	private boolean last;						// is this a last one in the file?
	private boolean wholeFile;				// covers the whole file?
	private String oid;
	private String name;
	
	public SourceObject(String content, ObjectTypes type, boolean executable) {
		this.content = content;
		this.type = type;
		this.executable = executable;
	}
	
	public IFile getFile() {
		return file;
	}
	public void setFile(IFile resourcePath) {
		this.file = resourcePath;
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
		return root;
	}

	public void setRoot(boolean isRoot) {
		this.root = isRoot;
	}

	public boolean isUploadable() {
		return type != null;
	}

	public ObjectTypes getType() {
		return type;
	}

	public boolean isExecutable() {
		return executable;
	}

	public void setExecutable(boolean executable) {
		this.executable = executable;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public boolean isWholeFile() {
		return wholeFile;
	}

	public void setWholeFile(boolean wholeFile) {
		this.wholeFile = wholeFile;
	}

	public void setType(ObjectTypes type) {
		this.type = type;
	}
	
	

	
}
