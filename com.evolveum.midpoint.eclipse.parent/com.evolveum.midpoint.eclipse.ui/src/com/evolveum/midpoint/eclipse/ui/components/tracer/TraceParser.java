package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.evolveum.midpoint.eclipse.runtime.PrismContextHolder;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.statistics.OperationsPerformanceInformationUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationsPerformanceInformationType;

public class TraceParser {
	
	private ViewOptions options;
	private OperationResultType rootResult;
	
	public TraceParser(ViewOptions options) {
		this.options = options;
	}

	public List<OpNode> parse(File traceFile) throws SchemaException, IOException {
		PrismContext prismContext = PrismContextHolder.getPrismContext();
		if (traceFile.getName().toLowerCase().endsWith(".zip")) {
			try (ZipInputStream zis = new ZipInputStream(new FileInputStream(traceFile))) {
				ZipEntry zipEntry = zis.getNextEntry();
				if (zipEntry != null) {
					rootResult = prismContext.parserFor(zis).xml().parseRealValue(OperationResultType.class);
				} else {
					System.err.println("No zip entry in " + traceFile);		// TODO error handling
					rootResult = null;
				}
			}
		} else {
			rootResult = prismContext.parserFor(traceFile).xml().parseRealValue(OperationResultType.class);
		}
		if (rootResult != null) {
			List<OpNode> rv = new ArrayList<>();
			addNode(null, rv, rootResult);
			return rv;
		} else {
			return new ArrayList<>();
		}
	}

	private void addNode(OpNode parent, List<OpNode> rv, OperationResultType result) {
		OpType type = OpType.determine(result);
		if (isVisible(result, type)) {
			OpNode newNode = new OpNode(result, type, getPerformance(result), parent);
			rv.add(newNode);
			for (OperationResultType child : result.getPartialResults()) {
				addNode(newNode, newNode.getChildren(), child);
			}
		} else {
			for (OperationResultType child : result.getPartialResults()) {
				addNode(parent, rv, child);
			}
		}
	}

	private boolean isVisible(OperationResultType result, OpType type) {
		return options.getShowOperationTypes().isEmpty() || options.getShowOperationTypes().contains(type);
	}

	public long getStartTimestamp() {
		return rootResult != null ? XmlTypeConverter.toMillis(rootResult.getStart()) : 0;
	}
	
	OperationsPerformanceInformationType getPerformance(OperationResultType result) {
		OperationsPerformanceInformationType rv = new OperationsPerformanceInformationType();
		addPerformance(rv, result);
		return rv;
	}

	private void addPerformance(OperationsPerformanceInformationType rv, OperationResultType result) { 
		if (result.getMicroseconds() != null) {
			OperationsPerformanceInformationType oper = new OperationsPerformanceInformationType();
			oper.beginOperation()
					.name(result.getOperation())
					.invocationCount(1)
					.totalTime(result.getMicroseconds())
					.minTime(result.getMicroseconds())
					.maxTime(result.getMicroseconds());
			OperationsPerformanceInformationUtil.addTo(rv, oper);
		}
		for (OperationResultType child : result.getPartialResults()) {
			addPerformance(rv, child);
		}
	}

}
