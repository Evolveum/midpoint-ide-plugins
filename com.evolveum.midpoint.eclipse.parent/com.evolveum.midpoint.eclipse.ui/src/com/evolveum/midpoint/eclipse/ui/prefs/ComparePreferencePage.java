package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class ComparePreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String COMPARE_RESULT_FILE_NAME_PATTERN = "compareResultFileNamePattern";
	public static final String COMPARE_RESULT_ROOT_DIRECTORY = "compareResultRootDirectory";
	public static final String COMPARE_SHOW_LOCAL_TO_REMOTE = "compareShowLocalToRemote";
	public static final String COMPARE_SHOW_REMOTE_TO_LOCAL = "compareShowRemoteToLocal";
	public static final String COMPARE_SHOW_LOCAL_NORMALIZED = "compareShowLocalNormalized";
	public static final String COMPARE_SHOW_REMOTE = "compareShowRemote";
	public static final String COMPARE_IGNORE_OPERATIONAL_DATA = "compareIgnoreOperationalData";
	public static final String COMPARE_OTHER_ITEMS_TO_IGNORE = "compareOtherItemsToIgnore";
	public static final String SHOW_COMPARISON_RESULT_MESSAGE_BOX = "showComparisonResultMessageBox";

	public ComparePreferencePage() {
		super(GRID);
	}

	protected void createFieldEditors() {
		
		addField(new StringFieldEditor(COMPARE_RESULT_FILE_NAME_PATTERN, "File name pattern for diff results", getFieldEditorParent()));
		Label patternInfo = new Label(getFieldEditorParent(), SWT.LEFT);
		patternInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		patternInfo.setText("Use $f for file name ($F = with relative path from root), $n for sequence number, $t for output type, $s for server.");
		patternInfo.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		addField(new ComboFieldEditor(COMPARE_RESULT_ROOT_DIRECTORY, "Directory considered root", MidPointPreferencePage.ROOT_DIRECTORY_OPTIONS, getFieldEditorParent()));

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		addField(new BooleanFieldEditor(COMPARE_SHOW_LOCAL_TO_REMOTE, "Provide 'local-to-remote' delta", getFieldEditorParent()));
		addField(new BooleanFieldEditor(COMPARE_SHOW_REMOTE_TO_LOCAL, "Provide 'remote-to-local' delta", getFieldEditorParent()));
		addField(new BooleanFieldEditor(COMPARE_SHOW_LOCAL_NORMALIZED, "Provide normalize form of local file", getFieldEditorParent()));
		addField(new BooleanFieldEditor(COMPARE_SHOW_REMOTE, "Provide current state of remote file", getFieldEditorParent()));
		
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
		addField(new BooleanFieldEditor(COMPARE_IGNORE_OPERATIONAL_DATA, "Ignore operational data", getFieldEditorParent()));
		addField(new StringFieldEditor(COMPARE_OTHER_ITEMS_TO_IGNORE, "Other items to ignore", getFieldEditorParent()));
		Label patternInfo2 = new Label(getFieldEditorParent(), SWT.LEFT);
		patternInfo2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		patternInfo2.setText("Separate item paths by commas.");
		patternInfo2.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
		.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		addField(new ComboFieldEditor(ComparePreferencePage.SHOW_COMPARISON_RESULT_MESSAGE_BOX, "Show message box after comparing", MidPointPreferencePage.COMPARISON_RESULT_BOX_OPTIONS, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}