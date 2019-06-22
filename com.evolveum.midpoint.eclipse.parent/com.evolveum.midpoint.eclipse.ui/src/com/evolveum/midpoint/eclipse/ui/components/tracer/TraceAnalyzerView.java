package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.PrismContextHolder;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

public class TraceAnalyzerView extends ViewPart {
	private TreeViewer viewer;
	private Label labelTraceFile;
	private File traceFile;
	private List<OpNode> traceRoots;
	private long start;
	
	private DatatypeFactory datatypeFactory;
	{
		try {
			datatypeFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	public TraceAnalyzerView() {
		super();
	}

	public void createPartControl(Composite parent) {
		labelTraceFile = new Label(parent, 0);
		refreshFileLabel();

		labelTraceFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent event) {
				super.mouseUp(event);
				File selected = browseForFile(traceFile, parent);
				if (selected != null) {
					traceFile = selected;
					refreshFileLabel();
					parseTraceFile();
				}
			}
		});

		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new TreeContentProvider());
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);

		Tree tree = (Tree) viewer.getControl();
//		tree.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				TreeItem item = (TreeItem) e.item;
//				if (item.getItemCount() > 0) {
//					item.setExpanded(!item.getExpanded());
//					// update the viewer
//					viewer.refresh();
//				}
//			}
//		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				TreeViewer viewer = (TreeViewer) event.getViewer();
				IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
				Object selectedNode = thisSelection.getFirstElement();
				viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));
			}
		});

//		Listener listener = new Listener() {
//
//			@Override
//			public void handleEvent(Event event) {
//				TreeItem treeItem = (TreeItem) event.item;
//				final TreeColumn[] treeColumns = treeItem.getParent().getColumns();
//				parent.getDisplay().asyncExec(new Runnable() {
//					@Override
//					public void run() {
//						for (TreeColumn treeColumn : treeColumns)
//							treeColumn.pack();
//					}
//				});
//			}
//		};
//
//		tree.addListener(SWT.Expand, listener);
		
		TreeViewerColumn operationColumn = new TreeViewerColumn(viewer, SWT.NONE);
		operationColumn.getColumn().setWidth(300);
		operationColumn.getColumn().setText("Operation");
		operationColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getResult().getOperation())));

		TreeViewerColumn statusColumn = new TreeViewerColumn(viewer, SWT.NONE);
		statusColumn.getColumn().setWidth(100);
		statusColumn.getColumn().setText("Status");
		statusColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getResult().getStatus())));

		TreeViewerColumn startColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
		startColumn.getColumn().setWidth(60);
		startColumn.getColumn().setText("Start");
		startColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getStart(start))));

		TreeViewerColumn microsecondsColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
		microsecondsColumn.getColumn().setWidth(50);
		microsecondsColumn.getColumn().setText("Time");
		microsecondsColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getResult().getMicroseconds() != null ? String.format("%.1f", n.getResult().getMicroseconds() / 1000.0f) : "")));

		TreeViewerColumn typeColumn = new TreeViewerColumn(viewer, SWT.NONE);
		typeColumn.getColumn().setWidth(60);
		typeColumn.getColumn().setText("Type");
		typeColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getType())));

//		TreeViewerColumn invocationIdColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
//		invocationIdColumn.getColumn().setWidth(30);
//		invocationIdColumn.getColumn().setText("ID");
//		invocationIdColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider("invocationId")));

		setViewerInput();
		
		getSite().setSelectionProvider(viewer); 

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	class MyLabelProvider extends LabelProvider implements IStyledLabelProvider {

		private Function<OpNode, Object> extractor;

		public MyLabelProvider(Function<OpNode, Object> extractor) {
			this.extractor = extractor;
		}

		@Override
		public StyledString getStyledText(Object o) {
			if (o instanceof OpNode) {
				Object value = extractor.apply((OpNode) o);
				return new StyledString(value != null ? value.toString() : "");
			} else {
				return null;
			}
		}
	}

	private void refreshFileLabel() {
		labelTraceFile.setText("Trace file: " + (traceFile != null ? traceFile : "(undefined)"));
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private File browseForFile(File existingFile, Composite parent) {
		FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN | SWT.SHEET);
		if (existingFile != null) {
			dialog.setFileName(existingFile.getPath());
		}
		String file = dialog.open();
		if (file != null) {
			file = file.trim();
			if (file.length() > 0) {
				return new File(file);
			}
		}
		return null;
	}

	private void parseTraceFile() {
		try {
			ViewOptions options = new ViewOptions();
//			options.show(OpType.CLOCKWORK_RUN);
//			options.show(OpType.MAPPING_EVALUATION);
			TraceParser parser = new TraceParser(options);
			traceRoots = parser.parse(traceFile);
			start = parser.getStartTimestamp();
			setViewerInput();
		} catch (Throwable t) {
			System.err.println("Couldn't parse trace file");
			t.printStackTrace();
			start = 0;
			traceRoots = null;
		}
	}

	public void setViewerInput() {
		viewer.setInput(traceRoots != null ? traceRoots.toArray() : null);
	}

}