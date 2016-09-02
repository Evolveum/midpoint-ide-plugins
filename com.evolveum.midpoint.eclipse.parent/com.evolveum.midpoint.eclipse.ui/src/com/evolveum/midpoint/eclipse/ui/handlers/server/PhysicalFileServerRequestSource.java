package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.ui.util.Util;

public class PhysicalFileServerRequestSource extends ServerRequestSource {

	private final String filename;				// not null
	private final IPath path;					// might be null
	
	public PhysicalFileServerRequestSource(String filename) {
		this.filename = filename;
		this.path = Util.physicalToLogicalPath(filename);
	}
	
	@Override
	public String getDisplayName() {
		return filename;
	}

	@Override
	public byte[] resolve() {
		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filename));
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int data;
			while ((data = bis.read()) != -1) {
				buffer.write(data);
			}
			bis.close();
			buffer.flush();
			return buffer.toByteArray();
		} catch (IOException e) {
			Util.processUnexpectedException(e);		// TODO file not found might be quite frequent error
			return null;
		}
	}

	@Override
	public IPath getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "PhysicalFileServerRequestSource [filename=" + filename + ", path=" + path + "]";
	}

}
