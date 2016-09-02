package com.evolveum.midpoint.eclipse.runtime.api;

import java.io.IOException;

public interface RuntimeService {
	
	void testConnection(ConnectionParameters parameters) throws IOException;

	ServerResponse executeServerRequest(ServerRequest request, ConnectionParameters connectionParameters);

}
