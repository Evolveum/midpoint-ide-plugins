package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.UnsupportedEncodingException;

import org.eclipse.core.runtime.IPath;

public class TextFragmentServerRequestSource extends ServerRequestSource {

	private final String text;			// not null
	private final IPath path;			// optional (to know display name)
	
	public TextFragmentServerRequestSource(String text, IPath path) {
		this.text = text;
		this.path = path;
	}
	
	@Override
	public byte[] resolve() {
		try {
			return text.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getDisplayName() {
		return path != null ? path.toPortableString() : null;
	}

	@Override
	public IPath getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "TextFragmentServerRequestSource [text=" + text + ", path=" + path + "]";
	}

}
