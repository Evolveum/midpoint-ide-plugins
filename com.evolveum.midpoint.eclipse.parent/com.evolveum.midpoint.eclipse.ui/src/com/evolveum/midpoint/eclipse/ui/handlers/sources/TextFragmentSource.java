package com.evolveum.midpoint.eclipse.ui.handlers.sources;

import org.eclipse.core.runtime.IPath;

public class TextFragmentSource extends Source {

	private final String text;			// not null
	private final IPath path;			// optional (to know display name)
	
	public TextFragmentSource(String text, IPath path) {
		this.text = text;
		this.path = path;
	}
	
	@Override
	public String resolve() {
		return text;
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
