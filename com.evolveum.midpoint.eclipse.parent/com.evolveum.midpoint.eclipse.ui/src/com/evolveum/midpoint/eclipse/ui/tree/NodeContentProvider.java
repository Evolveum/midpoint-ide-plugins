package com.evolveum.midpoint.eclipse.ui.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class NodeContentProvider implements ITreeContentProvider {
    @Override
    public boolean hasChildren(Object o) {
        return getChildren(o).length > 0;
    }

    @Override
    public Object getParent(Object o) {
    	return ((Node) o).getParent();
    }

    @Override
    public Object[] getElements(Object o) {
    	if (o instanceof Object[]) {
			return (Object[]) o;
		} else {
			return new Object[0];
		}
    }

    @Override
    public Object[] getChildren(Object o) {
    	return ((Node) o).getChildren().toArray();
    }
}

