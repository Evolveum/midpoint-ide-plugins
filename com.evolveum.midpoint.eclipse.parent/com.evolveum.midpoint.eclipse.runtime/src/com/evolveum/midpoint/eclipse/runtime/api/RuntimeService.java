package com.evolveum.midpoint.eclipse.runtime.api;

import java.io.IOException;
import java.util.List;

public interface RuntimeService {
	
	void testConnection(ConnectionParameters parameters) throws IOException;

	ServerResponse executeServerRequest(ServerRequest request, ConnectionParameters connectionParameters);

	List<ServerObject> downloadObjects(ObjectTypes type, int limit, ConnectionParameters connectionParameters) throws IOException;

}
