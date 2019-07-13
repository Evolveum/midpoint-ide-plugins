package com.evolveum.midpoint.eclipse.ui.tracer.views.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.evolveum.midpoint.eclipse.ui.tracer.common.OpType;
import com.evolveum.midpoint.eclipse.ui.tracer.common.OpViewType;
import com.evolveum.midpoint.eclipse.ui.tracer.common.Options;
import com.evolveum.midpoint.eclipse.ui.tracer.common.PerformanceCategory;
import com.evolveum.midpoint.eclipse.ui.tracer.editor.TracerViewerEditor;
import com.evolveum.midpoint.eclipse.ui.tracer.other.TraceAnalyzerView;

public class TraceOptionsView extends ViewPart {
	private Map<OpType, Button> typeBoxes = new HashMap<>();
	private Map<PerformanceCategory, Button> categoriesBoxes = new HashMap<>();
	private Button alsoParentsButton;
	private Button perfColumnsButton;
	private Button applyButton;

	public TraceOptionsView() {
		super();
	}

	public void setFocus() {
//		text.setFocus();
	}

	public void createPartControl(Composite parent) {
		
		RowLayout parentLayout = new RowLayout();
//		parentLayout.type = SWT.VERTICAL;
		parent.setLayout(parentLayout);

		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		String[] items = Arrays.asList(OpViewType.values()).stream()
				.map(type -> type.getLabel()).toArray(String[]::new);
		combo.setItems(items);
		combo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = combo.getSelectionIndex();
				if (i >= 0) {
					applyOpViewType(OpViewType.values()[i]);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Group buttonsGroup = new Group(parent, SWT.NULL);
		FillLayout buttonsLayout = new FillLayout();
		buttonsLayout.type = SWT.HORIZONTAL;
		buttonsGroup.setLayout(buttonsLayout);
		
		Group eventsGroup = new Group(buttonsGroup, SWT.SHADOW_IN);
		eventsGroup.setText("Events to show");
		GridLayout eventsLayout = new GridLayout();
		eventsLayout.numColumns = 1;
	    eventsGroup.setLayout(eventsLayout);
//	    eventsGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		for (OpType type : OpType.values()) {
			Button checkBox = new Button(eventsGroup, SWT.CHECK);
			checkBox.setText(type.getLabel());
			checkBox.setSelection(true);
			typeBoxes.put(type, checkBox);
		}
		
		Group categoriesGroup = new Group(buttonsGroup, SWT.SHADOW_IN);
		categoriesGroup.setText("Categories to show");
		GridLayout categoriesLayout = new GridLayout();
		eventsLayout.numColumns = 1;
		categoriesGroup.setLayout(categoriesLayout);
//		categoriesGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		for (PerformanceCategory category : PerformanceCategory.values()) {
			Button checkBox = new Button(categoriesGroup, SWT.CHECK);
			checkBox.setText(category.getLabel());
			checkBox.setSelection(true);
			categoriesBoxes.put(category, checkBox);
		}
		Label separator = new Label(categoriesGroup, SWT.HORIZONTAL | SWT.SEPARATOR);
//	    separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    alsoParentsButton = new Button(categoriesGroup, SWT.CHECK);
	    alsoParentsButton.setText("Show also parents");
	    alsoParentsButton.setSelection(true);

	    perfColumnsButton = new Button(categoriesGroup, SWT.CHECK);
	    perfColumnsButton.setText("Show performance columns");
	    perfColumnsButton.setSelection(true);

	    applyButton = new Button(parent, SWT.PUSH);
	    applyButton.setText("Apply");
	    applyButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Options options = createOptions();
//				TraceAnalyzerView analyzerView = getAnalyzerView();
//				if (analyzerView != null) {
//					analyzerView.applyOptions(options);
//				}
				for (TracerViewerEditor editor : getEditors()) {
					editor.applyOptions(options);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
	    });
	}

	private Options createOptions() {
		Options rv = new Options();
		for (Map.Entry<OpType, Button> e : typeBoxes.entrySet()) {
			if (e.getValue().getSelection()) {
				rv.getTypesToShow().add(e.getKey());
			}
		}
		for (Map.Entry<PerformanceCategory, Button> e : categoriesBoxes.entrySet()) {
			if (e.getValue().getSelection()) {
				rv.getCategoriesToShow().add(e.getKey());
			}
		}
		rv.setShowAlsoParents(alsoParentsButton.getSelection());
		rv.setShowPerformanceColumns(perfColumnsButton.getSelection());
		return rv;
	}

	private TraceAnalyzerView getAnalyzerView() {
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
            IViewPart viewPart = activePage.showView("com.evolveum.midpoint.eclipse.ui.views.trace.analyzer");
            return (TraceAnalyzerView) viewPart;
        } catch (PartInitException e) {
            return null;
        }
	}

	private List<TracerViewerEditor> getEditors() {
		List<TracerViewerEditor> rv = new ArrayList<>();
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
		    for (IWorkbenchPage page : window.getPages()) {
		        for (IEditorReference editorRef : page.getEditorReferences()) {
		        	System.out.println("Found editorRef: " + editorRef);
		        	IEditorPart editor = editorRef.getEditor(false);
		        	System.out.println("--> " + editor);
		        	if (editor instanceof TracerViewerEditor) {
		        		rv.add((TracerViewerEditor) editor);
		        	}
		        }
		    }
		}
		return rv;
	}
	
	private void applyOpViewType(OpViewType opViewType) {
		typeBoxes.forEach((type, button) -> 
		button.setSelection(opViewType.getTypes() == null || opViewType.getTypes().contains(type)));
		categoriesBoxes.forEach((category, button) -> 
		button.setSelection(opViewType.getCategories() == null || opViewType.getCategories().contains(category)));
		alsoParentsButton.setSelection(opViewType.isShowAlsoParents());
		perfColumnsButton.setSelection(opViewType.isShowPerformanceColumns());
	}


}