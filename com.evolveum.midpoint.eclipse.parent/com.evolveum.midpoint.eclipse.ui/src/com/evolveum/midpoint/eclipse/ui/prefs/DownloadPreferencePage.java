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

public class DownloadPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String DOWNLOADED_FILE_NAME_PATTERN = "downloadedFileNamePattern";
	public static final String DOWNLOADED_OBJECTS_LIMIT = "downloadedObjectsLimit";
	public static final String INCLUDE_IN_DOWNLOAD = "includeInDownload";
	public static final String EXCLUDE_FROM_DOWNLOAD = "excludeFromDownload";
	public static final String OVERWRITE_WHEN_DOWNLOADING = "overwriteWhenDownloading";

	public static final String VALUE_ALWAYS = "always";
	public static final String VALUE_ASK = "ask";
	public static final String VALUE_NEVER = "never";
	
	public DownloadPreferencePage() {
		super(GRID);
	}

	protected void createFieldEditors() {
		
		final String[][] OVERWRITE_OPTIONS = new String[][] { 
			{ "Always", VALUE_ALWAYS }, 
			{ "Ask before overwriting", VALUE_ASK },
			{ "Never", VALUE_NEVER }
		};
		
		addField(new StringFieldEditor(DOWNLOADED_FILE_NAME_PATTERN, "Downloaded file name pattern", getFieldEditorParent()));
		Label patternInfo = new Label(getFieldEditorParent(), SWT.LEFT);
		patternInfo.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		patternInfo.setText("Use $t for object type singular ('user'), $T plural ('users'), $n for object name, $o for OID, $s for server.");
		patternInfo.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
		
		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		addField(new StringFieldEditor(INCLUDE_IN_DOWNLOAD, "Types to download", getFieldEditorParent()));
		addField(new StringFieldEditor(EXCLUDE_FROM_DOWNLOAD, "Types NOT to download", getFieldEditorParent()));
		
		Label patternInfo2 = new Label(getFieldEditorParent(), SWT.LEFT);
		patternInfo2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		patternInfo2.setText("Use plural names, e.g. users, roles. Separate by commas.");
		patternInfo2.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));

		addField(new IntegerFieldEditor(DOWNLOADED_OBJECTS_LIMIT, "Max number of objects of one type", getFieldEditorParent()));

		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

		addField(new ComboFieldEditor(OVERWRITE_WHEN_DOWNLOADING, "Overwrite existing files", OVERWRITE_OPTIONS, getFieldEditorParent()));
		
//		new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL)
//			.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
		
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}