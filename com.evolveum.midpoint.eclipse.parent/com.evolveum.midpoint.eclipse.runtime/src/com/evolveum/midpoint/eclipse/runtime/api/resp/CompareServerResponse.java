package com.evolveum.midpoint.eclipse.runtime.api.resp;

import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.req.CompareServerRequest;
import com.evolveum.midpoint.util.DOMUtil;

public class CompareServerResponse extends ServerResponse {

	protected boolean wasParsed;
	
	protected String local;
	protected String remote;
	protected String localToRemote;
	protected String remoteToLocal;
	
	protected Boolean remoteExists;
	protected Integer itemDifferencesCount;
	
	public CompareServerResponse() {
	}
	
	public CompareServerResponse(Throwable t) {
		super(t);
	}
	
	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public String getLocalToRemote() {
		return localToRemote;
	}

	public void setLocalToRemote(String localToRemote) {
		this.localToRemote = localToRemote;
	}

	public String getRemoteToLocal() {
		return remoteToLocal;
	}

	public void setRemoteToLocal(String remoteToLocal) {
		this.remoteToLocal = remoteToLocal;
	}
	
	public Boolean getRemoteExists() {
		return remoteExists;
	}

	public Integer getItemDifferencesCount() {
		return itemDifferencesCount;
	}

	public void parseXmlResponse(String string, CompareServerRequest request) {
		Element root = DOMUtil.parseDocument(rawResponseBody).getDocumentElement();

		Element localToRemoteElement = getElement(root, Constants.API_TYPES_NS, "providedToCurrent");
		Element remoteToLocalElement = getElement(root, Constants.API_TYPES_NS, "currentToProvided");
		Element localElement = getElement(root, Constants.API_TYPES_NS, "normalizedObject");
		Element remoteElement = getElement(root, Constants.API_TYPES_NS, "currentObject");
	
		fixNamespaceDeclarations(localToRemoteElement);
		
		String changeTypeL2R = getChangeType(localToRemoteElement);
		String changeTypeR2L = getChangeType(remoteToLocalElement);
		
		if ("modify".equals(changeTypeL2R) || "modify".equals(changeTypeR2L)) {
			remoteExists = true;
		} else if ("add".equals(changeTypeL2R) || "delete".equals(changeTypeR2L)) {
			remoteExists = false;
		} else if (request.isShowRemote()) {
			remoteExists = remoteElement != null;
		} else {
			remoteExists = null;
		}
		
		Integer diffsL2R = getDifferenceCount(localToRemoteElement);
		Integer diffsR2L = getDifferenceCount(localToRemoteElement);
		if (diffsL2R != null) {
			itemDifferencesCount = diffsL2R;
		} else if (diffsR2L != null) {
			itemDifferencesCount = diffsR2L;
		}

		localToRemote = serialize(localToRemoteElement);
		remoteToLocal = serialize(remoteToLocalElement);
		local = serialize(localElement);
		remote = serialize(remoteElement);
		
		wasParsed = true;
	}
	
	private String getChangeType(Element delta) {
		return getElementTextContent(delta, Constants.TYPES_NS, "changeType");  
	}
	
	private Integer getDifferenceCount(Element delta) {
		if (!"modify".equals(getChangeType(delta))) {
			return null;
		}
		return getDirectChildren(delta, Constants.TYPES_NS, "itemDelta").size();
	}

	public boolean wasParsed() {
		return wasParsed;
	}

	public boolean noDifferences() {
		return Boolean.TRUE.equals(remoteExists) && itemDifferencesCount != null && itemDifferencesCount.intValue() == 0;
	}

}
