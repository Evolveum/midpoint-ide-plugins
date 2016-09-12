package com.evolveum.midpoint.eclipse.ui.handlers;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;

public class SelectServerHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		List<ServerInfo> servers = PluginPreferences.getServers();
		String serverNumberString = event.getParameter(PluginConstants.PARAM_SERVER_NUMBER);
		int newSelected;
		if (StringUtils.isBlank(serverNumberString)) {
			newSelected = -1;
		} else {
			newSelected = Integer.valueOf(serverNumberString);
		}
		System.out.println("Selecting server #" + newSelected);
		for (int i = 0; i < servers.size(); i++) {
			servers.get(i).setSelected(i == newSelected);
		}
		PluginPreferences.setServers(servers);
		return null;
	}

}
