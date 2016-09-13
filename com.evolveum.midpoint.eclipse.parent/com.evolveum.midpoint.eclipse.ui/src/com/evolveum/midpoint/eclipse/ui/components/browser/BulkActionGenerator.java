package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
import com.evolveum.midpoint.eclipse.ui.util.Console;
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

		Element top = null;
		
		List<Batch> batches = createBatches(objects, options);
		Element root;
		if (batches.size() > 1) {
			top = root = DOMUtil.getDocument(new QName(Constants.COMMON_NS, options.isWrapActions() ? "objects" : "actions", "c")).getDocumentElement();
		} else {
			root = null;
		}
		
		for (Batch batch : batches) {
			Element batchRoot;
			Element task = null;
			if (options.isWrapActions()) {
				if (root == null) {
					task = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "task", "c")).getDocumentElement();
				} else {
					task = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "task", "c"));
				}
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "name", "c")).setTextContent("Execute " + actionName + " on objects " + (batch.getFirst()+1) + " to " + (batch.getLast()+1));
				Element extension = DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "extension", "c"));
				Element executeScript = DOMUtil.createSubElement(extension, new QName(Constants.SCEXT_NS, "executeScript", "scext"));
				batchRoot = executeScript;
			} else {
				batchRoot = root;
			}
			
			Element pipe = root == null ? 
					DOMUtil.getDocument(new QName(Constants.SCRIPT_NS, "pipeline", "s")).getDocumentElement() :
					DOMUtil.createSubElement(batchRoot, new QName(Constants.SCRIPT_NS, "pipeline", "s"));
					
			if (top == null) {
				top = pipe;
			}
					
			createOidsQuery(pipe, batch);
			createAction(pipe, options);
			
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
			
		return DOMUtil.serializeDOMToString(top);
	}

	public void createOidsQuery(Element root, Batch batch) {
		Element search = DOMUtil.createSubElement(root, new QName(Constants.SCRIPT_NS, "expression", "s"));
		DOMUtil.setXsiType(search, new QName(Constants.SCRIPT_NS, "SearchExpressionType", "s"));
		DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent("ObjectType");
		Element filter = DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "searchFilter", "s"));
		Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);	
		for (ServerObject o : batch.getObjects()) {
			DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent(o.getOid());
			DOMUtil.createComment(inOid, " " + o.getName() + " ");
		}
	}

	public void createSingleSourceObjectQuery(Element root, SourceObject object) {
		Element search = DOMUtil.createSubElement(root, new QName(Constants.SCRIPT_NS, "expression", "s"));
		DOMUtil.setXsiType(search, new QName(Constants.SCRIPT_NS, "SearchExpressionType", "s"));
		DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(object.getType().getTypeName());
		Element filter = DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "searchFilter", "s"));
		if (object.getOid() != null) {
			Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);	
			DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent(object.getOid());
			DOMUtil.createComment(inOid, " " + object.getName() + " ");
		} else if (object.getName() != null) {
			Element equal = DOMUtil.createSubElement(filter, Constants.Q_EQUAL_Q);
			DOMUtil.createSubElement(equal, Constants.Q_PATH_Q).setTextContent("name");
			DOMUtil.createSubElement(equal, Constants.Q_VALUE_Q).setTextContent(object.getName());
		} else {
			Console.logWarning("No OID nor name provided; action on this object cannot be executed.");
			Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);	
			DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent("no such object 919432948jkas");
		}
	}

	public void createAction(Element root, GeneratorOptions options) {
		Element action = DOMUtil.createSubElement(root, new QName(Constants.SCRIPT_NS, "expression", "s"));
		DOMUtil.setXsiType(action, new QName(Constants.SCRIPT_NS, "ActionExpressionType", "s"));
		DOMUtil.createSubElement(action, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(actionName);
		if (options.isRaw()) {
			Element rawParam = DOMUtil.createSubElement(action, new QName(Constants.SCRIPT_NS, "parameter", "s"));
			DOMUtil.createSubElement(rawParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("raw");
			DOMUtil.createSubElement(rawParam, new QName(Constants.COMMON_NS, "value", "c")).setTextContent("true");
		}
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

	public String generateFromSourceObject(SourceObject object, GeneratorOptions options) {
		Element pipe = DOMUtil.getDocument(new QName(Constants.SCRIPT_NS, "pipeline", "s")).getDocumentElement();
		createSingleSourceObjectQuery(pipe, object);
		createAction(pipe, options);
		return DOMUtil.serializeDOMToString(pipe);
	}

}
