package com.evolveum.midpoint.eclipse.ui.components.browser;

import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;

public class ConnectorRefGenerator extends RefGenerator {

	public ConnectorRefGenerator() {
		super("connectorRef", ObjectTypes.CONNECTOR);
	}

	@Override
	protected String getSymbolicRefItemValue(ServerObject object) {
		return object.getSubtypes().size() == 1 ? object.getSubtypes().get(0) : "FILL IN CONNECTORTYPE HERE";
	}
	
	@Override
	protected String getSymbolicRefItemName(ServerObject object) {
		return "connectorType";
	}

}
