package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.util.DOMUtil;

public class ShowGenerator extends Generator {
	
	public ShowGenerator() {
	}

	@Override
	public String getLabel() {
		return null;		// not needed
	}

	@Override
	public String generate(List<ServerObject> objects, GeneratorOptions options) {
		if (objects.isEmpty()) {
			return null;
		}
		if (objects.size() == 1) {
			return objects.get(0).getXml();
		}
		
		try {
			Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "objects", "c"));
			Element root = doc.getDocumentElement();
			for (ServerObject object : objects) {
				Element obj = DOMUtil.parseDocument(object.getXml()).getDocumentElement();
				root.appendChild(doc.importNode(obj, true));
			}
			return DOMUtil.serializeDOMToString(root);
		} catch (Throwable t) {
			Console.logError("Couldn't copy selected objects to new XML document", t);
			return null;
		}
	}

}
