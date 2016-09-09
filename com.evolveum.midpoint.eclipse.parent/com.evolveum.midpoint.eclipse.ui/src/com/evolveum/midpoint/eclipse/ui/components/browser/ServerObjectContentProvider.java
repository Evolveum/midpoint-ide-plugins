package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class ServerObjectContentProvider implements ITreeContentProvider {

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		} else {
			return new Object[0];
		}
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Map) {
			return !((Map) element).isEmpty();
		} else if (element instanceof Map.Entry) {
			return hasChildren(((Map.Entry) element).getValue());
		} else if (element instanceof Collection) {
			return !((Collection) element).isEmpty();
		} else {
			return false;
		}
	}
	
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Map) {
			return ((Map) parentElement).entrySet().toArray();
		} else if (parentElement instanceof Map.Entry) {
			return getChildren(((Map.Entry) parentElement).getValue());
		} else if (parentElement instanceof Collection) {
			return ((Collection) parentElement).toArray();
		} else {
			return new Object[0];
		}
	}
	
}