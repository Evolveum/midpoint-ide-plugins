package com.evolveum.midpoint.eclipse.ui.handlers.sources;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.ui.util.Util;

public class WorkspaceFileSource extends Source {

	private final IFile file;			// not null
	
	public WorkspaceFileSource(IFile file) {
		this.file = file;
	}

	@Override
	public String resolve() {
		InputStream is = null;
		try {
			String charset = file.getCharset();
			System.out.println("Charset for " + file + " is: " + charset);
			is = file.getContents();
			return IOUtils.toString(is, charset);
		} catch (CoreException | IOException e) {
			Util.processUnexpectedException(e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
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
