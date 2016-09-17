package com.evolveum.midpoint.eclipse.runtime.api.resp;

public class TestConnectionResponse {
	
	private boolean success;
	private String message;
	private Throwable exception;
	private String version, revision;
	
	public TestConnectionResponse(boolean success, String message, Throwable exception) {
		this.success = success;
		this.message = message;
		this.exception = exception;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public String getFailureDescription() {
		if (success) {
			return null;
		} else if (message != null) {
			return message;
		} else {
			return String.valueOf(exception);
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}
	
	

}
