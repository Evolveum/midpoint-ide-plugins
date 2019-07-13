package com.evolveum.midpoint.eclipse.ui.tracer.other;

import javax.xml.namespace.QName;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.runtime.PrismContextHolder;
import com.evolveum.midpoint.eclipse.ui.tracer.common.OpNode;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;

public class TraceDetailsRawView extends ViewPart implements ISelectionListener {
	private Text text;

	public TraceDetailsRawView() {
       	super();
	}

	public void setFocus() {
		text.setFocus();
	}

	public void createPartControl(Composite parent) {
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
				
				this.text.setText(sb.toString());
			} else {
				text.setText(String.valueOf(first));
			}
		}
	}

	private String dump(TraceType trace) {
		try {
			return PrismContextHolder.getPrismContext().xmlSerializer().serializeRealValue(trace, new QName("trace"));
		} catch (Throwable t) {
			t.printStackTrace();
			return t.toString();
		}
	}
	
}