package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.evolveum.midpoint.eclipse.ui.handlers.server.UploadOrExecuteHandler;
import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class ActionsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String ACTION_FILE_PREFIX = "actionFile";
	public static final String ACTION_FILE_1 = "actionFile1";
	public static final String ACTION_FILE_2 = "actionFile2";
	public static final String ACTION_FILE_3 = "actionFile3";

	public static final String ACTION_OPEN_AFTER_PREFIX = "actionOpenAfter";
	public static final String ACTION_OPEN_AFTER_1 = "actionOpenAfter1";
	public static final String ACTION_OPEN_AFTER_2 = "actionOpenAfter2";
	public static final String ACTION_OPEN_AFTER_3 = "actionOpenAfter3";
	public static final String ACTION_OPEN_AFTER_OTHER = "actionOpenAfterOther";
	
	public static final String ACTION_AFTER_UPLOAD = "actionAfterUpload";
	
	public static final String OUTPUT_FILE_NAME_PATTERN = "outputFileNamePattern";
	public static final String OUTPUT_FILE_NAME_RELATIVE = "outputFileNameRelative";
	public static final String OUTPUT_FILE_NAME_PATTERN_NO_SOURCE = "outputFileNameNoSource";

	public static final String USE_MIDPOINT_LOG_VIEWER = "useMidPointLogViewer";
	
	public ActionsPreferencePage() {
		super(GRID);
	}

	protected void createFieldEditors() {
		
		final String[][] OPEN_AFTER_OPTIONS = new String[][] { 
			{ "Server log", UploadOrExecuteHandler.OUTPUT_TYPE_LOG }, 
			{ "Action data output", UploadOrExecuteHandler.OUTPUT_TYPE_DATA }, 
			{ "Action console output", UploadOrExecuteHandler.OUTPUT_TYPE_CONSOLE }, 
			{ "Operation result", UploadOrExecuteHandler.OUTPUT_TYPE_RESULT }, 
			{ "Nothing", "" } 
		};
		final String[][] AUTO_ACTION_OPTIONS = new String[][] { { "Action 1", "1" }, { "Action 2", "2" }, { "Action 3", "3" }, { "No action", "" } };
		
		addField(new FileFieldEditor(ACTION_FILE_1, "File for action 1", getFieldEditorParent()));
		addField(new ComboFieldEditor(ACTION_OPEN_AFTER_1, "Open after executing action 1", OPEN_AFTER_OPTIONS, getFieldEditorParent()));
		addField(new FileFieldEditor(ACTION_FILE_2, "File for action 2", getFieldEditorParent()));
		addField(new ComboFieldEditor(ACTION_OPEN_AFTER_2, "Open after executing action 2", OPEN_AFTER_OPTIONS, getFieldEditorParent()));
		addField(new FileFieldEditor(ACTION_FILE_3, "File for action 3", getFieldEditorParent()));
		addField(new ComboFieldEditor(ACTION_OPEN_AFTER_3, "Open after executing action 3", OPEN_AFTER_OPTIONS, getFieldEditorParent()));
		addField(new ComboFieldEditor(ACTION_OPEN_AFTER_OTHER, "Open after executing any action", OPEN_AFTER_OPTIONS, getFieldEditorParent()));
		
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
		addField(new RadioGroupFieldEditor(ACTION_AFTER_UPLOAD, "Action to execute after any upload/execute batch successfully finishes", 4, AUTO_ACTION_OPTIONS, getFieldEditorParent()));

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		addField(new StringFieldEditor(OUTPUT_FILE_NAME_PATTERN, "Action output files pattern", getFieldEditorParent()));
		Label patternInfo = new Label(getFieldEditorParent(), SWT.LEFT);
		patternInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		patternInfo.setText("Use $f for file name, $n for execution sequence number, $t for output type. An example: 'runs/$f.$n.$t'.");
		patternInfo.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		Label patternInfo2 = new Label(getFieldEditorParent(), SWT.LEFT);
		patternInfo2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		patternInfo2.setText("Absolute file name patterns must be in 'Eclipse logical' form, i.e. '/projectname/folder1/folder2/.../filename'.");
		patternInfo2.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		
		addField(new BooleanFieldEditor(OUTPUT_FILE_NAME_RELATIVE, "Path is relative to the file being executed", getFieldEditorParent()));
		addField(new StringFieldEditor(OUTPUT_FILE_NAME_PATTERN_NO_SOURCE, "Output files with no source", getFieldEditorParent()));
		
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		addField(new BooleanFieldEditor(USE_MIDPOINT_LOG_VIEWER, "Use midPoint log viewer to view server log fragments", getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}