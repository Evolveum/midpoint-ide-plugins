package com.evolveum.midpoint.eclipse.runtime.api.req;

public class ServerRequest {

	private ServerAction action;
	private String data;
	
	public ServerRequest(ServerAction action, String data) {
		super();
		this.action = action;
		this.data = data;
	}
	public ServerAction getAction() {
		return action;
	}
	public void setAction(ServerAction action) {
		this.action = action;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	
	
}
