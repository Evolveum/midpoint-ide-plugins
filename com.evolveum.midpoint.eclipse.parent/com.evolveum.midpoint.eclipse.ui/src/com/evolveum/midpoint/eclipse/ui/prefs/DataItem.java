package com.evolveum.midpoint.eclipse.ui.prefs;

public interface DataItem {
	
	boolean isSelected();
	
	void setSelected(boolean value);

	String[] getColumnValues();

	DataItem clone();
	
}
