package com.evolveum.midpoint.eclipse.ui.components.browser;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;

public class ObjectTypesListLabelProvider extends LabelProvider {
	public Image getImage(Object element) {
		return null;
	}

	public String getText(Object element) {
		ObjectTypes type = (ObjectTypes) element;
		return type.getDisplayName();
	}
}