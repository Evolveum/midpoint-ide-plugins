package com.evolveum.midpoint.eclipse.ui.components.tracer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.xml.ns._public.common.common_3.SingleOperationPerformanceInformationType;

public class TracePerformanceView extends ViewPart implements ISelectionListener {
	private TableViewer viewer;

	public TracePerformanceView() {
		super();
	}

	public void setFocus() {
//		text.setFocus();
	}

	public void createPartControl(Composite parent) {

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		TableViewerColumn colName = new TableViewerColumn(viewer, SWT.NONE);
		colName.getColumn().setWidth(500);
		colName.getColumn().setText("Operation");
		colName.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((SingleOperationPerformanceInformationType) element).getName();
			}
		});

		TableViewerColumn colCount = new TableViewerColumn(viewer, SWT.RIGHT);
		colCount.getColumn().setWidth(50);
		colCount.getColumn().setText("Count");
		colCount.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return String.valueOf(((SingleOperationPerformanceInformationType) element).getInvocationCount());
			}
		});

		TableViewerColumn colTotalTime = new TableViewerColumn(viewer, SWT.RIGHT);
		colTotalTime.getColumn().setWidth(100);
		colTotalTime.getColumn().setText("Total time");
		colTotalTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(((SingleOperationPerformanceInformationType) element).getTotalTime());
			}
		});

		TableViewerColumn colMinTime = new TableViewerColumn(viewer, SWT.RIGHT);
		colMinTime.getColumn().setWidth(50);
		colMinTime.getColumn().setText("Min");
		colMinTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(((SingleOperationPerformanceInformationType) element).getMinTime());
			}
		});

		TableViewerColumn colMaxTime = new TableViewerColumn(viewer, SWT.RIGHT);
		colMaxTime.getColumn().setWidth(50);
		colMaxTime.getColumn().setText("Max");
		colMaxTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return formatTime(((SingleOperationPerformanceInformationType) element).getMaxTime());
			}
		});

		TableViewerColumn colAvgTime = new TableViewerColumn(viewer, SWT.RIGHT);
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
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(ArrayContentProvider.getInstance());

		getViewSite().getPage().addSelectionListener(this);
	}

	protected String formatTime(Long time) {
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
				List<SingleOperationPerformanceInformationType> operations = new ArrayList<>(firstNode.getPerformance().getOperation());
				operations.sort(Comparator.comparing(SingleOperationPerformanceInformationType::getName));
				viewer.setInput(operations);
			}
		}
	}

}