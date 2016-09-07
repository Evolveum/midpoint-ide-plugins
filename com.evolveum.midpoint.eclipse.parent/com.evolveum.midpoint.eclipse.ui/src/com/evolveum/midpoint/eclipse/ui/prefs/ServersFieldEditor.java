package com.evolveum.midpoint.eclipse.ui.prefs;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

public class ServersFieldEditor extends AbstractServersFieldEditor<ServerInfo> {

	protected ServersFieldEditor(String name, String labelText, String[] columnNames, int[] columnWidths,
			Composite parent) {
		super(name, labelText, columnNames, columnWidths, parent);
	}

	@Override
	protected String createStringRepresentation(List<ServerInfo> items) {
		return ServerInfo.toXml(items);
	}

	@Override
	protected List<ServerInfo> parseStringRepresentation(String string) {
		return ServerInfo.fromXml(string);
	}

	@Override
	protected ServerInfo createNewItem() {
		ServerEditDialog dialog = ServerEditDialog.createNew(getShell(), ServerInfo.createDefault());
		dialog.create();
		if (dialog.open() == Window.OK) {
			return dialog.getServerDataItem();
		} else {
			return null;
		}
	}
	
	@Override
	protected void editPressed() {
		int index = table.getSelectionIndex();
		if (index >= 0) {
			ServerEditDialog dialog = ServerEditDialog.createEdit(getShell(), currentItems.get(index));
			dialog.create();
			if (dialog.open() == Window.OK) {
				ServerInfo newItem = dialog.getServerDataItem();
				currentItems.set(index, newItem);
				table.getItem(index).setText(newItem.getColumnValues());
			}
		} 
	}
	
	@Override
	protected void testPressed() {
		int index = table.getSelectionIndex();
		if (index >= 0) {
			ServerInfo item = currentItems.get(index);
			PluginPreferences.testConnection(item.getName(), item.getUrl(), item.getLogin(), item.getPassword());
		}
	}

}
