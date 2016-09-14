package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.util.DOMUtil;

public class TaskGenerator extends Generator {
	
	private static final String URI_PREFIX = "http://midpoint.evolveum.com/xml/ns/public/model/synchronization";
	
	public enum Action {
		RECOMPUTE("recompute", URI_PREFIX + "/recompute/handler-3", ObjectTypes.FOCUS, "Recomputation"), 
		DELETE("delete", URI_PREFIX + "/delete/handler-3", ObjectTypes.OBJECT, "Utility"),
		MODIFY("modify (execute changes)", URI_PREFIX + "/execute/handler-3", ObjectTypes.OBJECT, "ExecuteChanges");
		
		private final String displayName, handlerUri, category;
		private final ObjectTypes applicableTo;
		
		private Action(String displayName, String handlerUri, ObjectTypes applicableTo, String category) {
			this.displayName = displayName;
			this.handlerUri = handlerUri;
			this.applicableTo = applicableTo;
			this.category = category;
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
		return "Native task: " + action.displayName;
	}

	@Override
	public String generate(List<ServerObject> objects, GeneratorOptions options) {
		if (objects.isEmpty()) {
			return null;
		}
		Document doc = DOMUtil.getDocument(new QName(Constants.COMMON_NS, "objects", "c"));
		Element root = doc.getDocumentElement();
		
		List<Batch> batches = createBatches(objects, options, action.applicableTo);
		for (Batch batch : batches) {
			
			Element task = DOMUtil.createSubElement(root, new QName(Constants.COMMON_NS, "task", "c"));
			DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "name", "c")).setTextContent("Execute " + action.getDisplayName() + " on objects " + (batch.getFirst()+1) + " to " + (batch.getLast()+1));
			Element extension = DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "extension", "c"));
			DOMUtil.createSubElement(extension, new QName(Constants.MEXT_NS, "objectType", "mext")).setTextContent(action.applicableTo.getTypeName());
			Element objectQuery = DOMUtil.createSubElement(extension, new QName(Constants.MEXT_NS, "objectQuery", "mext"));
			Element filter = DOMUtil.createSubElement(objectQuery, Constants.Q_FILTER_Q);
			Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID_Q);	
			for (ServerObject o : batch.getObjects()) {
				DOMUtil.createSubElement(inOid, Constants.Q_VALUE_Q).setTextContent(o.getOid());
				DOMUtil.createComment(inOid, " " + o.getName() + " ");
			}
			
			ObjectTypes superType = null;
			for (ServerObject o : batch.getObjects()) {
				superType = ObjectTypes.commonSuperType(superType, o.getType());
			}
			
			if (action == Action.MODIFY) {
				Element delta = DOMUtil.createSubElement(extension, new QName(Constants.MEXT_NS, "objectDelta", "mext"));
				DOMUtil.createSubElement(delta, new QName(Constants.TYPES_NS, "changeType", "t")).setTextContent("modify");
				DOMUtil.createSubElement(delta, new QName(Constants.TYPES_NS, "objectType", "t")).setTextContent(superType.getTypeName());
				DOMUtil.createSubElement(delta, new QName(Constants.TYPES_NS, "oid", "t")).setTextContent("unused");
				Element itemDelta = DOMUtil.createSubElement(delta, new QName(Constants.TYPES_NS, "itemDelta", "t"));
				DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "modificationType", "t")).setTextContent("add");
				DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "path", "t")).setTextContent("TODO (e.g. displayName)");
				Element value = DOMUtil.createSubElement(itemDelta, new QName(Constants.TYPES_NS, "value", "t"));
				DOMUtil.setXsiType(value, DOMUtil.XSD_STRING);
				value.setTextContent("TODO");
			}
			
			DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "taskIdentifier", "c")).setTextContent(BulkActionGenerator.generateTaskIdentifier());
			DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "ownerRef", "c")).setAttribute("oid", "00000000-0000-0000-0000-000000000002");
			DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "executionStatus", "c")).setTextContent(
					options.isCreateSuspended() ? "suspended" : "runnable"
					);
			DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "category", "c")).setTextContent(action.category);
			DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "handlerUri", "c")).setTextContent(action.handlerUri);
			DOMUtil.createSubElement(task, new QName(Constants.COMMON_NS, "recurrence", "c")).setTextContent("single");
		}
			
		return DOMUtil.serializeDOMToString(doc);
	}

	@Override
	public boolean supportsRawOption() {
		return true;
	}

	@Override
	public boolean supportsDryRunOption() {
		return false;
	}

	@Override
	public boolean isExecutable() {
		return action != Action.MODIFY;
	}

	@Override
	public boolean supportsWrapIntoTask() {
		return false;
	}

}
