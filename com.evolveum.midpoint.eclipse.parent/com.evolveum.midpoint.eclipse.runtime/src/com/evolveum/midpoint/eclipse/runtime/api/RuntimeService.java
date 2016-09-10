package com.evolveum.midpoint.eclipse.runtime.api;

import java.util.Collection;
import java.util.List;

import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.SearchObjectsServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.TestConnectionResponse;

public interface RuntimeService {
	
	TestConnectionResponse testConnection(ConnectionParameters parameters);

	ServerResponse executeServerRequest(ServerRequest request, ConnectionParameters connectionParameters);

	SearchObjectsServerResponse downloadObjects(ObjectTypes type, int limit, ConnectionParameters connectionParameters);

	SearchObjectsServerResponse downloadObjects(List<String> oids, ConnectionParameters connectionParameters);

	ServerResponse getCurrentVersionOfObject(String data, ConnectionParameters connectionParameters);
	
	SearchObjectsServerResponse downloadObject(String oid, ConnectionParameters connectionParameters);
	
	// if interpretation==XML_QUERY, types can be at most one, and limit is ignored (TODO cleaner interface)
	SearchObjectsServerResponse listObjects(Collection<ObjectTypes> types, String query, QueryInterpretation interpretation, int limit, ConnectionParameters connectionParameters);
}
