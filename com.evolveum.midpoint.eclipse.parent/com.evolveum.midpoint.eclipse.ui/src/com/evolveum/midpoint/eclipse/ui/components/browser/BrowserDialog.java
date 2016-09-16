package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.QueryInterpretation;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerAction;
import com.evolveum.midpoint.eclipse.runtime.api.resp.SearchObjectsServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.ui.handlers.ResourceUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.server.DownloadHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler.RequestedAction;
import com.evolveum.midpoint.eclipse.ui.handlers.server.ServerRequestItem;
import com.evolveum.midpoint.eclipse.ui.handlers.server.ServerRequestPack;
import com.evolveum.midpoint.eclipse.ui.handlers.server.ServerResponseItem;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class BrowserDialog extends TitleAreaDialog {

	private static final int SHOW_ID = IDialogConstants.CLIENT_ID+1;
	private static final int DOWNLOAD_ID = IDialogConstants.CLIENT_ID+2;
	private static final int GENERATE_ID = IDialogConstants.CLIENT_ID+3;
	
	private static final int INITIAL_QUERY_HEIGHT = 156;
	private static final int INITIAL_RESULTS_HEIGHT = 156;
	
	private Text txtQuery;
	private ListViewer listTypes;

	private Text txtLimit;
	private Text txtOffset;
	private Button btnNamesAndOids;
	private Button btnNames;
	private Button btnOids;
	private Button btnQuery;

	private TreeViewer treeResults;
	private Integer objectCount;
	
	private Button btnSearch;
	private Button btnShow;
	private Button btnDownload;
	private Button btnGenerate;
	private Button btnExecute;
	
	private Button btnSymbolicReferences;
	private Button btnRunTimeResolution;
	private Button btnWrapActions;
	private Button btnUseOriginalQuery;
	
	private Combo comboWhatToGenerate;
	private Combo comboUseProject;
	private List<IProject> projects;
	private IProject initialProject;
	
	private String initialText;
	private Label lblResult;
	private Button btnConvertToXml;

	private IWorkbenchPage page;
	
	private List<Generator> generators = Arrays.asList(
			new BulkActionGenerator(BulkActionGenerator.Action.RECOMPUTE),
			new BulkActionGenerator(BulkActionGenerator.Action.ENABLE),
			new BulkActionGenerator(BulkActionGenerator.Action.DISABLE),
			new BulkActionGenerator(BulkActionGenerator.Action.DELETE),
			new BulkActionGenerator(BulkActionGenerator.Action.MODIFY),
			new BulkActionGenerator(BulkActionGenerator.Action.EXECUTE_SCRIPT),
			new BulkActionGenerator(BulkActionGenerator.Action.LOG),
			new BulkActionGenerator(BulkActionGenerator.Action.TEST_RESOURCE),
			new TaskGenerator(TaskGenerator.Action.RECOMPUTE),
			new TaskGenerator(TaskGenerator.Action.DELETE),
			new TaskGenerator(TaskGenerator.Action.MODIFY),
			new TaskGenerator(TaskGenerator.Action.SHADOW_CHECK),
			new QueryGenerator(),
			new AssignmentGenerator(),
			new RefGenerator("targetRef", ObjectTypes.OBJECT),
			new RefGenerator("resourceRef", ObjectTypes.RESOURCE),
			new RefGenerator("linkRef", ObjectTypes.SHADOW),
			new ConnectorRefGenerator(),
			new RefGenerator("parentOrgRef", ObjectTypes.ORG),
			new RefGenerator("ownerRef", ObjectTypes.ORG)
			);
	private Button btnExecAllByOid;
	private Button btnExecIndividually;
	private Button btnExecByN;
	private Button btnExecAllByQuery;
	private Combo comboExecution;
	private Text txtBatchSize;
	private Button btnCreateSuspended;
	private Button btnCreateRaw;
	private Button btnCreateDryRun;
	
	public BrowserDialog(Shell parentShell, ISelection selection) {
		super(parentShell);
		page = SelectionUtils.getActivePage();
		System.out.println("Page = " + page);
		
		setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
		setBlockOnOpen(false);
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
		}
		initialProject = getInitialProject(selection);
	}

	private IProject getInitialProject(ISelection selection) {
		List<IFile> files = SelectionUtils.getSelectedXmlFiles(selection);
		if (!files.isEmpty()) {
			return files.get(0).getProject();
		}
		IResource resource = SelectionUtils.getResourceFromActiveWindow();
		if (resource != null) {
			return resource.getProject();
		}
		return SelectionUtils.guessSelectedProjectFromExplorerOrNavigator();
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
		computeSearchBoxItemsEnablement();
		computeOptionsEnablement();
		
		return area;
	}

	private void createNameOrOid(Composite container) {
		Composite c = new Composite(container, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		c.setLayout(new GridLayout(1, false));

		Label label = new Label(c, SWT.NONE);
		label.setText("Names or OIDs (one per line); or an XML query");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
//		label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
		
		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);

		txtQuery = new Text(c, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		txtQuery.setLayoutData(gd2);
		if (initialText != null) {
			txtQuery.setText(initialText);
		}

	}
	
	private void createObjectTypes(Composite container) {
		
		Composite c = new Composite(container, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		c.setLayout(new GridLayout(1, false));
		
		Label label = new Label(c, SWT.NONE);
		label.setText("Object types");

		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd2.heightHint = INITIAL_QUERY_HEIGHT;

		listTypes = new ListViewer(c, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		listTypes.setContentProvider(new ArrayContentProvider());
		listTypes.setLabelProvider(new ObjectTypesListLabelProvider());
		ObjectTypes[] types = ObjectTypes.values();
		Arrays.sort(types, ObjectTypes.getDisplayNameComparator());
		listTypes.setInput(types);
		listTypes.getControl().setLayoutData(gd2);
		
		listTypes.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				searchPerformed();
			}
		});
	}
	
	private void createInterpretButtons(Composite container) {
		
		//c.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
		
		Group group1 = new Group(container, SWT.SHADOW_IN);
		group1.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

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
		btnQuery.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				computeSearchBoxItemsEnablement();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				computeSearchBoxItemsEnablement();
			}
		});
		
		if (initialText != null && initialText.trim().startsWith("<") && initialText.trim().endsWith(">")) {
			btnQuery.setSelection(true);
		} else {
			btnNamesAndOids.setSelection(true);
		}
	}

	private void createSearchButton(Composite container) {
		
		Composite c = new Composite(container, SWT.NONE);
		//c.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
		c.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		c.setLayout(new GridLayout(10, false));
		
		Label l1 = new Label(c, SWT.NONE);
		l1.setText("Max # of objects to show");
		
		txtLimit = new Text(c, SWT.BORDER);
		txtLimit.setLayoutData(new GridData(60, SWT.DEFAULT));
		txtLimit.setText("1000");

		Label l2 = new Label(c, SWT.NONE);
		l2.setText("Start at #");
		
		txtOffset = new Text(c, SWT.BORDER);
		txtOffset.setLayoutData(new GridData(60, SWT.DEFAULT));
		txtOffset.setText("0");

		btnSearch = new Button(c, SWT.NONE);
		btnSearch.setText("&Search");
		btnSearch.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
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
		
		btnConvertToXml = new Button(c, SWT.NONE);
		btnConvertToXml.setText("Convert to XML query");
		btnConvertToXml.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1, 1));
		btnConvertToXml.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				 convertToQueryPerformed();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				convertToQueryPerformed();
			}
		});
	}
	
	protected void convertToQueryPerformed() {
		String query = txtQuery.getText();
		List<ObjectTypes> types = ((IStructuredSelection)listTypes.getSelection()).toList();
		QueryInterpretation interpretation = getInterpretation();
		if (interpretation == null || interpretation == QueryInterpretation.XML_QUERY) {
			return;
		}
		int limit = getLimit();
		int offset = getOffset();
		if (limit < 0 || offset < 0) {
			return;
		}
		
		RuntimeService runtime = RuntimeActivator.getRuntimeService();
		String realQuery = runtime.createQuery(types, query, interpretation, limit, offset);
		
		txtQuery.setText(realQuery);
		btnNamesAndOids.setSelection(false);
		btnNames.setSelection(false);
		btnOids.setSelection(false);
		btnQuery.setSelection(true);
		computeSearchBoxItemsEnablement();
	}

	public int getLimit() {
		try {
			int rv = Integer.parseInt(txtLimit.getText());
			return rv < 0 ? 0 : rv;
		} catch (NumberFormatException e) {
			MessageDialog.openWarning(getShell(), "Wrong number", "Please enter a correct number for the limit of objects.");
			return -1;
		}
	}

	public int getOffset() {
		try {
			int rv = Integer.parseInt(txtOffset.getText());
			return rv < 0 ? 0 : rv;
		} catch (NumberFormatException e) {
			MessageDialog.openWarning(getShell(), "Wrong number", "Please enter a correct number for the offset.");
			return -1;
		}
	}

	public int getBatchSize() {
		try {
			int rv = Integer.parseInt(txtBatchSize.getText());
			if (rv < 1) {
				MessageDialog.openWarning(getShell(), "Wrong number", "Batch size has to be at least 1.");
				return -1;
			}
			return rv;
		} catch (NumberFormatException e) {
			MessageDialog.openWarning(getShell(), "Wrong number", "Please enter a correct number for the batch size.");
			return -1;
		}
	}

	protected void searchPerformed() {
		String query = txtQuery.getText();
		List<ObjectTypes> types = ((IStructuredSelection)listTypes.getSelection()).toList();
		
		QueryInterpretation interpretation = getInterpretation();
		if (interpretation == null) {
			return;
		}

		int limit = getLimit();
		int offset = getOffset();
		if (limit < 0 || offset < 0) {
			return;
		}

		Console.logMinor("Searching for: " + query + " in " + types + " (interpretation: " + interpretation + ")");

		Job job = new Job("Searching for objects") {
			protected IStatus run(IProgressMonitor monitor) {

				ConnectionParameters connectionParameters = PluginPreferences.getConnectionParameters();

				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				SearchObjectsServerResponse response = runtime.listObjects(types, query, interpretation, limit, offset, connectionParameters);
				if (response.isSuccess()) {
					if (response.getServerObjects().isEmpty()) {
						Util.showInformation("No objects", "There are no objects satisfying these criteria.");
					}
					Map<ObjectTypes,List<ServerObject>> map = createObjectMap(response.getServerObjects());
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							objectCount = response.getServerObjects().size();
							treeResults.setInput(getTypesFromMap(map));
							lblResult.setText(createResultText(objectCount, 0));
							
							if (map.keySet().size() == 1) {
								treeResults.expandAll();
							}
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

	public QueryInterpretation getInterpretation() {
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
			interpretation = null;
		}
		return interpretation;
	}

	private Object[] getTypesFromMap(Map<ObjectTypes, List<ServerObject>> map) {
		return map.entrySet().toArray();
	}

	protected TreeMap<ObjectTypes, List<ServerObject>> createObjectMap(List<ServerObject> serverObjects) {
		TreeMap<ObjectTypes, List<ServerObject>> rv = new TreeMap<>(ObjectTypes.getDisplayNameComparator());
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
		lblResult = new Label(container, SWT.NONE);
		lblResult.setText(createResultText(null, null));
		lblResult.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 2, 1));

		GridData gd2 = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
		gd2.heightHint = INITIAL_RESULTS_HEIGHT;

		treeResults = new TreeViewer(container, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL );
		Tree tree = treeResults.getTree();
		tree.setHeaderVisible(true);
		
		TreeColumn cName = new TreeColumn(tree, SWT.LEFT);
		tree.setLinesVisible(true);
		cName.setAlignment(SWT.LEFT);
		cName.setText("Name");
		cName.setWidth(300);
		
		TreeColumn cDisplayName = new TreeColumn(tree, SWT.LEFT);
		cDisplayName.setAlignment(SWT.LEFT);
		cDisplayName.setText("Display name");
		cDisplayName.setWidth(200);
		
		TreeColumn cSubType = new TreeColumn(tree, SWT.LEFT);
		cSubType.setAlignment(SWT.LEFT);
		cSubType.setText("Subtype");
		cSubType.setWidth(150);

		TreeColumn cOid = new TreeColumn(tree, SWT.LEFT);
		cOid.setAlignment(SWT.LEFT);
		cOid.setText("OID");
		cOid.setWidth(300);
		
		treeResults.setLabelProvider(new ServerObjectLabelProvider());
		treeResults.setContentProvider(new ServerObjectContentProvider());
		treeResults.getControl().setLayoutData(gd2);
		treeResults.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				actionButtonsRelatedSelectionChanged();
				lblResult.setText(createResultText(objectCount, getSelectedOids().size()));
			}
		});
		treeResults.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				showPerformed();
			}
		});
	}

	public String createResultText(Integer count, Integer selected) {
		StringBuilder sb = new StringBuilder();
		sb.append("Result:");
		if (count != null) {
			sb.append(" ").append(count).append(" objects(s)");
			if (selected != null) {
				sb.append(", ").append(selected).append(" selected");
			}
		}
		return sb.toString();
	}
	
	private void createOptions(Composite container) {
		Composite box = new Composite(container, SWT.BORDER);
		box.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		box.setLayout(new GridLayout(2, false));
		
		Composite combos = new Composite(box, SWT.NONE);
		combos.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		combos.setLayout(new GridLayout(2, false));
		
		Label labelUseProject = new Label(combos, SWT.NONE);
		labelUseProject.setText("Store objects in project");
		
		comboUseProject = new Combo(combos, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboUseProject.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		projects = new ArrayList<>();
		for (IProject p : SelectionUtils.getProjects()) {
			projects.add(p);
			comboUseProject.add(p.getName());
		}
		if (initialProject != null) {
			comboUseProject.setText(initialProject.getName());
		}
		comboUseProject.addModifyListener(new ActionButtonsRelatedModifyListener());

		Label labelGen = new Label(combos, SWT.NONE);
		labelGen.setText("Generate");
		
		comboWhatToGenerate = new Combo(combos, SWT.DROP_DOWN | SWT.READ_ONLY);
		comboWhatToGenerate.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		for (Generator g : generators) {
			comboWhatToGenerate.add(g.getLabel());
		}
		comboWhatToGenerate.addModifyListener(new ActionButtonsRelatedModifyListener());
		
		Label labelExec = new Label(combos, SWT.NONE);
		labelExec.setText("Execution");
		
		Composite execution = new Composite(combos, SWT.NONE);
		execution.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		execution.setLayout(layout);
		
		comboExecution = new Combo(execution, SWT.DROP_DOWN | SWT.READ_ONLY);
		String defVal = "By OIDs, in one batch";
		comboExecution.add(defVal);
		comboExecution.setText(defVal);
		comboExecution.add("By OIDs, one after one");
		comboExecution.add("By OIDs, in batches of N");
		//comboExecution.add("Using original query (ignoring selection)");
		comboExecution.add("Using original query");
		comboExecution.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				computeTxtBatchSizeIsEnabled();
			}
		});
		
		new Label(execution, SWT.NONE).setText("N = ");
		txtBatchSize = new Text(execution, SWT.BORDER);
		txtBatchSize.setLayoutData(new GridData(30, SWT.DEFAULT));
		txtBatchSize.setText("100");
		txtBatchSize.setEnabled(false);

		// flags
		
		Composite flags = new Composite(box, SWT.NONE);
		flags.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
//		flags.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
		flags.setLayout(new GridLayout(2, false));
		
		Composite symrefOptions = new Composite(flags, SWT.NONE);
		symrefOptions.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		GridLayout symrefLayOptions = new GridLayout(4, false);
		symrefLayOptions.marginWidth = 0;
		symrefLayOptions.marginHeight = 0;
		symrefOptions.setLayout(symrefLayOptions);
		
		btnSymbolicReferences = new Button(symrefOptions, SWT.CHECK);
		new Label(symrefOptions, SWT.NONE).setText("Use symbolic references (by name or connector type)");

		btnRunTimeResolution = new Button(symrefOptions, SWT.CHECK);
		new Label(symrefOptions, SWT.NONE).setText("Runtime resolution");

		btnWrapActions = new Button(flags, SWT.CHECK);
		btnWrapActions.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				computeOptionsEnablement();				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				computeOptionsEnablement();
			}
		});
		Label lblWrapActions = new Label(flags, SWT.NONE);
		lblWrapActions.setText("Wrap created bulk actions into tasks");
		
		btnCreateSuspended = new Button(flags, SWT.CHECK);
		Label lblCreateSuspended = new Label(flags, SWT.NONE);
		lblCreateSuspended.setText("Create tasks in suspended state");
		
		Composite options = new Composite(flags, SWT.NONE);
		options.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		GridLayout layOptions = new GridLayout(4, false);
		layOptions.marginWidth = 0;
		layOptions.marginHeight = 0;
		options.setLayout(layOptions);
		
		btnCreateRaw = new Button(options, SWT.CHECK);
		new Label(options, SWT.NONE).setText("Execute in raw mode");
		btnCreateDryRun = new Button(options, SWT.CHECK);
		new Label(options, SWT.NONE).setText("Execute in 'dry run' mode");
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}

	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		btnShow = createButton(parent, SHOW_ID, "S&how", false);
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
		btnShow.setEnabled(false);
		
		btnDownload = createButton(parent, DOWNLOAD_ID, "&Download", false);
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
		btnDownload.setEnabled(false);
		
		btnGenerate = createButton(parent, GENERATE_ID, "&Generate XML", false);
		btnGenerate.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				generatePerformed();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				generatePerformed();
			}
		});
		btnGenerate.setEnabled(false);
		
		btnExecute = createButton(parent, GENERATE_ID, "&Execute", false);
		btnExecute.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				executePerformed();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				executePerformed();
			}
		});
		btnExecute.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

//	protected void showPerformed() {
//		IWorkbenchWindow window = SelectionUtils.getActiveWindow();
//		String string = "This is the text file contents";
//		IStorage storage = new StringStorage(string);
//		IStorageEditorInput input = new StringInput(storage);
//		IWorkbenchPage page = window.getActivePage();
//		if (page != null) {
//			try {
//				page.openEditor(input, "org.eclipse.ui.DefaultTextEditor");
//			} catch (PartInitException e) {
//				Util.processUnexpectedException(e);
//			}
//		}
//	}

	private IProject getProject() {
		int project = comboUseProject.getSelectionIndex();
		return project < 0 ? null : projects.get(project);
	}
	
	protected void showPerformed() {
		IProject project = getProject();
		if (project == null) {
			return;
		}
		List<String> selectedOids = getSelectedOids();
		if (selectedOids.isEmpty()) {
			return;
		}
		Job job = new Job("Downloading from midPoint") {
			protected IStatus run(IProgressMonitor monitor) {
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				SearchObjectsServerResponse serverResponse = runtime.downloadObjects(selectedOids, PluginPreferences.getConnectionParameters());
				if (!serverResponse.isSuccess()) {
					Console.logError("Couldn't download selected objects: " + serverResponse.getErrorDescription(), serverResponse.getException());
				} else {
					String content = new ShowGenerator().generate(serverResponse.getServerObjects(), new GeneratorOptions());
					if (content == null) {
						return Status.OK_STATUS;
					}
					IFile file = writeFile(content, project, "out", monitor);
					if (file != null) {
						openFileInEditor(file);
					}
					return Status.OK_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected void generatePerformed() {
		IProject project = getProject();
		if (project == null) {
			return;
		}
		List<ServerObject> selectedObjects = getSelectedObjects();
		if (selectedObjects.size() == 0) {
			return;
		}
		int genIndex = comboWhatToGenerate.getSelectionIndex();
		if (genIndex < 0) {
			return;
		}
		GeneratorOptions options = createGeneratorOptions(selectedObjects);
		if (!options.isBatchByOids()) {
			return;
		}

		Generator generator = generators.get(genIndex);
		Job job = new Job("Generating XML") {
			protected IStatus run(IProgressMonitor monitor) {
				String content = generator.generate(selectedObjects, options);
				if (content == null) {
					return Status.OK_STATUS;
				}
				IFile file = writeFile(content, project, "out", monitor);
				if (file != null) {
					openFileInEditor(file);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}

	public GeneratorOptions createGeneratorOptions(List<ServerObject> selectedObjects) {
		GeneratorOptions options = new GeneratorOptions();
		options.setSymbolicReferences(btnSymbolicReferences.getSelection());
		options.setSymbolicReferencesRuntime(btnRunTimeResolution.getSelection());
		options.setWrapActions(btnWrapActions.getSelection());
		options.setCreateSuspended(btnCreateSuspended.getSelection());
		options.setRaw(btnCreateRaw.getSelection());
		options.setDryRun(btnCreateDryRun.getSelection());
		int execOption = comboExecution.getSelectionIndex();
		switch (execOption) {
		case 0: options.setBatchByOids(true); options.setBatchSize(selectedObjects.size()); break;
		case 1: options.setBatchByOids(true); options.setBatchSize(1); break;
		case 2: options.setBatchByOids(true); options.setBatchSize(getBatchSize()); break;
		case 3: Util.showWarning("Not implemented yet", "This feature is not yet implemented"); break;
		}
		return options;
	}

	protected void executePerformed() {
		IProject project = getProject();
		if (project == null) {
			return;
		}
		List<ServerObject> selectedObjects = getSelectedObjects();
		if (selectedObjects.size() == 0) {
			return;
		}
		int genIndex = comboWhatToGenerate.getSelectionIndex();
		if (genIndex < 0) {
			return;
		}
		GeneratorOptions options = createGeneratorOptions(selectedObjects);
		if (!options.isBatchByOids()) {
			return;
		}

		Generator generator = generators.get(genIndex);
		Job job = new Job("Generating XML") {
			protected IStatus run(IProgressMonitor monitor) {
				String content = generator.generate(selectedObjects, options);
				if (content == null) {
					return Status.OK_STATUS;
				}
				IFile file = writeFile(content, project, "exec", monitor);
				if (file == null) {
					return Status.OK_STATUS;
				}
				List<SourceObject> sourceObjects = ServerRequestPack.fromWorkspaceFiles(Collections.singletonList(file));
				if (sourceObjects.size() == 0) {
					Util.showAndLogWarning("No objects to upload/execute", "There are no objects to be executed (huh?)");
					return Status.OK_STATUS;
				}
				List<ServerRequestItem> items = new ArrayList<>();
				for (SourceObject object : sourceObjects) {
					if (!object.isExecutable() && !object.isUploadable()) {
						continue;
					}
					items.add(new ServerRequestItem(object.isExecutable() ? ServerAction.EXECUTE : ServerAction.UPLOAD, object));					
				}
				ServerRequestPack requestPack = new ServerRequestPack(items);
				FileRequestHandler.executePack(requestPack, RequestedAction.UPLOAD_OR_EXECUTE, false, monitor);
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
	}

	protected void openFileInEditor(IFile file) {
		if (page != null) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					try {
						IDE.openEditor(page, file);
					} catch (PartInitException e) {
						Util.processUnexpectedException(e);
					}					
				}
			});
		} else {
			Console.logError("No active page...");
		}
	}

	protected void downloadPerformed() {
		int project = comboUseProject.getSelectionIndex();
		if (project < 0) {
//			MessageDialog.openInformation(getShell(), "No project selected", "Please select a project where downloaded objects should be stored.");
			return;
		}
		List<String> selectedOids = getSelectedOids();
		if (selectedOids.isEmpty()) {
//			MessageDialog.openInformation(getShell(), "No objects selected", "Please select object(s) to download.");
			return;
		}
		Job job = new Job("Downloading from midPoint") {
			protected IStatus run(IProgressMonitor monitor) {
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				SearchObjectsServerResponse serverResponse = runtime.downloadObjects(selectedOids, PluginPreferences.getConnectionParameters());
				if (!serverResponse.isSuccess()) {
					Console.logError("Couldn't download selected objects: " + serverResponse.getErrorDescription(), serverResponse.getException());
				} else {
					DownloadHandler.writeFiles(serverResponse.getServerObjects(), projects.get(project), monitor);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	protected List<String> getSelectedOids() {
		List<String> rv = new ArrayList<>(); 
		ITreeSelection ts = treeResults.getStructuredSelection();
		if (ts == null || ts.getPaths() == null) {
			return rv;
		}
		for (TreePath path : ts.getPaths()) {
			System.out.println("Processing path: " + path);
			Object o = path.getLastSegment();
			if (o instanceof ServerObject) {
				rv.add(((ServerObject) o).getOid());
			} else if (o instanceof Map.Entry) {
				Map.Entry<ObjectTypes,List<ServerObject>> entry = (Map.Entry<ObjectTypes,List<ServerObject>>) o;
				for (ServerObject object : entry.getValue()) {
					rv.add(object.getOid());
				}
			}
		}
		System.out.println("Result: " + rv);
		return rv;
	}

	protected List<ServerObject> getSelectedObjects() {
		List<ServerObject> rv = new ArrayList<>(); 
		ITreeSelection ts = treeResults.getStructuredSelection();
		if (ts == null || ts.getPaths() == null) {
			return rv;
		}
		for (TreePath path : ts.getPaths()) {
			System.out.println("Processing path: " + path);
			Object o = path.getLastSegment();
			if (o instanceof ServerObject) {
				rv.add((ServerObject) o);
			} else if (o instanceof Map.Entry) {
				Map.Entry<ObjectTypes,List<ServerObject>> entry = (Map.Entry<ObjectTypes,List<ServerObject>>) o;
				rv.addAll(entry.getValue());
			}
		}
		return rv;
	}
	
	protected void actionButtonsRelatedSelectionChanged() {
		List<String> oids = getSelectedOids();
		boolean haveProject = comboUseProject.getSelectionIndex() >= 0;
		int whatToGenerate = comboWhatToGenerate.getSelectionIndex();
		
		btnShow.setEnabled(!oids.isEmpty() && haveProject);
		btnDownload.setEnabled(!oids.isEmpty() && haveProject);
		btnGenerate.setEnabled(!oids.isEmpty() && haveProject && whatToGenerate >= 0);
		btnExecute.setEnabled(!oids.isEmpty() && haveProject && whatToGenerate >= 0 && generators.get(whatToGenerate).isExecutable());
	}
	
	public void computeTxtBatchSizeIsEnabled() {
		txtBatchSize.setEnabled(comboExecution.getSelectionIndex() == 2 && isExecutable());
	}
	
	protected void computeOptionsEnablement() {
		btnSymbolicReferences.setEnabled(getGenerator().supportsSymbolicReferences());
		btnRunTimeResolution.setEnabled(getGenerator().supportsSymbolicReferencesAtRuntime());
		boolean isExecutable = isExecutable();
		comboExecution.setEnabled(isExecutable);
		computeTxtBatchSizeIsEnabled();
		btnWrapActions.setEnabled(getGenerator().supportsWrapIntoTask());
		btnCreateSuspended.setEnabled(getGenerator().supportsWrapIntoTask() && btnWrapActions.getSelection() || getGenerator().supportsCreateSuspended());
		btnCreateRaw.setEnabled(getGenerator().supportsRawOption());
		btnCreateDryRun.setEnabled(getGenerator().supportsDryRunOption());
	}
	
	public Generator getGenerator() {
		int whatToGenerate = comboWhatToGenerate.getSelectionIndex();
		return whatToGenerate >= 0 ? generators.get(whatToGenerate) : Generator.NULL_GENERATOR;
	}

	public boolean isExecutable() {
		int whatToGenerate = comboWhatToGenerate.getSelectionIndex();
		boolean isExecutable = whatToGenerate >= 0 && generators.get(whatToGenerate).isExecutable();
		return isExecutable;
	}
	
	public void computeSearchBoxItemsEnablement() {
		btnConvertToXml.setEnabled(!btnQuery.getSelection());
		txtLimit.setEnabled(!btnQuery.getSelection());
		txtOffset.setEnabled(!btnQuery.getSelection());
	}

//	class ActionButtonsRelatedSelectionChangedListener implements ISelectionChangedListener {
//	}
	class ActionButtonsRelatedModifyListener implements ModifyListener {
		@Override
		public void modifyText(ModifyEvent e) {
			actionButtonsRelatedSelectionChanged();
			computeOptionsEnablement();
		}
	}
	
	public static List<IFile> writeFiles(List<ServerObject> allObjects, IProject project, String type, IProgressMonitor monitor) {
		List<IFile> rv = new ArrayList<>();
		monitor.beginTask("Writing files", allObjects.size());
		int count = 0;
		try {
			for (ServerObject object : allObjects) {
				monitor.subTask("Writing " + object.getName());
				if (monitor.isCanceled()) {
					break;
				}
				IFile file = prepareOutputFileForCreation(project, type);
				if (file.exists()) {
					file.delete(true, monitor);
				} else {
					ResourceUtils.createParentFolders(file.getParent());
				}
				file.create(new ByteArrayInputStream(object.getXml().getBytes("utf-8")), true, monitor);
				Console.logMinor("File " + file.getFullPath() + " was successfully created.");
				rv.add(file);
				count++;
				monitor.worked(1);
			}
		} catch (Throwable t) {
			Util.processUnexpectedException(t);
		}
		Console.log("Created " + count + " object(s)");
		return rv;
	}
	
	public static IFile writeFile(String contents, IProject project, String type, IProgressMonitor monitor) {
		try {
			IFile file = prepareOutputFileForCreation(project, type);
			if (file.exists()) {
				file.delete(true, monitor);
			} else {
				ResourceUtils.createParentFolders(file.getParent());
			}
			file.create(new ByteArrayInputStream(contents.getBytes("utf-8")), true, monitor);
			Console.logMinor("File " + file.getFullPath() + " was successfully created.");
			return file;
		} catch (Throwable t) {
			Util.processUnexpectedException(t);
			return null;
		}
	}
	
	private static IFile prepareOutputFileForCreation(IContainer selected, String type) {
		IPath path = computeFilePath(selected, type);
		System.out.println("Path = " + path);
		if (path == null) {
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}


	private static IPath computeFilePath(IContainer root, String type) {
		String pattern = PluginPreferences.getGeneratedFileNamePattern();
		if (StringUtils.isBlank(pattern)) {
			return null;
		}
		
		String patternResolved = pattern
				.replace("$n", DownloadHandler.fixComponent(ServerResponseItem.formatActionCounter(PluginPreferences.getAndIncrementGenCounter())))
				.replace("$t", DownloadHandler.fixComponent(type))
				.replace("$s", DownloadHandler.fixComponent(PluginPreferences.getSelectedServerShortName()));
		
		System.out.println("pattern = " + pattern + ", resolvedPattern = " + patternResolved);
		 
		Path patternResolvedPath = new Path(patternResolved);
		IPath rv;
		if (patternResolvedPath.isAbsolute()) {
			rv = patternResolvedPath;
		} else {
			rv = root.getFullPath().append(patternResolvedPath);
		}
		System.out.println("Final result = " + rv);
		return rv;
	}

}


