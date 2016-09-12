package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.util.DOMUtil;

public class TaskGenerator extends Generator {
	
	private static final String URI_PREFIX = "http://midpoint.evolveum.com/xml/ns/public/model/synchronization";
	
	public enum Action {
		RECOMPUTE("recompute", URI_PREFIX + "/recompute/handler-3"), 
		DELETE("delete", "/delete/handler-3");
		
		private String displayName, handlerUri;
		private Action(String displayName, String handlerUri) {
			this.displayName = displayName;
			this.handlerUri = handlerUri;
		}
		public String getDisplayName() {
			return displayName;
		}
		public String getHandlerUri() {
			return handlerUri;
		}
	}
	
	private Action action;
	
	public TaskGenerator(Action action) {
		this.action = action;
	}

	@Override
	public String getLabel() {
		return "Task: " + action;
	}

	@Override
	public String generate(List<ServerObject> objects, GeneratorOptions options) {
		if (objects.isEmpty()) {
			return null;
		}
		Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "objects", "c"));
		Element root = doc.getDocumentElement();
		
		List<Batch> batches = createBatches(objects, options);
		for (Batch batch : batches) {
			Element task = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "task", "c"));
//			DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "name", "c")).setTextContent("Execute " + action.getDisplayName() + "on objects " + (batch.getFirst()+1) + " to " + (batch.getLast()+1));
//			Element extension = DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "extension", "c"));
//			Element executeScript = DOMUtil.createSubElement(extension, new QName(Constants.SCEXT_NS, "executeScript", "scext"));
//			
//			<extension>
//	        <mext:objectQuery>
//	            <q:filter>
//	                <q:equal>
//	                    <q:path>locality</q:path>
//	                    <q:value>Bratislava</q:value>
//	                </q:equal>
//	            </q:filter>
//	        </mext:objectQuery>
//	    </extension>
//				batchRoot = executeScript;
//			} else {
//				batchRoot = root;
//			}
			
//			Element pipe = DOMUtil.createSubElement(batchRoot, new QName(Constants.SCRIPT_NS, "pipeline", "s"));
//			Element search = DOMUtil.createSubElement(pipe, new QName(Constants.SCRIPT_NS, "expression", "s"));
//			DOMUtil.setXsiType(search, new QName(Constants.SCRIPT_NS, "SearchExpressionType", "s"));
//			DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent("ObjectType");
//			Element filter = DOMUtil.createSubElement(search, new QName(Constants.SCRIPT_NS, "searchFilter", "s"));
//			Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);	
//			for (ServerObject o : batch.getObjects()) {
//				DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent(o.getOid());
//				DOMUtil.createComment(inOid, " " + o.getName() + " ");
//			}
//			
//			Element action = DOMUtil.createSubElement(pipe, new QName(Constants.SCRIPT_NS, "expression", "s"));
//			DOMUtil.setXsiType(action, new QName(Constants.SCRIPT_NS, "ActionExpressionType", "s"));
//			DOMUtil.createSubElement(action, new QName(Constants.SCRIPT_NS, "type", "s")).setTextContent(actionName);
//			
//			if (task != null) {
//				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "ownerRef", "c")).setAttribute("oid", "00000000-0000-0000-0000-000000000002");
//				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "executionStatus", "c")).setTextContent(
//						options.isCreateSuspended() ? "suspended" : "runnable"
//						);
//				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "category", "c")).setTextContent("BulkAction");
//				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "handlerUri", "c")).setTextContent("http://midpoint.evolveum.com/xml/ns/public/model/scripting/handler-3");
//				DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "recurrence", "c")).setTextContent("single");
//			}
		}
			
		return DOMUtil.serializeDOMToString(doc);
	}

	@Override
	public boolean supportsRawOption() {
		return true;
	}

	@Override
	public boolean supportsDryRunOption() {
		return true;
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
