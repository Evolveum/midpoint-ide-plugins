package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;

public abstract class Generator {

	public static final Generator NULL_GENERATOR = new NullGenerator();
	public abstract String getLabel();
	public abstract boolean isExecutable();
	public abstract String generate(List<ServerObject> objects, GeneratorOptions options);

	public boolean supportsRawOption() {
		return false;
	}
	public boolean supportsSymbolicReferences() {
		return false;
	}
	
}
