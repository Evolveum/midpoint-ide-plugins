package com.evolveum.midpoint.eclipse.runtime.api.resp;

import java.util.ArrayList;
import java.util.List;

public class SearchObjectsServerResponse extends ServerResponse {

	private final List<ServerObject> serverObjects = new ArrayList<>();
	
	public SearchObjectsServerResponse() {
	}
	
	public SearchObjectsServerResponse(Throwable t) {
		super(t);
	}

	public List<ServerObject> getServerObjects() {
		return serverObjects;
	}

}
