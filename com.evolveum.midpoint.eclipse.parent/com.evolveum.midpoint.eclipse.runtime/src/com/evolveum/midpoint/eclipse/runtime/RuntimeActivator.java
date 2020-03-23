package com.evolveum.midpoint.eclipse.runtime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xml.sax.SAXException;

import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.impl.RuntimeServiceImpl;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.schema.MidPointPrismContextFactory;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;

public class RuntimeActivator implements BundleActivator {

	private static BundleContext context;
	private static RuntimeActivator instance;
	private static RuntimeService runtimeService;
	private static PrismContext prismContext;
	
	public RuntimeActivator() {
		super();
		instance = this;
		try {
			System.out.println("CURRENT DIR = " + new File(".").getAbsolutePath());
			prismContext = new MidPointPrismContextFactory(new File("schema")).createPrismContext();
			prismContext.initialize();
			System.out.println("Prism context created and initialized");
		} catch (SchemaException | SAXException | IOException e) {
			throw new SystemException("Couldn't initialize prismContext: " + e.getMessage(), e);
		}
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

	public static PrismContext getPrismContext() {
		return prismContext;
	}
}
