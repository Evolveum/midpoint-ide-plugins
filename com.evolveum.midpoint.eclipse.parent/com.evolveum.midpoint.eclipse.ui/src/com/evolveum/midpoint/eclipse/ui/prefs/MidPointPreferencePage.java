package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.handlers.TestConnectionHandler;
import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class MidPointPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String MIDPOINT_URL = "midPointUrl";
	public static final String MIDPOINT_LOGIN = "midPointLogin";
	public static final String MIDPOINT_PASSWORD = "midPointPassword";
	public static final String MIDPOINT_LOGFILE = "midPointLogFile";
	public static final String SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX = "showUploadExecuteResultMessageBox";
	public static final String SHOW_COMPARISON_RESULT_MESSAGE_BOX = "showComparisonResultMessageBox";

	public static final String VALUE_NEVER = "never";
	public static final String VALUE_WHEN_ERRORS = "whenErrors";
	public static final String VALUE_WHEN_DIFFERENCES_OR_ERRORS = "whenDifferencesOrErrors";
	public static final String VALUE_ALWAYS = "always";

	public final static String[][] RESULT_BOX_OPTIONS = new String[][] { 
		{ "Always", VALUE_ALWAYS }, 
		{ "Only when errors", VALUE_WHEN_ERRORS }, 
		{ "Never", VALUE_NEVER } 
	};

	public final static String[][] COMPARISON_RESULT_BOX_OPTIONS = new String[][] { 
		{ "Always", VALUE_ALWAYS }, 
		{ "Only when differences or errors", VALUE_WHEN_DIFFERENCES_OR_ERRORS },
		{ "Only when errors", VALUE_WHEN_ERRORS }, 
		{ "Never", VALUE_NEVER } 
	};

	public static final String VALUE_CURRENT_PROJECT = "currentProject";
	public static final String VALUE_CURRENT_PROJECT_MINUS_1 = "currentProjectMinus1";
	public static final String VALUE_CURRENT_PROJECT_MINUS_2 = "currentProjectMinus2";
	public static final String VALUE_CURRENT_PROJECT_MINUS_3 = "currentProjectMinus3";
	public static final String VALUE_CURRENT_DIRECTORY = "currentDirectory";
	public static final String VALUE_CURRENT_DIRECTORY_PLUS_1 = "currentDirectoryPlus1";
	public static final String VALUE_CURRENT_DIRECTORY_PLUS_2 = "currentDirectoryPlus2";
	public static final String VALUE_CURRENT_DIRECTORY_PLUS_3 = "currentDirectoryPlus3";
	
	public static final String[][] ROOT_DIRECTORY_OPTIONS = new String[][] { 
		{ "Current project", VALUE_CURRENT_PROJECT }, 
		{ "One level under current project", VALUE_CURRENT_PROJECT_MINUS_1 }, 
		{ "Two levels under current project", VALUE_CURRENT_PROJECT_MINUS_2 },
		{ "Three levels under current project", VALUE_CURRENT_PROJECT_MINUS_3 },
		{ "Current directory", VALUE_CURRENT_DIRECTORY },
		{ "One level above current directory", VALUE_CURRENT_DIRECTORY_PLUS_1 },
		{ "Two levels above current directory", VALUE_CURRENT_DIRECTORY_PLUS_2 },
		{ "Three levels above current directory", VALUE_CURRENT_DIRECTORY_PLUS_3 }
	};

	private StringFieldEditor urlField;
	private StringFieldEditor loginField;
	private StringButtonFieldEditor passwordField; 
	
	public MidPointPreferencePage() {
		super(GRID);
	}

	protected void createFieldEditors() {
		urlField = new StringFieldEditor(MIDPOINT_URL, "Server URL", getFieldEditorParent());
		loginField = new StringFieldEditor(MIDPOINT_LOGIN, "Login", getFieldEditorParent());
		passwordField = new StringButtonFieldEditor(MIDPOINT_PASSWORD, "Password", getFieldEditorParent()) {
			@Override
		    protected void doFillIntoGrid(Composite parent, int numColumns) {
		        super.doFillIntoGrid(parent, numColumns);
		        getTextControl().setEchoChar('*');
		    }
			@Override
			protected String changePressed() {
				testConnection();
				return null;
			}
		};
		passwordField.setChangeButtonText("Test connection");

		addField(urlField);
		addField(loginField);
		addField(passwordField);

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		addField(new FileFieldEditor(MIDPOINT_LOGFILE, "Server log file", getFieldEditorParent()));
		
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
		addField(new ComboFieldEditor(SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX, "Show message box after upload/execute", RESULT_BOX_OPTIONS, getFieldEditorParent()));
		addField(new ComboFieldEditor(SHOW_COMPARISON_RESULT_MESSAGE_BOX, "Show message box after comparing", COMPARISON_RESULT_BOX_OPTIONS, getFieldEditorParent()));

	}

	protected void testConnection() {
		ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(IHandlerService.class);
		
		try {
			Command command = commandService.getCommand(PluginConstants.COMMAND_TEST_CONNECTION);
			Parameterization[] params = new Parameterization[] { 
					new Parameterization(command.getParameter(TestConnectionHandler.PARAM_SERVER_URL), urlField.getStringValue()),
					new Parameterization(command.getParameter(TestConnectionHandler.PARAM_LOGIN), loginField.getStringValue()),
					new Parameterization(command.getParameter(TestConnectionHandler.PARAM_PASSWORD), passwordField.getStringValue()),				
					};
			ParameterizedCommand parametrizedCommand = new ParameterizedCommand(command, params);
			handlerService.executeCommand(parametrizedCommand, null);
		} catch (CommandException e) {
			e.printStackTrace();		// TODO log
		}
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}