package com.evolveum.midpoint.eclipse.ui.tracer.views.lens;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class PrismNodeContentProvider implements ITreeContentProvider {
    @Override
    public boolean hasChildren(Object o) {
        return getChildren(o).length > 0;
    }

    @Override
    public Object getParent(Object o) {
    	return ((PrismNode) o).getParent();
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
    	return ((PrismNode) o).getChildren().toArray();
    }
}

