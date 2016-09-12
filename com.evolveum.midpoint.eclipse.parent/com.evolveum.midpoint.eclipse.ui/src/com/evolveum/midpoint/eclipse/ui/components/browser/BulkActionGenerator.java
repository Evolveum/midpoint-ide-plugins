package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.util.DOMUtil;

public class BulkActionGenerator extends Generator {
	
	private String actionName;
	
	public BulkActionGenerator(String actionName) {
		this.actionName = actionName;
	}

	@Override
	public String getLabel() {
		return "Bulk action: " + actionName;
	}

	@Override
	public String generate(List<ServerObject> objects, GeneratorOptions options) {
		if (objects.isEmpty()) {
			return null;
		}
		Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, options.isWrapActions() ? "objects" : "actions", "c"));
		Element root = doc.getDocumentElement();
		
		List<Batch> batches = createBatches(objects, options);
		for (Batch batch : batches) {
			Element batchRoot;
			Element task = null;
			if (options.isWrapActions()) {
				task = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "task", "c"));
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "name", "c")).setTextContent("Execute " + actionName + "on objects " + (batch.getFirst()+1) + " to " + (batch.getLast()+1));
				Element extension = DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "extension", "c"));
				Element executeScript = DOMUtil.createSubElement(extension, new QName(Constants.SCEXT_NS, "executeScript", "scext"));
				batchRoot = executeScript;
			} else {
				batchRoot = root;
			}
			
			Element pipe = DOMUtil.createSubElement(batchRoot, new QName(Constants.SCRIPT_NS, "pipeline", "s"));
			Element search = DOMUtil.createSubElement(pipe, new QName(Constants.SCRIPT_NS, "expression", "s"));
			DOMUtil.setXsiType(search, new QName(Constants.SCRIPT_NS, "SearchExpressionType", "s"));
			DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent("ObjectType");
			Element filter = DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "searchFilter", "s"));
			Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);	
			for (ServerObject o : batch.getObjects()) {
				DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent(o.getOid());
				DOMUtil.createComment(inOid, " " + o.getName() + " ");
			}
			
			Element action = DOMUtil.createSubElement(pipe, new QName(Constants.SCRIPT_NS, "expression", "s"));
			DOMUtil.setXsiType(action, new QName(Constants.SCRIPT_NS, "ActionExpressionType", "s"));
			DOMUtil.createSubElement(action, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(actionName);
			
			if (task != null) {
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "ownerRef", "c")).setAttribute("oid", "00000000-0000-0000-0000-000000000002");
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "executionStatus", "c")).setTextContent(
						options.isCreateSuspended() ? "suspended" : "runnable"
						);
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "category", "c")).setTextContent("BulkAction");
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "handlerUri", "c")).setTextContent("http://midpoint.evolveum.com/xml/ns/public/model/scripting/handler-3");
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "recurrence", "c")).setTextContent("single");
			}
		}
			
		return DOMUtil.serializeDOMToString(doc);
	}

	@Override
	public boolean supportsRawOption() {
		return "assign".equals(actionName) || "delete".equals(actionName);
	}

	@Override
	public boolean supportsDryRunOption() {
		return false;		// only in 3.5
	}

	@Override
	public boolean isExecutable() {
		return true;
	}

	@Override
	public boolean supportsWrapIntoTask() {
		return true;
	}
	
	

}
