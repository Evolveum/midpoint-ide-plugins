package com.evolveum.midpoint.eclipse.ui.tracer.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;

public class TraceTreeContentProvider implements ITreeContentProvider {
    @Override
    public boolean hasChildren(Object o) {
        return getChildren(o).length > 0;
    }

    @Override
    public Object getParent(Object o) {
    	OpNode parent = ((OpNode) o).getParent();
        if (parent != null) {
        	return parent.isVisible() ? parent : getParent(parent);
        } else {
        	return null;
        }
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
    	List<OpNode> rv = new ArrayList<>();
    	collectChildren(rv, ((OpNode) o).getChildren());
        return rv.toArray();
    }

	private void collectChildren(List<OpNode> rv, List<OpNode> nodes) {
		 for (OpNode node : nodes) {
			 if (node.isVisible()) {
				 rv.add(node);
			 } else {
				 collectChildren(rv, node.getChildren());
			 }
		 }
	}
   
}

