package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.util.DOMUtil;

public class BulkActionGenerator extends Generator {
	
	public enum Action {
		
		RECOMPUTE("recompute", "recompute", ObjectTypes.FOCUS, false, true, false),
		ENABLE("enable", "enable", ObjectTypes.FOCUS, false, true, false),
		DISABLE("disable", "disable", ObjectTypes.FOCUS, false, true, false),
		DELETE("delete", "delete", ObjectTypes.OBJECT, true, true, true),
		MODIFY("modify", "modify", ObjectTypes.OBJECT, true, true, false),
		LOG("log", "log", ObjectTypes.OBJECT, false, false, false),
		TEST_RESOURCE("test resource", "test-resource", ObjectTypes.RESOURCE, false, false, false),
		EXECUTE_SCRIPT("execute script", "execute-script", ObjectTypes.OBJECT, false, false, false);
		
		private final String displayName, actionName;
		private final ObjectTypes applicableTo;
		private final boolean supportsRaw, supportsDryRun, requiresConfirmation;
		
		private Action(String displayName, String actionName, ObjectTypes applicableTo, boolean supportsRaw, boolean supportsDryRun, boolean requiresConfirmation) {
			this.displayName = displayName;
			this.actionName = actionName;
			this.applicableTo = applicableTo;
			this.supportsRaw = supportsRaw;
			this.supportsDryRun = supportsDryRun;
			this.requiresConfirmation = requiresConfirmation;
		}
	}
	
	private Action action;
	
	public BulkActionGenerator(Action action) {
		this.action = action;
	}

	@Override
	public String getLabel() {
		return "Bulk action: " + action.displayName;
	}

	@Override
	public String generate(List<ServerObject> objects, GeneratorOptions options) {
		if (objects.isEmpty()) {
			return null;
		}

		Element top = null;
		
		List<Batch> batches = createBatches(objects, options, action.applicableTo);
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
					top = task;
				} else {
					task = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "task", "c"));
				}
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "name", "c")).setTextContent("Execute " + action.displayName + " on objects " + (batch.getFirst()+1) + " to " + (batch.getLast()+1));
				Element extension = DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "extension", "c"));
				Element executeScript = DOMUtil.createSubElement(extension, new QName(Constants.SCEXT_NS, "executeScript", "scext"));
				batchRoot = executeScript;
			} else {
				batchRoot = root;
			}
			
			Element pipe;
			if (batchRoot == null) {
				pipe = DOMUtil.getDocument(new QName(Constants.SCRIPT_NS, "pipeline", "s")).getDocumentElement();
				top = pipe;
			} else {
				pipe = DOMUtil.createSubElement(batchRoot, new QName(Constants.SCRIPT_NS, "pipeline", "s")); 
			}
					
			createOidsSearch(pipe, batch);
			createAction(pipe, options);
			
			if (task != null) {
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "taskIdentifier", "c")).setTextContent(generateTaskIdentifier());
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "ownerRef", "c")).setAttribute("oid", "00000000-0000-0000-0000-000000000002");
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "executionStatus", "c")).setTextContent(
						options.isCreateSuspended() ? "suspended" : "runnable"
						);
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "category", "c")).setTextContent("BulkActions");
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "handlerUri", "c")).setTextContent("http://midpoint.evolveum.com/xml/ns/public/model/scripting/handler-3");
				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "recurrence", "c")).setTextContent("single");
			}
		}
			
		return DOMUtil.serializeDOMToString(top);
	}

	public static String generateTaskIdentifier() {
		return System.currentTimeMillis() + ":" + Math.round(Math.random() * 1000000000.0);
	}

	public void createOidsSearch(Element root, Batch batch) {
		Element search = DOMUtil.createSubElement(root, new QName(Constants.SCRIPT_NS, "expression", "s"));
		DOMUtil.setXsiType(search, new QName(Constants.SCRIPT_NS, "SearchExpressionType", "s"));
		DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(action.applicableTo.getTypeName());
		Element filter = DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "searchFilter", "s"));
		Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);	
		for (ServerObject o : batch.getObjects()) {
			DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent(o.getOid());
			DOMUtil.createComment(inOid, " " + o.getName() + " ");
		}
	}

	public void createSingleSourceSearch(Element root, SourceObject object) {
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
		Element actionE = DOMUtil.createSubElement(root, new QName(Constants.SCRIPT_NS, "expression", "s"));
		DOMUtil.setXsiType(actionE, new QName(Constants.SCRIPT_NS, "ActionExpressionType", "s"));
		DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(action.actionName);
		if (options.isRaw() && supportsRawOption()) {
			Element rawParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
			DOMUtil.createSubElement(rawParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("raw");
			DOMUtil.createSubElement(rawParam, new QName(Constants.COMMON_NS, "value", "c")).setTextContent("true");
		}
		if (options.isDryRun() && supportsDryRunOption()) {
			Element rawParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
			DOMUtil.createSubElement(rawParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("dryRun");
			DOMUtil.createSubElement(rawParam, new QName(Constants.COMMON_NS, "value", "c")).setTextContent("true");
		}
		if (action == Action.MODIFY) {
			Element deltaParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
			DOMUtil.createSubElement(deltaParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("delta");
			Element objectDelta = DOMUtil.createSubElement(deltaParam, new QName(Constants.COMMON_NS, "value", "c"));
			DOMUtil.setXsiType(objectDelta, new QName(Constants.TYPES_NS, "ObjectDeltaType", "t"));
			Element itemDelta = DOMUtil.createSubElement(objectDelta, new QName(Constants.TYPES_NS, "itemDelta", "t"));
			
			DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "modificationType", "t")).setTextContent("add");
			DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "path", "t")).setTextContent("TODO (e.g. displayName)");
			Element value = DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "value", "t"));
			DOMUtil.setXsiType(value, DOMUtil.XSD_STRING);
			value.setTextContent("TODO");
		} else if (action == Action.EXECUTE_SCRIPT) {
			Element scriptParam = DOMUtil.createSubElement(actionE, new QName(Constants.SCRIPT_NS, "parameter", "s"));
			DOMUtil.createSubElement(scriptParam, new QName(Constants.SCRIPT_NS, "name", "s")).setTextContent("script");
			Element script = DOMUtil.createSubElement(scriptParam, new QName(Constants.COMMON_NS, "value", "c"));
			DOMUtil.setXsiType(script, new QName(Constants.COMMON_NS, "ScriptExpressionEvaluatorType", "c"));
			DOMUtil.createSubElement(script, new QName(Constants.COMMON_NS, "code", "c")).setTextContent("\n                    log.info('{}', input.asPrismObject().debugDump())");
			DOMUtil.createComment(actionE, " <s:parameter><s:name>outputItem</s:name><c:value xmlns:c='http://midpoint.evolveum.com/xml/ns/public/common/common-3'>UserType</c:value></s:parameter> ");
		}
		
	}

	@Override
	public boolean supportsRawOption() {
		return action.supportsRaw;
	}

	@Override
	public boolean supportsDryRunOption() {
		return action.supportsDryRun;
	}

	@Override
	public boolean isExecutable() {
		return action != Action.MODIFY && action != Action.EXECUTE_SCRIPT;
	}

	@Override
	public boolean supportsWrapIntoTask() {
		return true;
	}

	public String generateFromSourceObject(SourceObject object, GeneratorOptions options) {
		Element pipe = DOMUtil.getDocument(new QName(Constants.SCRIPT_NS, "pipeline", "s")).getDocumentElement();
		createSingleSourceSearch(pipe, object);
		createAction(pipe, options);
		return DOMUtil.serializeDOMToString(pipe);
	}

	@Override
	protected boolean requiresExecutionConfirmation() {
		return action.requiresConfirmation;
	}
	
	public String getActionDescription() {
		return action.displayName;
	}

}
