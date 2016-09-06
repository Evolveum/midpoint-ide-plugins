package com.evolveum.midpoint.eclipse.ui.prefs;

import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;

public class ServersCache {

	private static ServersCache instance;

	public static ServersCache getInstance() {
		if (instance == null) {
			instance = new ServersCache();
			EclipseActivator.getInstance().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					System.out.println("PropertyChange: " + event.getProperty());
					if (MidPointPreferencePage.SERVERS.equals(event.getProperty())) {
						instance.clear();
					}
				}
			});
		}
		return instance;
	}
	
	protected void clear() {
		System.out.println("Clearing cache.");
		cachedServers = null;
	}

	private List<ServerDataItem> cachedServers = null;
	
	public List<ServerDataItem> getServers() {
		if (cachedServers == null) {
			cachedServers = ServerDataItem.fromXml(EclipseActivator.getInstance().getPreferenceStore().getString(MidPointPreferencePage.SERVERS));
		}
		return cachedServers;
	}

}
