package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.util.DOMUtil;

public class AssignmentGenerator extends Generator {
	
	@Override
	public String getLabel() {
		return "Assignment";
	}

	@Override
	public boolean isExecutable() {
		return false;
	}

	@Override
	public String generate(List<ServerObject> objects, GeneratorOptions options) {
		if (objects.isEmpty()) {
			return null;
		}
		Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "assignments", "c"));
		Element root = doc.getDocumentElement();
		for (ServerObject object : objects) {
			if (isApplicableFor(object.getType())) {
				Element aRoot = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "assignment", "c"));
				if (object.getType() == ObjectTypes.RESOURCE) {
					Element construction = DOMUtil.createSubElement(aRoot, new QName(Constants.COMMON_NS, "construction", "c"));
					Element resourceRef = DOMUtil.createSubElement(construction, new QName(Constants.COMMON_NS, "resourceRef", "c"));
					createRefContent(resourceRef, object, options);
				} else {
					Element targetRef = DOMUtil.createSubElement(aRoot, new QName(Constants.COMMON_NS, "targetRef", "c"));
					createRefContent(targetRef, object, options);				
				}
			} else {
				DOMUtil.createComment(root, " " + getLabel() + " cannot be created for object " + object.getName() + " of type " + object.getType().getTypeName() + " ");
			}
		}
		return DOMUtil.serializeDOMToString(doc);
	}

	private boolean isApplicableFor(ObjectTypes type) {
		return ObjectTypes.ABSTRACT_ROLE.isAssignableFrom(type) || type == ObjectTypes.RESOURCE;
	}
	
	public boolean supportsSymbolicReferences() {
		return true;
	}
	
	public boolean supportsSymbolicReferencesAtRuntime() {
		return true;
	}
}
