package com.evolveum.midpoint.eclipse.ui.tracer.other;

import static com.evolveum.midpoint.eclipse.ui.tracer.views.performance.TracePerformanceView.formatTime;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.ui.tracer.common.OpNode;
import com.evolveum.midpoint.eclipse.ui.tracer.common.Options;
import com.evolveum.midpoint.eclipse.ui.tracer.common.PerformanceCategory;
import com.evolveum.midpoint.eclipse.ui.tracer.common.TraceParser;
import com.evolveum.midpoint.eclipse.ui.tracer.common.TraceTreeContentProvider;
import com.evolveum.midpoint.eclipse.ui.tracer.views.options.TraceOptionsView;
import com.evolveum.midpoint.eclipse.ui.tracer.views.performance.TracePerformanceView;

public class TraceAnalyzerView extends ViewPart {
	private TreeViewer viewer;
	private Label labelTraceFile;
	private File traceFile;
	private List<OpNode> traceRoots;
	private long start;
	private TraceOptionsView optionsView;
	
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
		viewer.setContentProvider(new TraceTreeContentProvider());
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);

		//Tree tree = (Tree) viewer.getControl();
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
		operationColumn.getColumn().setWidth(500);
		operationColumn.getColumn().setText("Operation");
		operationColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getOperationNameFormatted())));

		TreeViewerColumn stateColumn = new TreeViewerColumn(viewer, SWT.NONE);
		stateColumn.getColumn().setWidth(60);
		stateColumn.getColumn().setText("State");
		stateColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getClockworkState())));

		TreeViewerColumn execWaveColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
		execWaveColumn.getColumn().setWidth(35);
		execWaveColumn.getColumn().setText("EW");
		execWaveColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getExecutionWave())));

		TreeViewerColumn projWaveColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
		projWaveColumn.getColumn().setWidth(35);
		projWaveColumn.getColumn().setText("PW");
		projWaveColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getProjectionWave())));

		TreeViewerColumn statusColumn = new TreeViewerColumn(viewer, SWT.NONE);
		statusColumn.getColumn().setWidth(100);
		statusColumn.getColumn().setText("Status");
		statusColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getResult().getStatus())));

		TreeViewerColumn importanceColumn = new TreeViewerColumn(viewer, SWT.NONE);
		importanceColumn.getColumn().setWidth(20);
		importanceColumn.getColumn().setText("W");
		importanceColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getImportanceSymbol())));

		TreeViewerColumn startColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
		startColumn.getColumn().setWidth(60);
		startColumn.getColumn().setText("Start");
		startColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getStart(start))));

		TreeViewerColumn microsecondsColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
		microsecondsColumn.getColumn().setWidth(80);
		microsecondsColumn.getColumn().setText("Time");
		microsecondsColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> formatTime(n.getResult().getMicroseconds()))));

		TreeViewerColumn typeColumn = new TreeViewerColumn(viewer, SWT.NONE);
		typeColumn.getColumn().setWidth(100);
		typeColumn.getColumn().setText("Type");
		typeColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getType())));
		
		addPerformanceColumn(PerformanceCategory.REPOSITORY);
		addPerformanceColumn(PerformanceCategory.REPOSITORY_CACHE);
		addPerformanceColumn(PerformanceCategory.MAPPING_EVALUATION);
		addPerformanceColumn(PerformanceCategory.ICF);
		
		TreeViewerColumn logLinesColumn = new TreeViewerColumn(viewer, SWT.NONE);
		logLinesColumn.getColumn().setWidth(50);
		logLinesColumn.getColumn().setText("Log");
		logLinesColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getLogEntriesCount())));

//		TreeViewerColumn invocationIdColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
//		invocationIdColumn.getColumn().setWidth(30);
//		invocationIdColumn.getColumn().setText("ID");
//		invocationIdColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider("invocationId")));

		optionsView = getOptionsView();
		System.out.println("Options view = " + optionsView);
		
		setViewerInput();
		
		getSite().setSelectionProvider(viewer); 
		
		GridLayoutFactory.fillDefaults().generateLayout(parent);
		
	}
	
	private TraceOptionsView getOptionsView() {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workbenchWindow == null) {
            IWorkbenchWindow[] allWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (IWorkbenchWindow window : allWindows) {
                workbenchWindow = window;
                if (workbenchWindow != null) {
                    break;
                }
            }
        }

        if (workbenchWindow == null) {
            throw new IllegalStateException("Could not retrieve workbench window");
        }
        IWorkbenchPage activePage = workbenchWindow.getActivePage();

        try {
            IViewPart viewPart = activePage.showView("com.evolveum.midpoint.eclipse.ui.views.trace.options");
            return (TraceOptionsView) viewPart;
        } catch (PartInitException e) {
            return null;
        }
	}

	private void addPerformanceColumn(PerformanceCategory category) {
		TreeViewerColumn countColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
		countColumn.getColumn().setWidth(70);
		countColumn.getColumn().setText(category.getShortLabel() + " #");
		countColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(getCount(element));
			}
			private int getCount(Object element) {
				return ((OpNode) element).getPerformanceByCategory().get(category).getTotalCount();
			}
			@Override
			public Color getForeground(Object element) {
				return TracePerformanceView.getColor(getCount(element));
			}
		});
		//countColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getPerformanceByCategory().get(category).getTotalCount())));

		TreeViewerColumn timeColumn = new TreeViewerColumn(viewer, SWT.RIGHT);
		timeColumn.getColumn().setWidth(80);
		timeColumn.getColumn().setText(category.getShortLabel() + " time");
		timeColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> formatTime(n.getPerformanceByCategory().get(category).getTotalTime()))));
		timeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(getTime(element));
			}
			private long getTime(Object element) {
				return ((OpNode) element).getPerformanceByCategory().get(category).getTotalTime();
			}
			@Override
			public Color getForeground(Object element) {
				return TracePerformanceView.getColor(getTime(element));
			}
		});

	}

	public class MyLabelProvider extends LabelProvider implements IStyledLabelProvider {

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
			TraceParser parser = new TraceParser();
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

	public void applyOptions(Options options) {
		System.out.println("Applying options");
		if (traceRoots != null) {
			for (OpNode root : traceRoots) {
				root.applyOptions(options);
			}
			viewer.refresh();
		}
	}

}