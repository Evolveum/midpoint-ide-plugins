package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;

public abstract class Generator {

	public abstract String getLabel();
	public abstract boolean isExecutable();
	public abstract String generate(List<ServerObject> objects);
	
}
