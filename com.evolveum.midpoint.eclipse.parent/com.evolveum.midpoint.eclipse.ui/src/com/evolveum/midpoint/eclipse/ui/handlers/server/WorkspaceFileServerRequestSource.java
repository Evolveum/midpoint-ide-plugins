package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.ui.util.Util;

public class WorkspaceFileServerRequestSource extends ServerRequestSource {

	private final IFile file;			// not null
	
	public WorkspaceFileServerRequestSource(IFile file) {
		this.file = file;
	}

	@Override
	public byte[] resolve() {
		InputStream is = null;
		try {
			is = file.getContents();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int data;
			while ((data = bis.read()) != -1) {
				buffer.write(data);
			}
			buffer.flush();
			// items.add(new ServerRequestItem(action,
			// file.getFullPath().toOSString(),
			return buffer.toByteArray();
		} catch (CoreException | IOException e) {
			Util.processUnexpectedException(e);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	@Override
	public String getDisplayName() {
		return file.getFullPath().toPortableString();
	}

	@Override
	public IPath getPath() {
		return file.getFullPath();
	}

	@Override
	public String toString() {
		return "WorkspaceFileServerRequestSource [file=" + file + "]";
	}

}
