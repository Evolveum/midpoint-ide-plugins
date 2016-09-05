package com.evolveum.midpoint.eclipse.runtime.api;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.util.DOMUtil;

public class CompareServerResponse extends ServerResponse {

	protected boolean wasParsed;
	
	protected String local;
	protected String remote;
	protected String localToRemote;
	protected String remoteToLocal;
	
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

	public void parseXmlResponse(String string) {
		Element root = DOMUtil.parseDocument(rawResponseBody).getDocumentElement();

		remoteToLocal = getContent(root, Constants.API_TYPES_NS, "currentToProvided");
		localToRemote = getContent(root, Constants.API_TYPES_NS, "providedToCurrent");
		local = getContent(root, Constants.API_TYPES_NS, "normalizedObject");
		remote = getContent(root, Constants.API_TYPES_NS, "currentObject");
		wasParsed = true;
	}

	private String getContent(Element root, String nsUri, String elementName) {
		NodeList list = root.getElementsByTagNameNS(nsUri, elementName);
		if (list.getLength() > 0) {
			return DOMUtil.serializeDOMToString(list.item(0));
		} else {
			return null;
		}
	}

	public boolean wasParsed() {
		return wasParsed;
	}

}
