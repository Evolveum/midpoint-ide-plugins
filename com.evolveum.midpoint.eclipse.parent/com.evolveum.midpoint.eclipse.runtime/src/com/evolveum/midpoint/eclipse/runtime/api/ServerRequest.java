package com.evolveum.midpoint.eclipse.runtime.api;

public class ServerRequest {

	private ServerAction action;
	private byte[] data;
	
	public ServerRequest(ServerAction action, byte[] data) {
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
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	
	
}
