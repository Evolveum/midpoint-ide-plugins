package com.evolveum.midpoint.eclipse.ui.handlers.server;

import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.runtime.api.ServerAction;

public class ServerRequestItem {
	
	private ServerAction serverAction;
	private ServerRequestSource source;
	private String predefinedActionNumber;
	
	public ServerRequestItem(ServerAction action, ServerRequestSource source, String actionNumber) {
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
	public byte[] getContent() {
		return source.resolve();
	}
	public IPath getSourcePath() {
		return source.getPath();
	}
	public String getPredefinedActionNumber() {
		return predefinedActionNumber;
	}
	public void setPredefinedActionNumber(String predefinedActionNumber) {
		this.predefinedActionNumber = predefinedActionNumber;
	}

	@Override
	public String toString() {
		return "ServerRequestItem [action=" + serverAction + ", source=" + source + ", predefinedActionNumber="
				+ predefinedActionNumber + "]";
	}
	
	
}