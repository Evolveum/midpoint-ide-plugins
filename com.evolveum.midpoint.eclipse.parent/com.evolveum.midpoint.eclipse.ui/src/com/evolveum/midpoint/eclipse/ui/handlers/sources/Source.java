package com.evolveum.midpoint.eclipse.ui.handlers.sources;

import org.eclipse.core.runtime.IPath;

public abstract class Source {

	public abstract IPath getPath();
	public abstract String getDisplayName();
	public abstract String resolve();
	
}
