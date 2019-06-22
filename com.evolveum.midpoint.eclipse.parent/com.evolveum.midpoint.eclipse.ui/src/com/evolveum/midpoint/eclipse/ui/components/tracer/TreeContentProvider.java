package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.util.DOMUtil;

public class TreeContentProvider implements ITreeContentProvider {
    @Override
    public boolean hasChildren(Object o) {
        return getChildren(o).length > 0;
    }

    @Override
    public Object getParent(Object o) {
    	//System.out.println("getParent called for " + describe(o));
    	if (o instanceof OpNode) {
        	return ((OpNode) o).getParent();
        } else {
        	return null;
        }
    }

    @Override
    public Object[] getElements(Object o) {
    	//System.out.println("getElements called for " + describe(o));
    	if (o instanceof Object[]) {
			return (Object[]) o;
		} else {
			return new Object[0];
		}
    }

    @Override
    public Object[] getChildren(Object o) {
    	//System.out.println("getChildren called for " + describe(o));
        if (o instanceof OpNode) {
        	return ((OpNode) o).getChildren().toArray();
        } else {
        	return new Object[0];
        }
    }
    
	private static String get(Element e, String elementName) {
		Element sub = DOMUtil.getChildElement(e, elementName);
		return sub != null ? sub.getTextContent() : null;
	}

//	@Override
//	public void dispose() {
//	}
//
//	@Override
//	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//		ITreeContentProvider.super.inputChanged(viewer, oldInput, newInput);
//	}
}

