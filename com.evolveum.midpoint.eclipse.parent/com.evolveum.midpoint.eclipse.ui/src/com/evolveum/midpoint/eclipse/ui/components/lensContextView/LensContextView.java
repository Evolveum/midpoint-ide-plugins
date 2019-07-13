package com.evolveum.midpoint.eclipse.ui.components.lensContextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.ui.components.tracer.OpNode;
import com.evolveum.midpoint.eclipse.ui.components.tracer.ViewedObject;
import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ClockworkTraceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensElementContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensProjectionContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

public class LensContextView extends ViewPart implements ISelectionListener {
	private TreeViewer viewer;
	private Label label;
	private List<String> currentColumnLabels;
	private List<ViewedObject> objects;
	private LensContextType inputContext, outputContext;
	private List<PrismNode> roots;
	
	public LensContextView() {
		super();
	}

	public void createPartControl(Composite parent) {
		label = new Label(parent, 0);
		label.setText("Prism object: ");

		createViewer(parent);

		setViewerInput();
		
		//getSite().setSelectionProvider(viewer); 
		
		GridLayoutFactory.fillDefaults().generateLayout(parent);
		
		getViewSite().getPage().addSelectionListener(this);
	}

	public void createViewer(Composite parent) {
		viewer = new TreeViewer(parent);
		viewer.setContentProvider(new PrismNodeContentProvider());
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);

		TreeViewerColumn itemNameColumn = new TreeViewerColumn(viewer, SWT.NONE);
		itemNameColumn.getColumn().setWidth(200);
		itemNameColumn.getColumn().setText("Item");
		itemNameColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getLabel())));

		currentColumnLabels = Arrays.asList("old", "current", "new");//getColumnLabels(objects);
		for (int i = 0; i < currentColumnLabels.size(); i++) {
			TreeViewerColumn valueColumn = new TreeViewerColumn(viewer, SWT.NONE);
			valueColumn.getColumn().setWidth(100);
			valueColumn.getColumn().setText(currentColumnLabels.get(i));
			int index = i;
			valueColumn.setLabelProvider(new DelegatingStyledCellLabelProvider(new MyLabelProvider(n -> n.getValue(index))));
		}
	}
	
	public class MyLabelProvider extends LabelProvider implements IStyledLabelProvider {

		private Function<PrismNode, Object> extractor;

		public MyLabelProvider(Function<PrismNode, Object> extractor) {
			this.extractor = extractor;
		}

		@Override
		public StyledString getStyledText(Object o) {
			if (o instanceof PrismNode) {
				Object value = extractor.apply((PrismNode) o);
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
		viewer.setInput(roots != null ? roots.toArray() : null);
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		OpNode node = null;
		if (selection instanceof IStructuredSelection) {
			Object first = ((IStructuredSelection) selection).getFirstElement();
			if (first instanceof OpNode) {
				node = ((OpNode) first);
				ClockworkTraceType trace = node.getTrace(ClockworkTraceType.class);
				if (trace != null) {
					inputContext = trace.getInputLensContext();
					outputContext = trace.getOutputLensContext();
				} else {
					inputContext = null;
					outputContext = null;
				}

//				if (objects != null) {
//					List<String> newColumnLabels = getColumnLabels(objects);					
//					if (!newColumnLabels.equals(currentColumnLabels)) {
//						Tree tree = viewer.getTree();
//						Composite parent = tree.getParent();
//						tree.dispose();
//						createViewer(parent);
//						GridLayoutFactory.fillDefaults().generateLayout(parent);
//						parent.layout(true);
//					}
//				}
			} else {
//				objects = null;
				inputContext = null;
				outputContext = null;
			}
			
		}
		if (node != null && (inputContext != null || outputContext != null)) {
			OperationResultType result = node.getResult();
			label.setText(result.getOperation() + " (" + result.getInvocationId() + "): " + node.getTraceNames());
			parseContexts();
			Object input = viewer.getInput();
			if (roots != null) {
				if (input instanceof Object[] && ((Object[]) input).length == roots.size()) {
					Object[] inputArray = (Object[]) input;
					for (int i = 0; i < roots.size(); i++) {
						inputArray[i] = roots.get(i);
					}
					viewer.refresh();
				} else {
					System.out.println("Structure changed, setting new input");
					setViewerInput();
				}
			}
		}
	}

//	private List<String> getColumnLabels(List<ViewedObject> objects) {
//		return objects != null ? objects.stream().map(vo -> vo.getLabel()).collect(Collectors.toList()) : Collections.emptyList();
//	}

//	private void parse() {
//		if (objects == null || objects.isEmpty()) {
//			roots = null;
//			return;
//		}
//		try {
//			List<PrismContainerValue<?>> values = objects.stream().map(vo -> vo.getObject().getValue()).collect(Collectors.toList());
//			roots = parseContainerValue(values, null);
//		} catch (SchemaException e) {
//			e.printStackTrace();
//			roots = null;
//		}
//	}
	private void parseContexts() {
		roots = new ArrayList<>();
		roots.add(parseContext("input", inputContext));
		roots.add(parseContext("output", outputContext));
	}
	
	private LensContextNode parseContext(String label, LensContextType context) {
		return new LensContextNode(label, context, null);
	}
}