package com.evolveum.midpoint.eclipse.runtime.api.resp;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.util.DOMUtil;

public class ServerResponse {

	protected int statusCode;
	protected String reasonPhrase;
	protected String rawResponseBody;
	protected Throwable exception;
	
	public ServerResponse() {
	}
	
	public ServerResponse(Throwable t) {
		exception = t;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int responseCode) {
		this.statusCode = responseCode;
	}
	public String getReasonPhrase() {
		return reasonPhrase;
	}
	public void setReasonPhrase(String responseStatusLineText) {
		this.reasonPhrase = responseStatusLineText;
	}
	public String getRawResponseBody() {
		return rawResponseBody;
	}
	public void setRawResponseBody(String rawResponseBody) {
		this.rawResponseBody = rawResponseBody;
	}
	public Throwable getException() {
		return exception;
	}
	public void setException(Throwable exception) {
		this.exception = exception;
	}

	public boolean isSuccess() {
		return exception == null && statusCode >= 200 && statusCode < 300;		// TODO operationResult
	}

	public String getErrorDescription() {
		if (exception != null) {
			return exception.toString();
		}
		return statusCode + ": " + reasonPhrase;
	}
	
	protected Element getElement(Element root, String nsUri, String elementName) {
		NodeList list = root.getElementsByTagNameNS(nsUri, elementName);
		if (list.getLength() > 0) {
			return (Element) list.item(0);
		} else {
			return null;
		}
	}

	protected String serialize(Element e) {
		return e != null ? DOMUtil.serializeDOMToString(e) : null;
	}

	protected String getContent(Element root, String nsUri, String elementName) {
		return serialize(getElement(root, nsUri, elementName));
	}

	protected String getElementTextContent(Element root, String ns, String localName) {
		if (root == null) {
			return null;
		}
		NodeList nodes = root.getElementsByTagNameNS(ns, localName);
		if (nodes.getLength() > 0) {
			return ((Element) (nodes.item(0))).getTextContent();
		} else {
			return null;
		}
	}

	protected List<Element> getDirectChildren(Element element, String ns, String localName) {
		return DOMUtil.getChildElements(element, new QName(ns, localName));
	}
	
	protected void fixNamespaceDeclarations(Element e) {
		if (e != null) {
			DOMUtil.fixNamespaceDeclarations(e);
		}
	}
}
