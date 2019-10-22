package com.evolveum.midpoint.eclipse.ui.tree;

import java.util.Locale;

import com.evolveum.midpoint.eclipse.ui.tracer.common.OpNode;
import com.evolveum.midpoint.eclipse.ui.tracer.other.TraceDetailsView;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultImportanceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ParamsType;

public class ResultNode extends Node {

	private final OpNode opNode;
	
	public ResultNode(OpNode opNode, Node parent) {
		super(parent);
		this.opNode = opNode;
		createChildren();
	}

	@Override
	public String getLabel() {
		return "Operation result";
	}

	@Override
	public String getValue() {
		return opNode.getOperationNameFormatted() + " (" + opNode.getResult().getStatus() + ")";
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ResultNode;			// to avoid closing&reopening of this node in the tree
	}
	
	private void createChildren() {
		OperationResultType result = opNode.getResult();
		TextNode.create("Operation", opNode.getOperationNameFormatted(), this);
		TextNode.create("Operation raw", result.getOperation(), this);
		TextNode.create("Qualifiers", toString(result.getQualifier()), this);
		TextNode.create("Importance", getImportanceString(result), this);
		TextNode.create("Status", toString(result.getStatus()), this);
		TextNode.create("Message", toString(result.getMessage()), this);
		TextNode.create("Invocation ID", toString(result.getInvocationId()), this);
		TextNode.create("Start", toString(result.getStart()), this);
		TextNode.create("End", toString(result.getEnd()), this);
		TextNode.create("Duration", result.getMicroseconds() != null ? String.format(Locale.US, "%.1f ms", result.getMicroseconds() / 1000.0) : "?", this);
		if (result.getAsynchronousOperationReference() != null) {
			TextNode.create("Async operation ref", toString(result.getAsynchronousOperationReference()), this);
		}
		addParams("Parameter", result.getParams(), this);
		addParams("Context", result.getContext(), this);
		addParams("Return", result.getReturns(), this);
	}
	
	private void addParams(String prefix, ParamsType params, Node parent) {
		if (params != null) {
			for (EntryType e : params.getEntry()) {
				TextNode.create(prefix + ": " + e.getKey(), TraceDetailsView.dump(e.getEntryValue()), parent);
			}
		}
	}

	private String getImportanceString(OperationResultType result) {
		if (result.getImportance() != null) {
			return result.getImportance().toString();
		} else if (Boolean.TRUE.equals(result.isMinor())) {
			return OperationResultImportanceType.MINOR.toString();
		} else {
			return OperationResultImportanceType.NORMAL.toString();
		}
	}

	private String toString(Object o) {
		return o != null ? o.toString() : "";
	}

	@Override
	public Object getObject() {
		return opNode;
	}
}
