package com.evolveum.midpoint.eclipse.ui.handlers.server;

import org.eclipse.core.runtime.IPath;

public abstract class ServerRequestSource {

	public abstract IPath getPath();
	public abstract String getDisplayName();
	public abstract String resolve();
	
}
