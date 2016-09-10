package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.util.DOMUtil;

public class RefGenerator extends Generator {
	
	private String refName;
	private ObjectTypes applicableFor;
	
	public RefGenerator(String refName, ObjectTypes applicableFor) {
		this.refName = refName;
		this.applicableFor = applicableFor;
	}

	@Override
	public String getLabel() {
		return "Reference (" + refName + ")";
	}

	@Override
	public boolean isExecutable() {
		return false;
	}

	@Override
	public String generate(List<ServerObject> objects) {
		if (objects.isEmpty()) {
			return null;
		}
		Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "references", "c"));
		Element root = doc.getDocumentElement();
		for (ServerObject object : objects) {
			if (isApplicableFor(object.getType())) {
				Element refRoot = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, refName, "c"));
				refRoot.setAttribute("oid", object.getOid());
				DOMUtil.setQNameAttribute(refRoot, "type", object.getType().getTypeQName());
				DOMUtil.createComment(refRoot, " " + object.getName() + " ");
			} else {
				DOMUtil.createComment(root, " " + getLabel() + " is not applicable for object " + object.getName() + " of type " + object.getType().getTypeName() + " ");
			}
		}
		return DOMUtil.serializeDOMToString(doc);
	}

	private boolean isApplicableFor(ObjectTypes type) {
		return applicableFor.isAssignableFrom(type);
	}

}
