package com.evolveum.midpoint.eclipse.runtime.api;

import java.io.IOException;
import java.util.List;

import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.SearchObjectsServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.TestConnectionResponse;

public interface RuntimeService {
	
	TestConnectionResponse testConnection(ConnectionParameters parameters);

	ServerResponse executeServerRequest(ServerRequest request, ConnectionParameters connectionParameters);

	SearchObjectsServerResponse getObjects(ObjectTypes type, int limit, ConnectionParameters connectionParameters) throws IOException;

	ServerResponse getCurrentVersionOfObject(String data, ConnectionParameters connectionParameters);
	
	SearchObjectsServerResponse getObject(String oid, ConnectionParameters connectionParameters);
}
