package com.evolveum.midpoint.eclipse.runtime;

import com.evolveum.midpoint.prism.PrismContext;

public class PrismContextHolder {

	public static PrismContext getPrismContext() {
		return RuntimeActivator.getPrismContext();
	}
}
