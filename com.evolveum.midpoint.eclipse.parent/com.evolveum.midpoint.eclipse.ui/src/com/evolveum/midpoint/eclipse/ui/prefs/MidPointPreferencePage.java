package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class MidPointPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public static final String SERVERS = "servers";

	public static final String VALUE_NEVER = "never";
	public static final String VALUE_WHEN_ERRORS = "whenErrors";
	public static final String VALUE_WHEN_DIFFERENCES_OR_ERRORS = "whenDifferencesOrErrors";
	public static final String VALUE_ALWAYS = "always";

	public static final String VALUE_ONLY_IF_COMPLEX = "onlyIfComplex";

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

	public static final String[][] ROOT_DIRECTORY_OPTIONS_FOR_SELECTION = new String[][] { 
		{ "Current project", VALUE_CURRENT_PROJECT }, 
		{ "Current directory", VALUE_CURRENT_DIRECTORY },
	};

	public MidPointPreferencePage() {
		super(GRID);
	}

	protected void createFieldEditors() {

		addField(new ServersFieldEditor(SERVERS, "Servers", 
				new String[] { "Name", "URL", "Login", "Short name", "Properties" }, 
				new int[] { 100, 200, 100, 50, 200 }, getFieldEditorParent()));

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}