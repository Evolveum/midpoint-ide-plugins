package com.evolveum.midpoint.eclipse.runtime.api;

public class ServerResponse {

	protected int statusCode;
	protected String reasonPhrase;
	protected String rawResponseBody;
	protected Throwable exception;
	
	public ServerResponse() {
	}
	
	public ServerResponse(Throwable t) {
		exception = t;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int responseCode) {
		this.statusCode = responseCode;
	}
	public String getReasonPhrase() {
		return reasonPhrase;
	}
	public void setReasonPhrase(String responseStatusLineText) {
		this.reasonPhrase = responseStatusLineText;
	}
	public String getRawResponseBody() {
		return rawResponseBody;
	}
	public void setRawResponseBody(String rawResponseBody) {
		this.rawResponseBody = rawResponseBody;
	}
	public Throwable getException() {
		return exception;
	}
	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public boolean isSuccess() {
		return exception == null && statusCode >= 200 && statusCode < 300;		// TODO operationResult
	}

	public String getErrorDescription() {
		if (exception != null) {
			return exception.toString();
		}
		return statusCode + ": " + reasonPhrase;
	}

}
