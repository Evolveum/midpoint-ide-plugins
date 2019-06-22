package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.util.HashSet;
import java.util.Set;

public class ViewOptions {
	
	private final Set<OpType> showOperationTypes = new HashSet<>();

	public Set<OpType> getShowOperationTypes() {
		return showOperationTypes;
	}

	public void show(OpType... types) {
		for (OpType type : types) {
			showOperationTypes.add(type);			
		}
	}


}
