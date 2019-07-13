package com.evolveum.midpoint.eclipse.ui.tracer.views.entry;

import com.evolveum.midpoint.eclipse.ui.tree.TextNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.eclipse.ui.tree.Node;
import com.evolveum.midpoint.eclipse.ui.tree.PrismValueNode;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ModelExecuteDeltaTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;

public class TraceNode<T extends TraceType> extends Node {

	protected final T trace;
	
	public TraceNode(T trace, Node parent) {
		super(parent);
		this.trace = trace;
	}

	@Override
	public String getLabel() {
		return trace.getClass().getSimpleName();
	}

	@Override
	public String getValue() {
		return "...";
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TraceNode;			// to avoid closing&reopening of this node in the tree
		// TODO
	}

	public static Node create(TraceType trace, TextNode parent) throws SchemaException {
		if (trace instanceof ModelExecuteDeltaTraceType) {
			return new ModelExecuteDeltaTraceNode((ModelExecuteDeltaTraceType) trace, parent);
		} else if (trace != null) {
			return PrismValueNode.create("Trace", trace.asPrismContainerValue(), parent);			
		} else {
			return TextNode.create("Trace", "", parent);
		}
	}

	@Override
	public Object getObject() {
		return trace.asPrismContainerValue();
	}
	
}
