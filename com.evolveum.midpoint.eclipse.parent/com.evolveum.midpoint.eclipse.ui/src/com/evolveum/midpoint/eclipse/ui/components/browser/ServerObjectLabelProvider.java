package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;

import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;

public class ServerObjectLabelProvider extends LabelProvider {
	public String getText(Object element) {
		if (element instanceof ServerObject) {
			ServerObject o = (ServerObject) element;
			return o.getName() + " (" + o.getOid() + ")";
		} else if (element instanceof Map.Entry) {
			Map.Entry<ObjectTypes,List<ServerObject>> entry = (Map.Entry<ObjectTypes,List<ServerObject>>) element;
			return entry.getKey().getDisplayName() + ": " + entry.getValue().size() + " object(s)";
		} else if (element instanceof ObjectTypes) {
			return ((ObjectTypes) element).getElementName();
		} else {
			return element.getClass() + ": " + element;
		}
	}
}