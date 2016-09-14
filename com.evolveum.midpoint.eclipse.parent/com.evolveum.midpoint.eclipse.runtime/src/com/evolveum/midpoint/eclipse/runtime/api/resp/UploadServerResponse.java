package com.evolveum.midpoint.eclipse.runtime.api.resp;

public class UploadServerResponse extends ServerResponse {
	
	private String errorDescription = null;

	public UploadServerResponse() {
	}
	
	public UploadServerResponse(Throwable t) {
		super(t);
	}

	@Override
	public String getErrorDescription() {
		return errorDescription != null ? errorDescription : super.getErrorDescription();
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

}
