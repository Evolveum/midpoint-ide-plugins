package com.evolveum.midpoint.eclipse.ui.tracer.views.log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.ui.tracer.common.OpNode;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LogSegmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

public class TraceLogView extends ViewPart implements ISelectionListener {
	private Text text;

	public TraceLogView() {
       	super();
	}

	public void setFocus() {
		text.setFocus();
	}

	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setText("");
		text.setFont(JFaceResources.getTextFont());
		
		getViewSite().getPage().addSelectionListener(this);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof OpNode) {
				OpNode node = ((OpNode) first);
				StringBuilder sb = new StringBuilder();
				collectLogEntries(sb, node);
				this.text.setText(sb.toString());
			} else {
				text.setText(String.valueOf(first));
			}
		}
	}
	
	private static class LogSegment {
		private final LogSegmentType segment;
		private final OpNode owner;
		public LogSegment(LogSegmentType segment, OpNode owner) {
			this.segment = segment;
			this.owner = owner;
		}
	}

	public void collectLogEntries(StringBuilder sb, OpNode node) {
		List<LogSegment> allSegments = new ArrayList<>();
		collectLogSegments(allSegments, node);
		allSegments.sort(Comparator.comparing(seg -> seg.segment.getSequenceNumber()));
		
		for (LogSegment segment : allSegments) {
			sb.append("---> Segment #" + segment.segment.getSequenceNumber() + " in " + segment.owner.getOperationQualified() + "\n");
			for (String entry : segment.segment.getEntry()) {
				sb.append(entry).append("\n");
			}
		}
	}

	private void collectLogSegments(List<LogSegment> allSegments, OpNode node) {
		for (LogSegmentType segment : node.getResult().getLog()) {
			allSegments.add(new LogSegment(segment, node));
		}
		node.getChildren().forEach(child -> collectLogSegments(allSegments, child));
	}
	
}