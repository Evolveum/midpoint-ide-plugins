package com.evolveum.midpoint.eclipse.ui.tracer.views.entry;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.ui.controls.text.TextValueViewer;
import com.evolveum.midpoint.eclipse.ui.tracer.common.OpNode;
import com.evolveum.midpoint.eclipse.ui.tree.Node;
import com.evolveum.midpoint.eclipse.ui.tree.NodeContentProvider;
import com.evolveum.midpoint.eclipse.ui.tree.ResultNode;
import com.evolveum.midpoint.eclipse.ui.tree.TextNode;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TraceType;

public class EntryView extends ViewPart implements ISelectionListener {
	
	private TreeViewer viewer;
	private Node resultNode, traceNode;
	private final Object[] roots = new Object[2];
	
	private TextValueViewer textValueViewer;
	
	public EntryView() {
		super();
	}

	public void createPartControl(Composite parent) {
		
		parent.setLayout(new FillLayout());
		SashForm sash = new SashForm(parent, SWT.VERTICAL);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		sash.setLayout(gridLayout);
		
		createViewer(sash);
		createText(sash);		

		resultNode = TextNode.create("Result", "", null);
		traceNode = TextNode.create("Trace", "", null);
		setViewerInput();
		
		//GridLayoutFactory.fillDefaults().generateLayout(parent);
		
		//getSite().setSelectionProvider(viewer); 

		getViewSite().getPage().addSelectionListener(this);
	}

	public void createViewer(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new NodeContentProvider());
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		TreeViewerColumn nameColumn = new TreeViewerColumn(viewer, SWT.NONE);
		nameColumn.getColumn().setWidth(150);
		nameColumn.getColumn().setText("Item");
		nameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getLabel())));

		TreeViewerColumn valueColumn = new TreeViewerColumn(viewer, SWT.NONE);
		valueColumn.getColumn().setWidth(400);
		valueColumn.getColumn().setText("Value");
		valueColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getValue())));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				applySelection();
			}
		});
	}
	
	private void applySelection() {
		ITreeSelection selection = viewer.getStructuredSelection();
		if (selection.isEmpty()) {
			textValueViewer.setObject(null);
		} else {
			Object first = selection.getFirstElement();
			if (first instanceof Node) {
				textValueViewer.setObject(((Node) first).getObject());
			} else {
				textValueViewer.setObject(null);
			}
		}
	}

	private void createText(Composite parent) {
		textValueViewer = new TextValueViewer();
		textValueViewer.create(parent);
	}
	
	public class MyLabelProvider extends LabelProvider implements IStyledLabelProvider {

		private Function<Node, Object> extractor;

		public MyLabelProvider(Function<Node, Object> extractor) {
			this.extractor = extractor;
		}

		@Override
		public StyledString getStyledText(Object o) {
			if (o instanceof Node) {
				Object value = extractor.apply((Node) o);
				return new StyledString(value != null ? value.toString() : "");
			} else {
				return null;
			}
		}
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void setViewerInput() {
		roots[0] = resultNode;
		roots[1] = traceNode;
		viewer.setInput(roots);
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof OpNode) {
				OpNode currentOpNode = ((OpNode) first);
				resultNode = new ResultNode(currentOpNode, null);
				try {
					traceNode = createTraceNode(currentOpNode);
				} catch (SchemaException e) {
					e.printStackTrace();
					traceNode = TextNode.create("Trace", "Error: " + e.getMessage(), null);
				}
				roots[0] = resultNode;
				roots[1] = traceNode;
				viewer.refresh();
				applySelection();
			} else {
				return;
			}
		} else {
			return;
		}
	}

	private Node createTraceNode(OpNode opNode) throws SchemaException {
		List<TraceType> traces = opNode.getResult().getTrace();
		if (traces.isEmpty()) {
			return TextNode.create("Trace", "(none)", null);
		} else if (traces.size() == 1) {
			return createTraceNode(traces.get(0), null);
		} else {
			TextNode rv = TextNode.create("Trace", "Number: " + traces.size(), null);
			for (TraceType trace : traces) {
				createTraceNode(trace, rv);
			}
			return rv;
		}
	}

	private Node createTraceNode(TraceType trace, TextNode parent) throws SchemaException {
		return TraceNode.create(trace, parent);
	}
}