package com.evolveum.midpoint.eclipse.runtime;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.impl.RuntimeServiceImpl;

public class RuntimeActivator implements BundleActivator {

	private static BundleContext context;
	private static RuntimeActivator instance;
	private static RuntimeService runtimeService;
	
	public RuntimeActivator() {
		super();
		instance = this;
		runtimeService = new RuntimeServiceImpl();
	}
	
	public static RuntimeActivator getInstance() {
		return instance;
	}
	
	public static RuntimeService getRuntimeService() {
		return runtimeService;
	}

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		RuntimeActivator.context = bundleContext;
	}

	public void stop(BundleContext bundleContext) throws Exception {
		RuntimeActivator.context = null;
	}
	
}
