package com.evolveum.midpoint.eclipse.ui.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Node {
	
	protected final List<Node> children = new ArrayList<>();
	protected Node parent;		// due to hack
	
	public Node(Node parent) {
		this.parent = parent;
		if (parent != null) {
			parent.children.add(this);
		}
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	public Node getParent() {
		return parent;
	}
	
	public abstract String getLabel();
	public abstract String getValue();
	
	@Override
	public int hashCode() {
		return Objects.hash(getLabel(), getValue());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof Node)) {
			return false;
		} else {
			Node node = (Node) obj;
			return Objects.equals(getLabel(), node.getLabel()) &&
					Objects.equals(getValue(), node.getValue());
		}
	}
}
