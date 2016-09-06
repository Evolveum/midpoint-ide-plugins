package com.evolveum.midpoint.eclipse.ui.prefs;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class ServersPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	public static final String SERVERS = "servers";
	
	public ServersPreferencePage() {
		super(GRID);
	}
	
	protected void createFieldEditors() {
		addField(new ServersFieldEditor(SERVERS, "Servers", 
				new String[] { "Name", "URL", "Login", "Properties" }, 
				new int[] { 100, 200, 100, 200 }, getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(EclipseActivator.getInstance().getPreferenceStore());
	}
	
}