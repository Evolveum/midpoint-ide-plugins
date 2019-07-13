package com.evolveum.midpoint.eclipse.ui.components.tracer;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class ViewedObject {
	private String label;
	private PrismObject<?> object;
	
	public ViewedObject(String label, PrismObject<? extends ObjectType> object) {
		this.label = label;
		this.object = object;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public PrismObject<?> getObject() {
		return object;
	}
	public void setObject(PrismObject<?> object) {
		this.object = object;
	}
	
	
}
