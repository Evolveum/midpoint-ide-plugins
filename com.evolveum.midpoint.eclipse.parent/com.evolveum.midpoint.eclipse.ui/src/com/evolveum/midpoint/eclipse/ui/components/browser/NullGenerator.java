package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;

public class NullGenerator extends Generator {

	@Override
	public String getLabel() {
		return "";
	}

	@Override
	public String generate(List<ServerObject> objects, GeneratorOptions options) {
		return "";
	}

}
