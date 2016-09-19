package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class UploadPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String ACTION_AFTER_UPLOAD = "actionAfterUpload";
	public static final String VALIDATE_AFTER_UPLOAD = "validateAfterUpload";
	public static final String SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX = "showUploadExecuteResultMessageBox";
	
	public UploadPreferencePage() {
		super(GRID);
	}

	protected void createFieldEditors() {
		
		final String[][] AUTO_ACTION_OPTIONS = new String[][] { { "Action 1", "1" }, { "Action 2", "2" }, { "Action 3", "3" }, { "No action", "" } };
		
		addField(new RadioGroupFieldEditor(ACTION_AFTER_UPLOAD, "Action to execute after any upload/execute batch successfully finishes", 4, AUTO_ACTION_OPTIONS, getFieldEditorParent()));

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		addField(new ComboFieldEditor(SHOW_UPLOAD_EXECUTE_RESULT_MESSAGE_BOX, "Show message box after upload/execute", MidPointPreferencePage.RESULT_BOX_OPTIONS, getFieldEditorParent()));

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		//addField(new BooleanFieldEditor(VALIDATE_AFTER_UPLOAD, "Validate resource objects after upload", getFieldEditorParent()));

		
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}