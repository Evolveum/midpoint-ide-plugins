package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.JFaceResources;
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
		
		Label action = new Label(getFieldEditorParent(), SWT.LEFT);
		action.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		action.setText("Action to execute after any upload/execute batch successfully finishes:");
		
		Label action2 = new Label(getFieldEditorParent(), SWT.LEFT);
		action2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		action2.setText("Please set in 'Actions' preference page.");
		action2.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));

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