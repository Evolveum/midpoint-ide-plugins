package com.evolveum.midpoint.eclipse.ui.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class EclipseActivator extends AbstractUIPlugin {

	private static EclipseActivator INSTANCE;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		INSTANCE = this;
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		INSTANCE = null;
		super.stop(context);
	}
	
	public static EclipseActivator getInstance() {
		return INSTANCE;
	}

	
}
