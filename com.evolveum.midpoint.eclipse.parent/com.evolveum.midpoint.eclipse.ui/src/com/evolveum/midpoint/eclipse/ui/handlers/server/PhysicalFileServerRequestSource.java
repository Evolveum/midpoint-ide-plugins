package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.ui.util.Util;

public class PhysicalFileServerRequestSource extends ServerRequestSource {

	private final String filename;				// not null
	private final IFile file;					// might be null
	
	public PhysicalFileServerRequestSource(String filename) {
		this.filename = filename;
		this.file = Util.physicalToLogicalFile(filename);
	}
	
	@Override
	public String getDisplayName() {
		return filename;
	}

	@Override
	public String resolve() {
		InputStream is = null;
		try {
			String charset = file != null ? file.getCharset() : "utf-8";
			System.out.println("Charset for " + file + " is: " + charset);
			is = new FileInputStream(filename);
			return IOUtils.toString(is, charset);
		} catch (CoreException | IOException e) {
			Util.processUnexpectedException(e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	public IPath getPath() {
		return file != null ? file.getFullPath() : null;
	}

	@Override
	public String toString() {
		return "PhysicalFileServerRequestSource [filename=" + filename + ", file=" + file + "]";
	}

}
