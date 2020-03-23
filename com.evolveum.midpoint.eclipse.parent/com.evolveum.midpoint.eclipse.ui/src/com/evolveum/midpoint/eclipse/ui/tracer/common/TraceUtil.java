package com.evolveum.midpoint.eclipse.ui.tracer.common;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import com.evolveum.midpoint.eclipse.ui.tracer.other.TraceDetailsView;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ParamsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;

public class TraceUtil {

	@SuppressWarnings("unchecked")
	public static <T> T getTrace(OperationResultType result, Class<T> aClass) {
		for (TraceType trace : result.getTrace()) {
			if (aClass.isAssignableFrom(trace.getClass())) {
				return (T) trace;
			}
		}
		return null;
	}

	public static String getContext(OperationResultType opResult, String name) {
		if (opResult.getContext() != null) {
			for (EntryType e : opResult.getContext().getEntry()) {
				if (name.equals(e.getKey())) {
					return TraceDetailsView.dump(e.getEntryValue());
				}
			}
		}
		return "";
	}

	public static String getParameter(OperationResultType opResult, String name) {
		if (opResult.getParams() != null) {
			for (EntryType e : opResult.getParams().getEntry()) {
				if (name.equals(e.getKey())) {
					return TraceDetailsView.dump(e.getEntryValue());
				}
			}
		}
		return "";
	}
	
	public static List<JAXBElement<?>> selectByKey(ParamsType params, String key) {
		if (params != null) {
			return params.getEntry().stream()
				.filter(e -> key.equals(e.getKey()))
				.map(e -> e.getEntryValue())
				.collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	public static String getReturn(OperationResultType opResult, String name) {
		return String.join(", ", getReturnsAsStringList(opResult, name).toArray(new String[0]));
	}

	public static List<String> getReturnsAsStringList(OperationResultType opResult, String name) {
		return asStringList(selectByKey(opResult.getReturns(), name));
	}

	private static List<String> asStringList(List<JAXBElement<?>> elements) {
		return elements.stream()
				.map(e -> TraceDetailsView.dump(e))
				.collect(Collectors.toList());
	}
}
