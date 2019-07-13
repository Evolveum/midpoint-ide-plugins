package com.evolveum.midpoint.eclipse.ui.tracer.views.lens;

import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.path.ItemName;

public abstract class PrismNode {
	
	protected final List<PrismNode> children = new ArrayList<>();
	protected PrismNode parent;		// due to hack in PrismItemNode
	
	public PrismNode(PrismNode parent) {
		this.parent = parent;
		if (parent != null) {
			parent.children.add(this);
		}
	}
	public List<PrismNode> getChildren() {
		return children;
	}
	public PrismNode getParent() {
		return parent;
	}
	public abstract String getLabel();
	public abstract String getValue(int i);
	
}
