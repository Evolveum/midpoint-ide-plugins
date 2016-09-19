package com.evolveum.midpoint.eclipse.runtime.api.req;

public class ServerRequest {

	private ServerAction action;
	private String data;
	private boolean validate;			// brutal hack (currently not used, anyway)
	
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
	public boolean isValidate() {
		return validate;
	}
	public void setValidate(boolean validate) {
		this.validate = validate;
	}
	
	
	
}
