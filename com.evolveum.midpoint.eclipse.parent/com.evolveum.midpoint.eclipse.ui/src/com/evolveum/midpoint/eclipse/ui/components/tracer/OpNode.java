package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationsPerformanceInformationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;

public class OpNode {
	
	private final OperationResultType result;
	private final OpType type;
	private final List<OpNode> children = new ArrayList<>();
	private final OpNode parent;
	private final OperationsPerformanceInformationType performance;
	
	public OpNode(OperationResultType result, OpType type, OperationsPerformanceInformationType performance, OpNode parent) {
		this.result = result;
		this.type = type;
		this.performance = performance;
		this.parent = parent;
	}
	public OperationResultType getResult() {
		return result;
	}
	
	public OperationsPerformanceInformationType getPerformance() {
		return performance;
	}
	public OpType getType() {
		return type;
	}
	public List<OpNode> getChildren() {
		return children;
	}
	public OpNode getParent() {
		return parent;
	}
	
	public long getStart(long base) {
		return XmlTypeConverter.toMillis(result.getStart()) - base; 
	}
	public String dump() {
		try {
			return OperationResult.createOperationResult(result).debugDump();
		} catch (SchemaException|RuntimeException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	public TraceType getFirstTrace() {
		return result.getTrace().isEmpty() ? null : result.getTrace().get(0);
	}
	
}
