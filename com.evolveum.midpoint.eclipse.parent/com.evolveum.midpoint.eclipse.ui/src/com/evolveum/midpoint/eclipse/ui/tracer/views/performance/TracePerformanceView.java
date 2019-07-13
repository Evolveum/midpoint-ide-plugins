package com.evolveum.midpoint.eclipse.ui.tracer.views.performance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.ui.tracer.common.OpNode;
import com.evolveum.midpoint.eclipse.ui.tracer.common.PerformanceCategory;
import com.evolveum.midpoint.eclipse.ui.tracer.common.PerformanceCategoryInfo;
import com.evolveum.midpoint.xml.ns._public.common.common_3.SingleOperationPerformanceInformationType;

public class TracePerformanceView extends ViewPart implements ISelectionListener {
	private TableViewer categoriesViewer;
	private TableViewer operationsViewer;

	public TracePerformanceView() {
		super();
	}

	public void setFocus() {
//		text.setFocus();
	}

	public void createPartControl(Composite parent) {

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);

		categoriesViewer = new TableViewer(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		TableViewerColumn colCategory = new TableViewerColumn(categoriesViewer, SWT.NONE);
		colCategory.getColumn().setWidth(200);
		colCategory.getColumn().setText("Category");
		colCategory.setLabelProvider(new ColumnLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return ((Map.Entry<PerformanceCategory, PerformanceCategoryInfo>) element).getKey().getLabel();
			}
		});

		TableViewerColumn colSubCount = new TableViewerColumn(categoriesViewer, SWT.RIGHT);
		colSubCount.getColumn().setWidth(50);
		colSubCount.getColumn().setText("Total #");
		colSubCount.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(getCount(element));
			}
			@SuppressWarnings("unchecked")
			private int getCount(Object element) {
				return ((Map.Entry<PerformanceCategory, PerformanceCategoryInfo>) element).getValue().getTotalCount();
			}
			@Override
			public Color getForeground(Object element) {
				return getColor(getCount(element));
			}
		});

		TableViewerColumn colSubTime = new TableViewerColumn(categoriesViewer, SWT.RIGHT);
		colSubTime.getColumn().setWidth(70);
		colSubTime.getColumn().setText("Total time");
		colSubTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(getTime(element));
			}
			@SuppressWarnings("unchecked")
			private long getTime(Object element) {
				return ((Map.Entry<PerformanceCategory, PerformanceCategoryInfo>) element).getValue().getTotalTime();
			}
			@Override
			public Color getForeground(Object element) {
				return getColor(getTime(element));
			}
		});

		TableViewerColumn colOwnCount = new TableViewerColumn(categoriesViewer, SWT.RIGHT);
		colOwnCount.getColumn().setWidth(50);
		colOwnCount.getColumn().setText("Own #");
		colOwnCount.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(getCount(element));
			}
			@SuppressWarnings("unchecked")
			private int getCount(Object element) {
				return ((Map.Entry<PerformanceCategory, PerformanceCategoryInfo>) element).getValue().getOwnCount();
			}
			@Override
			public Color getForeground(Object element) {
				return getColor(getCount(element));
			}
		});

		TableViewerColumn colOwnTime = new TableViewerColumn(categoriesViewer, SWT.RIGHT);
		colOwnTime.getColumn().setWidth(70);
		colOwnTime.getColumn().setText("Own time");
		colOwnTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(getTime(element));
			}
			@SuppressWarnings("unchecked")
			private long getTime(Object element) {
				return ((Map.Entry<PerformanceCategory, PerformanceCategoryInfo>) element).getValue().getOwnTime();
			}
			@Override
			public Color getForeground(Object element) {
				return getColor(getTime(element));
			}
		});

		// make lines and header visible
		final Table table = categoriesViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		categoriesViewer.setContentProvider(ArrayContentProvider.getInstance());

		operationsViewer = new TableViewer(sash, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		TableViewerColumn colName = new TableViewerColumn(operationsViewer, SWT.NONE);
		colName.getColumn().setWidth(500);
		colName.getColumn().setText("Operation");
		colName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SingleOperationPerformanceInformationType) element).getName();
			}
		});

		TableViewerColumn colCount = new TableViewerColumn(operationsViewer, SWT.RIGHT);
		colCount.getColumn().setWidth(50);
		colCount.getColumn().setText("Count");
		colCount.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((SingleOperationPerformanceInformationType) element).getInvocationCount());
			}
		});

		TableViewerColumn colTotalTime = new TableViewerColumn(operationsViewer, SWT.RIGHT);
		colTotalTime.getColumn().setWidth(100);
		colTotalTime.getColumn().setText("Total time");
		colTotalTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(((SingleOperationPerformanceInformationType) element).getTotalTime());
			}
		});

		TableViewerColumn colMinTime = new TableViewerColumn(operationsViewer, SWT.RIGHT);
		colMinTime.getColumn().setWidth(50);
		colMinTime.getColumn().setText("Min");
		colMinTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(((SingleOperationPerformanceInformationType) element).getMinTime());
			}
		});

		TableViewerColumn colMaxTime = new TableViewerColumn(operationsViewer, SWT.RIGHT);
		colMaxTime.getColumn().setWidth(50);
		colMaxTime.getColumn().setText("Max");
		colMaxTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(((SingleOperationPerformanceInformationType) element).getMaxTime());
			}
		});

		TableViewerColumn colAvgTime = new TableViewerColumn(operationsViewer, SWT.RIGHT);
		colAvgTime.getColumn().setWidth(50);
		colAvgTime.getColumn().setText("Avg");
		colAvgTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				SingleOperationPerformanceInformationType op = (SingleOperationPerformanceInformationType) element;
				return formatTime(op.getTotalTime() / op.getInvocationCount());
			}
		});

		// make lines and header visible
		final Table table2 = operationsViewer.getTable();
		table2.setHeaderVisible(true);
		table2.setLinesVisible(true);

		operationsViewer.setContentProvider(ArrayContentProvider.getInstance());

		sash.setWeights(new int[] { 2, 5 });

		getViewSite().getPage().addSelectionListener(this);
	}

	public static String formatTime(Long time) {
		if (time == null) {
			return "";
		} else {
			return String.format(Locale.US, "%.1f", time / 1000.0);
		}
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof OpNode) {
				OpNode firstNode = ((OpNode) first);
				List<Map.Entry<PerformanceCategory, PerformanceCategoryInfo>> categories = new ArrayList<>(firstNode.getPerformanceByCategory().entrySet());
				categories.sort(Comparator.comparing(e -> e.getKey()));
				categoriesViewer.setInput(categories);
				
				List<SingleOperationPerformanceInformationType> operations = new ArrayList<>(firstNode.getPerformance().getOperation());
				operations.sort(Comparator.comparing(SingleOperationPerformanceInformationType::getName));
				operationsViewer.setInput(operations);
			}
		}
	}
	
	public static Color getColor(long value) {
		if (value != 0) {
			return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		} else {
			return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		}
	}

}