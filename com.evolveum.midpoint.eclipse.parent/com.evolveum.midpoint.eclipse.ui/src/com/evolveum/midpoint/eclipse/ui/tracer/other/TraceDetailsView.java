package com.evolveum.midpoint.eclipse.ui.tracer.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.runtime.PrismContextHolder;
import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.tracer.common.OpNode;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EntryType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MappingEvaluationTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultImportanceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ParamsType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;
import com.evolveum.prism.xml.ns._public.types_3.RawType;

public class TraceDetailsView extends ViewPart implements ISelectionListener {
	private Text text;

	public TraceDetailsView() {
       	super();
	}

	public void setFocus() {
		text.setFocus();
	}

	public void createPartControl(Composite parent) {
//		Font mono = new Font(parent.getDisplay(), "Monospaced", 10, SWT.NONE);
		
		text = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setText("Hello world");
		text.setFont(JFaceResources.getTextFont());
		
		getViewSite().getPage().addSelectionListener(this);
	}

	/**
	 * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof OpNode) {
				OpNode node = ((OpNode) first);
				OperationResultType result = node.getResult();
				StringBuilder sb = new StringBuilder();
				for (TraceType trace : result.getTrace()) {
					sb.append(dump(trace));
					sb.append("\n------------------------------------------------------------------------\n");
				}
				sb.append("Operation:  ").append(result.getOperation()).append("\n");
				sb.append("Qualifier:  ").append(result.getQualifier()).append("\n");
				sb.append("Importance: ");
				if (result.getImportance() != null) {
					sb.append(result.getImportance());
				} else if (Boolean.TRUE.equals(result.isMinor())) {
					sb.append(OperationResultImportanceType.MINOR);
				} else {
					sb.append(OperationResultImportanceType.NORMAL);
				}
				sb.append("\n");
				sb.append("Status:     ").append(result.getStatus());
				if (result.getMessage() != null) {
					sb.append(": " + result.getMessage());
				}
				sb.append("\n");
				sb.append("Inv. ID:    ").append(result.getInvocationId()).append("\n");
				sb.append("\n");
				sb.append("Start:      ").append(result.getStart()).append("\n");
				sb.append("End:        ").append(result.getEnd()).append("\n");
				sb.append("Duration :  ").append(result.getMicroseconds() != null ? String.format(Locale.US, "%.1f ms", result.getMicroseconds() / 1000.0) : "?").append("\n");
				sb.append("\n");
				sb.append(dump(" - par: ", result.getParams()));
				sb.append(dump(" - ctx: ", result.getContext()));
				sb.append(dump(" - ret: ", result.getReturns()));
				sb.append("\n------------------------------------------------------------------------\n");
				
				this.text.setText(sb.toString());
			} else {
				text.setText(String.valueOf(first));
			}
		}

	}
	private String dump(TraceType trace) {
		TraceType traceNoText;
		List<String> texts;
		if (trace instanceof MappingEvaluationTraceType) {
			texts = new ArrayList<>();
			texts.add(((MappingEvaluationTraceType) trace).getTextTrace());
			try {
				texts.add(PrismContextHolder.getPrismContext().xmlSerializer().serializeRealValue(((MappingEvaluationTraceType) trace).getMapping(), new QName("mapping")));
			} catch (SchemaException e) {
				e.printStackTrace();
				texts.add(e.getMessage());
			}
			traceNoText = null;
		} else if (!trace.getText().isEmpty()) {
			traceNoText = trace.clone();
			traceNoText.asPrismContainerValue().removeProperty(TraceType.F_TEXT);
			texts = trace.getText();
		} else {
			traceNoText = trace;
			texts = Collections.emptyList();
		}
		
		String rv = "";
		if (traceNoText != null) {
			rv = trace.getClass().getSimpleName() + ":" + "\n" + traceNoText.asPrismContainerValue().debugDump().substring(8);		// hack to remove id=null
			rv += "\n------------------------------\n";
		}
		for (String text : texts) {
			rv += text;
			rv += "\n------------------------------\n";
		}
		return rv;
	}

	private String dump(String prefix, ParamsType params) {
		String rv = "";
		if (params != null) {
			for (EntryType e : params.getEntry()) {
				rv += prefix + e.getKey() + " = " + dump(e.getEntryValue()) + "\n"; 
			}
		}
		return rv;
	}

	public static String dump(JAXBElement<?> jaxb) {
		if (jaxb == null) {
			return "";
		}
		Object value = jaxb.getValue();
		if (value instanceof RawType) {
			return ((RawType) value).extractString();
		} else {
			return String.valueOf(value);
		}
	}
	
}