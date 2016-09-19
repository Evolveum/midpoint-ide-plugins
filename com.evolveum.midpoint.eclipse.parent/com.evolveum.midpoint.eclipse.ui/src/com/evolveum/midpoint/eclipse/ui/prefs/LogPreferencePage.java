package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class LogPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String LOG_FILE_NAME_PATTERN = "logFileNamePattern";
	public static final String LOG_FILE_DEFAULT_PROJECT = "logFileDefaultProject";
	
	public static final String LOG_CONSOLE_REFRESH_INTERVAL = "logConsoleRefreshInterval";
	public static final String LOG_GO_BACK_N = "logGoBackN";
	
	public static final String USE_MIDPOINT_LOG_VIEWER = "useMidPointLogViewer";
	//public static final String DEFAULT_LOG_VIEWER_CONFIGURATION = "defaultLogViewerConfiguration";
	
	public LogPreferencePage() {
		super(GRID);
	}

	protected void createFieldEditors() {
		
		final String[][] USE_MIDPOINT_LOG_VIEWER_OPTIONS = new String[][] { 
			{ "Never", MidPointPreferencePage.VALUE_NEVER }, 
			{ "Only if complex", MidPointPreferencePage.VALUE_ONLY_IF_COMPLEX }, 
			{ "Always", MidPointPreferencePage.VALUE_ALWAYS } 
		};
		
		addField(new StringFieldEditor(LOG_FILE_NAME_PATTERN, "Extracted log file name pattern", getFieldEditorParent()));
		Label patternInfo = new Label(getFieldEditorParent(), SWT.LEFT);
		patternInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		patternInfo.setText("Use $n for file number, $s for server. If relative, file name pattern relates to current project.");
		patternInfo.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		
		addField(new StringFieldEditor(LOG_FILE_DEFAULT_PROJECT, "Default project if none is selected", getFieldEditorParent()));
		Label projectInfo = new Label(getFieldEditorParent(), SWT.LEFT);
		projectInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		projectInfo.setText("Use * for first available project.");
		projectInfo.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
		addField(new IntegerFieldEditor(LOG_CONSOLE_REFRESH_INTERVAL, "Log console refresh interval (seconds)", getFieldEditorParent()));
		addField(new IntegerFieldEditor(LOG_GO_BACK_N, "How many KBs of log file to fetch", getFieldEditorParent()));

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
		addField(new ComboFieldEditor(USE_MIDPOINT_LOG_VIEWER, "Use midPoint log viewer", USE_MIDPOINT_LOG_VIEWER_OPTIONS, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}