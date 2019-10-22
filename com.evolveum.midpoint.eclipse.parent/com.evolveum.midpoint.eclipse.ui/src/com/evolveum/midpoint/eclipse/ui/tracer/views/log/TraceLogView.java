package com.evolveum.midpoint.eclipse.ui.tracer.views.log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.ui.controls.text.Format;
import com.evolveum.midpoint.eclipse.ui.tracer.common.OpNode;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LogSegmentType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

public class TraceLogView extends ViewPart implements ISelectionListener {
	private Composite textSwitched;
	private Text textWrapped, textUnwrapped;
	private Button showSegmentSeparators;
	private Button wrap;
	private OpNode currentOpNode;
	
	public TraceLogView() {
       	super();
	}

	public void setFocus() {
		textSwitched.setFocus();
	}

	public void createPartControl(Composite parent) {
		
		Composite main = new Composite(parent, SWT.SHADOW_NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		main.setLayout(gridLayout);
		main.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Group options = new Group(main, SWT.SHADOW_IN);
		options.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		FillLayout fillLayout = new FillLayout();
		fillLayout.marginHeight = 5;
		fillLayout.marginWidth = 5;
		fillLayout.spacing = 15;
		options.setLayout(fillLayout);
		
		textSwitched = new Composite(main, SWT.SHADOW_NONE);
		textSwitched.setLayoutData(new GridData(GridData.FILL_BOTH));
		StackLayout stackLayout = new StackLayout();
		textSwitched.setLayout(stackLayout);
		
		textWrapped = createText(textSwitched, true);
		textUnwrapped = createText(textSwitched, false);

		wrap = new Button(options, SWT.CHECK);
		wrap.setText("Wrap text");
		wrap.setSelection(true);
		wrap.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				stackLayout.topControl = wrap.getSelection() ? textWrapped : textUnwrapped;
				textSwitched.layout();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		showSegmentSeparators = new Button(options, SWT.CHECK);
		showSegmentSeparators.setText("Show segment separators");
		showSegmentSeparators.setSelection(false);
		showSegmentSeparators.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTexts();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		stackLayout.topControl = wrap.getSelection() ? textWrapped : textUnwrapped;
		textSwitched.layout();
		
		getViewSite().getPage().addSelectionListener(this);
	}

	public Text createText(Composite parent, boolean isWrap) {
		Text text = new Text(parent, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | (isWrap ? SWT.WRAP : SWT.H_SCROLL) | SWT.V_SCROLL);
		text.setText("");
		text.setFont(JFaceResources.getTextFont());
		return text;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof OpNode) {
				currentOpNode = ((OpNode) first);
				updateTexts();
//			} else {
//				currentOpNode = null;
//				updateTexts(String.valueOf(first));		// TODO ok?
			}
		}
	}
	
	private void updateTexts() {
		StringBuilder sb = new StringBuilder();
		if (currentOpNode != null) {
			collectLogEntries(sb, currentOpNode);
		}
		updateTexts(sb.toString());
	}
	
	private void updateTexts(String text) {
		textWrapped.setText(text);
		textUnwrapped.setText(text);
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
			if (showSegmentSeparators.getSelection()) {
				sb.append("---> Segment #" + segment.segment.getSequenceNumber() + " in " + segment.owner.getOperationQualified() + " (inv: " + segment.owner.getResult().getInvocationId() + ")\n");
			}
			for (String entry : segment.segment.getEntry()) {
				// ugly hacking to normalize line ends
				if (entry.endsWith("\r")) {
					entry = entry.substring(0, entry.length()-1);
				}
				String normalized = entry.replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
				sb.append(normalized).append("\n");
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