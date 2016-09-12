package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;

public class ServerObjectLabelProvider implements ITableLabelProvider {
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

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
	
	public static final int NAME = 0;
	public static final int DISPLAY_NAME = 1;
	public static final int SUBTYPE = 2;
	public static final int OID = 3;

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof ServerObject) {
			ServerObject o = (ServerObject) element;
			switch (columnIndex) {
			case NAME: return o.getName();
			case DISPLAY_NAME: return o.getDisplayName();
			case SUBTYPE: return StringUtils.join(o.getSubtypes(), ", ");
			case OID: return o.getOid();
			}
			return o.getName() + " (" + o.getOid() + ")";
		} 
		
		if (columnIndex > 0) {
			return "";
		}
		if (element instanceof Map.Entry) {
			Map.Entry<ObjectTypes,List<ServerObject>> entry = (Map.Entry<ObjectTypes,List<ServerObject>>) element;
			return entry.getKey().getDisplayName() + ": " + entry.getValue().size() + " object(s)";
		} else {
			return element.getClass() + ": " + element;
		}
	}
}