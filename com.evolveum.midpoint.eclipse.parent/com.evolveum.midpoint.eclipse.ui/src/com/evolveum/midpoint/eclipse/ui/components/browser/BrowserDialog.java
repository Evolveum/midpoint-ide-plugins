package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.QueryInterpretation;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.resp.SearchObjectsServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class BrowserDialog extends TitleAreaDialog {

	private static final int SHOW_ID = IDialogConstants.CLIENT_ID+1;
	private static final int DOWNLOAD_ID = IDialogConstants.CLIENT_ID+2;
	private static final int GENERATE_ID = IDialogConstants.CLIENT_ID+3;
	
	private Text txtQuery;
	private ListViewer listTypes;

	private Text txtLimit;
	private Button btnNamesAndOids;
	private Button btnNames;
	private Button btnOids;
	private Button btnQuery;

	private TreeViewer treeResults;
	
	private Button btnSearch;
	private Button btnShow;
	private Button btnDownload;
	private Button btnGenerate;
	private Button btnExecute;
	
	private Button btnSymbolicReferences;
	private Button btnWrapActions;
	private Button btnUseOriginalQuery;
	
	private Combo comboWhatToGenerate;
	private Combo comboUseProject;
	private List<IProject> projects;
	private IProject initialProject;
	
	private String initialText;
	private Label lblResult;

	public BrowserDialog(Shell parentShell, ISelection selection) {
		super(parentShell);
		if (selection instanceof ITextSelection) {
			String t = ((ITextSelection) selection).getText();
			if (t != null) {
				t = t.trim();
				if (t.startsWith("\"") && t.endsWith("\"") || t.startsWith("'") && t.endsWith("'")) {
					initialText = t.substring(1, t.length()-1);
				} else {
					initialText = t;
				}
			}
		} else {
			List<IFile> files = SelectionUtils.getXmlFiles(selection);
			if (!files.isEmpty()) {
				initialProject = files.get(0).getProject();
			}
		}
	}

	@Override
	public void create() {
		super.create();
		setTitle("Browse object on midPoint server: " + PluginPreferences.getSelectedServerName());
		setMessage("Please enter search criteria", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, true);
		container.setLayout(layout);
		
		createNameOrOid(container);
		createObjectTypes(container);
		createInterpretButtons(container);
		createSearchButton(container);
		createResult(container);
		createOptions(container);
		
		return area;
	}

	private void createNameOrOid(Composite container) {
		Composite c = new Composite(container, SWT.NONE);
		//c.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
		c.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, true, 1, 1));
		c.setLayout(new GridLayout(1, false));

		Label label = new Label(c, SWT.NONE);
		label.setText("Names or OIDs (one per line); or an XML query");
		
		GridData gd1 = new GridData();
		gd1.grabExcessHorizontalSpace = true;
		gd1.minimumWidth = 150;
		gd1.widthHint = 300;
		gd1.heightHint = 200;
		//gd1.minimumHeight = 50;
		
		txtQuery = new Text(c, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		txtQuery.setLayoutData(gd1);
		if (initialText != null) {
			txtQuery.setText(initialText);
		}
	}
	
	private void createObjectTypes(Composite container) {
		
		Composite c = new Composite(container, SWT.NONE);
		c.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, true, true, 1, 1));
		c.setLayout(new GridLayout(1, false));
		
		Label label = new Label(c, SWT.NONE);
		label.setText("Object types");

//		GridData gd1 = new GridData();
//		gd1.grabExcessHorizontalSpace = true;
//		gd1.horizontalAlignment = GridData.FILL;
//		label.setLayoutData(gd1);

		GridData gd2 = new GridData();
		gd2.grabExcessHorizontalSpace = true;
		gd2.minimumWidth = 150;
		gd2.widthHint = 300;
		gd2.heightHint = 200;
		//gd2.minimumHeight = 50;

//		GridData gd2 = new GridData();
//		gd2.grabExcessHorizontalSpace = true;
//		gd2.horizontalAlignment = GridData.FILL;
//		gd2.horizontalSpan = 1;
//		gd2.verticalSpan = ROWS_FOR_TYPES-1;
//		gd2.heightHint = 200;
//		gd2.minimumHeight = 100;

		listTypes = new ListViewer(c, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		listTypes.setContentProvider(new ArrayContentProvider());
		listTypes.setLabelProvider(new ObjectTypesListLabelProvider());
		listTypes.setInput(ObjectTypes.values());
		listTypes.getControl().setLayoutData(gd2);
	}
	
	private void createInterpretButtons(Composite container) {
		
		//c.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
		
		Group group1 = new Group(container, SWT.SHADOW_IN);
		group1.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 2, 1));

		group1.setText("How to interpret object specification");
		group1.setLayout(new RowLayout(SWT.HORIZONTAL));

		btnNamesAndOids = new Button(group1, SWT.RADIO);
		btnNamesAndOids.setText("Names and OIDs");
		btnNames = new Button(group1, SWT.RADIO);
		btnNames.setText("Names");
		btnOids = new Button(group1, SWT.RADIO);
		btnOids.setText("OIDs");
		btnQuery = new Button(group1, SWT.RADIO);
		btnQuery.setText("XML query");
		
		if (initialText != null && initialText.trim().startsWith("<") && initialText.trim().endsWith(">")) {
			btnQuery.setSelection(true);
		} else {
			btnNamesAndOids.setSelection(true);
		}
		
	}

	private void createSearchButton(Composite container) {
		
		Composite c = new Composite(container, SWT.NONE);
		//c.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
		c.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, true, 2, 1));
		c.setLayout(new GridLayout(3, false));
		
		Label l2 = new Label(c, SWT.NONE);
		l2.setText("Max # of objects to show");
		
		txtLimit = new Text(c, SWT.BORDER);
		txtLimit.setLayoutData(new GridData(100, SWT.DEFAULT));
		txtLimit.setText("1000");
		txtLimit.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event e) {
				try {
					int i = Integer.parseInt(e.text);
					if (i <= 0) {
						e.doit = false;
					}
				} catch (NumberFormatException ex) {
					e.doit = false;
				}
			}
		});

		btnSearch = new Button(c, SWT.NONE);
		btnSearch.setText("Search");
		btnSearch.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1));
		btnSearch.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchPerformed(); 
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				searchPerformed();
			}
		});
		
	}

	protected void searchPerformed() {
		String query = txtQuery.getText();
		List<ObjectTypes> types = ((IStructuredSelection)listTypes.getSelection()).toList();
		
		QueryInterpretation interpretation;
		if (btnNamesAndOids.getSelection()) {
			interpretation = QueryInterpretation.NAMES_AND_OIDS;
		} else if (btnNames.getSelection()) {
			interpretation = QueryInterpretation.NAMES;
		} else if (btnOids.getSelection()) {
			interpretation = QueryInterpretation.OIDS;
		} else if (btnQuery.getSelection()) {
			interpretation = QueryInterpretation.XML_QUERY;
		} else {
			Console.logError("Query interpretation is not known");
			return;
		}

		int limit = Integer.parseInt(txtLimit.getText());

		Console.log("Searching for: " + query + " in " + types + " (interpretation: " + interpretation + ")");

		Job job = new Job("Searching for objects") {
			protected IStatus run(IProgressMonitor monitor) {

				ConnectionParameters connectionParameters = PluginPreferences.getConnectionParameters();

				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				SearchObjectsServerResponse response = runtime.getList(types, query, interpretation, limit, connectionParameters);
				if (response.isSuccess()) {
					if (response.getServerObjects().isEmpty()) {
						Util.showInformation("No objects", "There are no objects satisfying these criteria.");
					}
					LinkedHashMap<ObjectTypes,List<ServerObject>> map = createObjectMap(response.getServerObjects());
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							treeResults.setInput(getTypesFromMap(map));
							lblResult.setText(createResultText(response.getServerObjects().size()));
						}
					});
				} else {
					Util.showAndLogError("Search failed", "Search couldn't be performed: " + response.getErrorDescription(), response.getException());
				}
				
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	private Object[] getTypesFromMap(LinkedHashMap<ObjectTypes, List<ServerObject>> map) {
		return map.entrySet().toArray();
	}

	protected LinkedHashMap<ObjectTypes, List<ServerObject>> createObjectMap(List<ServerObject> serverObjects) {
		LinkedHashMap<ObjectTypes, List<ServerObject>> rv = new LinkedHashMap<>();
		for (ServerObject object : serverObjects) {
			List<ServerObject> forType = rv.get(object.getType());
			if (forType == null) {
				forType = new ArrayList<>();
				rv.put(object.getType(), forType);
			}
			forType.add(object);
		}
		return rv;
	}

	private void createResult(Composite container) {
		GridData gd1 = new GridData();
		lblResult = new Label(container, SWT.NONE);
		lblResult.setText(createResultText(null));
		gd1.horizontalSpan = 2;
		gd1.grabExcessHorizontalSpace = true;
		gd1.horizontalAlignment = GridData.FILL;
		lblResult.setLayoutData(gd1);

		GridData gd2 = new GridData();
		gd2.grabExcessHorizontalSpace = true;
		gd2.horizontalAlignment = GridData.FILL;
		gd2.horizontalSpan = 2;
		gd2.verticalSpan = 1;
		gd2.heightHint = 150;
		//gd2.minimumHeight = 50;

		treeResults = new TreeViewer(container, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
		treeResults.setLabelProvider(new ServerObjectLabelProvider());
		treeResults.setContentProvider(new ServerObjectContentProvider());
		treeResults.getControl().setLayoutData(gd2);
	}

	public String createResultText(Integer count) {
		return "Result:" + (count != null ? " " + count + " object(s)" : "");
	}
	
	private void createOptions(Composite container) {
		Composite box = new Composite(container, SWT.BORDER);
		box.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		box.setLayout(new GridLayout(2, false));
		
		Composite combos = new Composite(box, SWT.NONE);
		combos.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		combos.setLayout(new GridLayout(2, false));
//		combos.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
		
		Label labelUseProject = new Label(combos, SWT.NONE);
		labelUseProject.setText("Store objects in project");
		
		comboUseProject = new Combo(combos, SWT.DROP_DOWN | SWT.READ_ONLY);
		projects = new ArrayList<>();
		for (IProject p : SelectionUtils.getProjects()) {
			projects.add(p);
			comboUseProject.add(p.getName());
		}
		if (initialProject != null) {
			comboUseProject.setText(initialProject.getName());
		}

		Label labelGen = new Label(combos, SWT.NONE);
		labelGen.setText("Generate");
		
		comboWhatToGenerate = new Combo(combos, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboWhatToGenerate.add("Reference (targetRef)");
		comboWhatToGenerate.add("Reference (resourceRef)");
		comboWhatToGenerate.add("Reference (linkRef)");
		comboWhatToGenerate.add("Reference (connectorRef)");
		comboWhatToGenerate.add("Reference (targetRef)");
		comboWhatToGenerate.add("Assignment");
		comboWhatToGenerate.add("Query returning these objects");
		comboWhatToGenerate.add("Bulk action: enable");
		comboWhatToGenerate.add("Bulk action: disable");
		comboWhatToGenerate.add("Bulk action: modify");
		comboWhatToGenerate.add("Bulk action: recompute");
		comboWhatToGenerate.add("Bulk action: delete");
		comboWhatToGenerate.add("Task: recompute");		
		comboWhatToGenerate.add("Task: modify");
		comboWhatToGenerate.add("Task: delete");
		
		// flags
		
		Composite flags = new Composite(box, SWT.NONE);
		flags.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
//		flags.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
		flags.setLayout(new GridLayout(2, false));
		
		btnSymbolicReferences = new Button(flags, SWT.CHECK);
		new Label(flags, SWT.NONE).setText("Use symbolic references (by name or connector type)");

		btnWrapActions = new Button(flags, SWT.CHECK);
		Label lblWrapActions = new Label(flags, SWT.NONE);
		lblWrapActions.setText("Wrap created bulk actions into tasks");
		
		btnUseOriginalQuery = new Button(flags, SWT.CHECK);
		Label lblUseOriginalQuery = new Label(flags, SWT.NONE);
		lblUseOriginalQuery.setText("Use original query when generating artefact");
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnShow = createButton(parent, SHOW_ID, "Show", false);
		btnShow.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showPerformed();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				showPerformed();
			}
		});
		
		btnDownload = createButton(parent, DOWNLOAD_ID, "Download", false);
		btnDownload.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				downloadPerformed();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				downloadPerformed();
			}
		});
		btnGenerate = createButton(parent, GENERATE_ID, "Generate XML", false);
		btnExecute = createButton(parent, GENERATE_ID, "Execute", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected void showPerformed() {
		IWorkbenchWindow window = SelectionUtils.getActiveWindow();
		String string = "This is the text file contents";
		IStorage storage = new StringStorage(string);
		IStorageEditorInput input = new StringInput(storage);
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			try {
				page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
			} catch (PartInitException e) {
				Util.processUnexpectedException(e);
			}
		}
	}

	protected void downloadPerformed() {
		IResource resource = SelectionUtils.getResourceFromActiveWindow();
		Util.showAndLogWarning("Hi", "Resource: " + resource);
		
	}
}

