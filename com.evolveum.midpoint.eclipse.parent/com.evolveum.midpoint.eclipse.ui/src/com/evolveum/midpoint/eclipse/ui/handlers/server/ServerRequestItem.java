package com.evolveum.midpoint.eclipse.ui.handlers.server;

import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.runtime.api.CompareServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.ServerAction;
import com.evolveum.midpoint.eclipse.runtime.api.ServerRequest;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Expander;

public class ServerRequestItem {
	
	private ServerAction serverAction;
	private ServerRequestSource source;
	private int predefinedActionNumber;
	
	public ServerRequestItem(ServerAction action, ServerRequestSource source, int actionNumber) {
		this.serverAction = action;
		this.source = source;
		this.predefinedActionNumber = actionNumber;
	}
	
	public ServerRequestItem(ServerAction action, ServerRequestSource source) {
		this.serverAction = action;
		this.source = source;
	}


	public ServerAction getServerAction() {
		return serverAction;
	}
	public void setServerAction(ServerAction action) {
		this.serverAction = action;
	}
	public String getDisplayName() {
		return source.getDisplayName();
	}
	public String getExpandedContent() {
		String content = source.resolve();
		return Expander.expand(content, PluginPreferences.getSelectedServer());
	}
	public IPath getSourcePath() {
		return source.getPath();
	}
	public int getPredefinedActionNumber() {
		return predefinedActionNumber;
	}
	public void setPredefinedActionNumber(int predefinedActionNumber) {
		this.predefinedActionNumber = predefinedActionNumber;
	}

	@Override
	public String toString() {
		return "ServerRequestItem [action=" + serverAction + ", source=" + source + ", predefinedActionNumber="
				+ predefinedActionNumber + "]";
	}

	public ServerRequest createServerRequest() {
		String expandedContent = getExpandedContent();
		if (expandedContent == null) {
			return null;
		}
		if (serverAction == ServerAction.COMPARE) {
			CompareServerRequest csr = new CompareServerRequest(serverAction, expandedContent);
			csr.setShowLocalToRemote(PluginPreferences.getCompareShowLocalToRemote());
			csr.setShowRemoteToLocal(PluginPreferences.getCompareShowRemoteToLocal());
			csr.setShowLocal(PluginPreferences.getCompareShowLocalNormalized());
			csr.setShowRemote(PluginPreferences.getCompareShowRemote());
			csr.setIgnoreItems(PluginPreferences.getCompareIgnoreItems());
			return csr;
		} else {
			return new ServerRequest(serverAction, expandedContent);
		}	
	}
	
	
}