package com.evolveum.midpoint.eclipse.ui.tracer.common;

import com.evolveum.midpoint.eclipse.ui.tracer.other.TraceDetailsView;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
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

	public static String getReturn(OperationResultType opResult, String name) {
		if (opResult.getReturns() != null) {
			for (EntryType e : opResult.getReturns().getEntry()) {
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

}
