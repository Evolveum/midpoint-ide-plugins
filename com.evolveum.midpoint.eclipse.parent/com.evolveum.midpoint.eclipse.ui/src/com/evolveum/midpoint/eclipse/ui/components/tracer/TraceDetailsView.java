package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.bind.JAXBElement;

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

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.xml.ns._public.common.common_3.EntryType;
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
				String text = "";
				for (TraceType trace : result.getTrace()) {
					text += dump(trace);
					text += "\n------------------------------------------------------------------------\n";
				}
				text += "Operation: " + result.getOperation() + "\n";
				text += "Status:    " + result.getStatus() + "\n";
				text += "Duration:  " + (result.getMicroseconds() != null ? String.format(Locale.US, "%.1f", result.getMicroseconds() / 1000.0) : "?") + "\n";
				text += "\n";
				text += dump(" - par: ", result.getParams());
				text += dump(" - ctx: ", result.getContext());
				text += dump(" - ret: ", result.getReturns());
				text += "\n------------------------------------------------------------------------\n";
				
				this.text.setText(text);
			} else {
				text.setText(String.valueOf(first));
			}
		}
	}

	private String dump(TraceType trace) {
		TraceType traceNoText;
		List<String> texts;
		if (!trace.getText().isEmpty()) {
			traceNoText = trace.clone();
			traceNoText.asPrismContainerValue().removeProperty(TraceType.F_TEXT);
			texts = trace.getText();
		} else {
			traceNoText = trace;
			texts = Collections.emptyList();
		}
		
		String rv = trace.getClass().getSimpleName() + ":" + "\n" + traceNoText.asPrismContainerValue().debugDump().substring(8);		// hack to remove id=null
		for (String text : texts) {
			rv += "\n------------------------------\n";
			rv += text;
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

	private String dump(JAXBElement<?> jaxb) {
		if (jaxb == null) {
			return "(null)";
		}
		Object value = jaxb.getValue();
		if (value instanceof RawType) {
			return ((RawType) value).extractString();
		} else {
			return String.valueOf(value);
		}
	}
	
}