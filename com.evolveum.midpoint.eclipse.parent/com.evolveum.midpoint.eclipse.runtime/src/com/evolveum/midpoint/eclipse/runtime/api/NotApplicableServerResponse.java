package com.evolveum.midpoint.eclipse.runtime.api;

public class NotApplicableServerResponse extends ServerResponse {
	
	private String message;

	public NotApplicableServerResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
	
}
