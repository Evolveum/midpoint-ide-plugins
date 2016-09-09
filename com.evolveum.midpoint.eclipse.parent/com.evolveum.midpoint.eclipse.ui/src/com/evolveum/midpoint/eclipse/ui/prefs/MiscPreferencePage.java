package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class MiscPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String GENERATED_FILE_NAME_PATTERN = "generatedFileNamePattern";
	
	public MiscPreferencePage() {
		super(GRID);
	}

	protected void createFieldEditors() {
		
		addField(new StringFieldEditor(GENERATED_FILE_NAME_PATTERN, "Generated file name pattern", getFieldEditorParent()));
		Label patternInfo = new Label(getFieldEditorParent(), SWT.LEFT);
		patternInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		patternInfo.setText("Use $n for file number, $t for file type, $s for server.");
		patternInfo.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}