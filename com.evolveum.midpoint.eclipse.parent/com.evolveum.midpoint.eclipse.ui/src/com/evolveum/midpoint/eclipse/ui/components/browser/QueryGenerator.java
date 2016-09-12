package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.util.DOMUtil;

public class QueryGenerator extends Generator {

	@Override
	public String getLabel() {
		return "Query returning these objects";
	}

	@Override
	public String generate(List<ServerObject> objects, GeneratorOptions options) {
		if (objects.isEmpty()) {
			return null;
		}
		Document doc = DOMUtil.getDocument(Constants.Q_QUERY);
		Element query = doc.getDocumentElement();
		Element filter = DOMUtil.createSubElement(query, Constants.Q_FILTER);
		Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID);	
		for (ServerObject o : objects) {
			DOMUtil.createSubElement(inOid, Constants.Q_VALUE).setTextContent(o.getOid());
		}
		return DOMUtil.serializeDOMToString(doc);	
	}

	@Override
	public boolean supportsSymbolicReferences() {
		return false;		// not yet
	}
}
