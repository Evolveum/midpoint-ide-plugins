package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.util.DOMUtil;

public abstract class Generator {

	public static final Generator NULL_GENERATOR = new NullGenerator();
	public abstract String getLabel();
	public abstract String generate(List<ServerObject> objects, GeneratorOptions options);

	public boolean supportsRawOption() {
		return false;
	}
	public boolean supportsDryRunOption() {
		return false;
	}
	public boolean supportsSymbolicReferences() {
		return false;
	}
	public boolean supportsSymbolicReferencesAtRuntime() {
		return false;
	}
	public boolean isExecutable() {
		return false;
	}
	
	public void createRefContent(Element refRoot, ServerObject object, GeneratorOptions options) {
		createRefContent(refRoot, object, options, getSymbolicRefItemName(object), getSymbolicRefItemValue(object));
	}
	
	public static void createRefContent(Element refRoot, ServerObject object, GeneratorOptions options, String symbolicRefItemName, String symbolicRefItemValue) {
		DOMUtil.setQNameAttribute(refRoot, "type", object.getType().getTypeQName());
		if (options.isSymbolicReferences()) {
			Element filter = DOMUtil.createSubElement(refRoot, new QName(Constants.COMMON_NS, "filter", "c"));
			Element equal = DOMUtil.createSubElement(filter, new QName(Constants.QUERY_NS, "equal", "q"));
			DOMUtil.createSubElement(equal, new QName(Constants.QUERY_NS, "path", "q")).setTextContent(symbolicRefItemName);
			DOMUtil.createSubElement(equal, new QName(Constants.QUERY_NS, "value", "q")).setTextContent(symbolicRefItemValue);
			if (options.isSymbolicReferencesRuntime()) {
				DOMUtil.createSubElement(refRoot, new QName(Constants.COMMON_NS, "resolutionTime", "c")).setTextContent("run");
			}
		} else {
			refRoot.setAttribute("oid", object.getOid());
			DOMUtil.createComment(refRoot, " " + object.getName() + " ");
		}
	}
	
	protected String getSymbolicRefItemValue(ServerObject object) {
		return object.getName();
	}

	protected String getSymbolicRefItemName(ServerObject object) {
		return "name";
	}

	protected void createInOidQueryFilter(Element filter, List<ServerObject> objects) {
		Element inOid = DOMUtil.createSubElement(filter, Constants.Q_IN_OID);	
		for (ServerObject o : objects) {
			DOMUtil.createSubElement(inOid, Constants.Q_VALUE).setTextContent(o.getOid());
		}
	}

	public boolean supportsWrapIntoTask() {
		return false;
	}
	public boolean supportsCreateSuspended() {
		return false;
	}
	protected List<Batch> createBatches(List<ServerObject> objects, GeneratorOptions options, ObjectTypes applicableTo) {
		List<Batch> rv = new ArrayList<Batch>();
		Batch current = null;
		int index = 0;
		for (ServerObject object : objects) {
			if (!applicableTo.isAssignableFrom(object.getType())) {
				continue;
			}
			if (current == null || current.getObjects().size() == options.getBatchSize()) {
				current = new Batch();
				current.setFirst(index);
				rv.add(current);
			}
			current.getObjects().add(object);
			index++;
		}
		return rv;
	}

	protected boolean requiresExecutionConfirmation() {
		return false;
	}
	
	public String getActionDescription() {
		return null;
	}
}
