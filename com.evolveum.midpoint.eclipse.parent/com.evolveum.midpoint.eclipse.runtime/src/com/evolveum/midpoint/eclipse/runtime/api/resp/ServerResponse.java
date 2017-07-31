package com.evolveum.midpoint.eclipse.runtime.api.resp;

import java.util.List;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.eclipse.runtime.api.OperationResultStatus;
import com.evolveum.midpoint.util.DOMUtil;

public class ServerResponse {

	protected int statusCode;
	protected String reasonPhrase;
	protected String rawResponseBody;
	protected Throwable exception;
	
	protected String operationResultStatusString;
	protected String operationResultMessage;
	
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
	public String getOperationResultStatusString() {
		return operationResultStatusString;
	}
	public void setOperationResultStatusString(String operationResultStatusString) {
		this.operationResultStatusString = operationResultStatusString;
	}
	public String getOperationResultMessage() {
		return operationResultMessage;
	}
	public void setOperationResultMessage(String operationResultMessage) {
		this.operationResultMessage = operationResultMessage;
	}

	public OperationResultStatus getStatus() {
		if (exception != null || statusCode < 200 || statusCode >= 300) {		// TODO
			return OperationResultStatus.ERROR;
		} else if (operationResultStatusString == null || "success".equals(operationResultStatusString)) {
			return OperationResultStatus.SUCCESS;
		} else if ("in_progress".equals(operationResultStatusString) || "warning".equals(operationResultStatusString) || "handledError".equals(operationResultStatusString)) {
			return OperationResultStatus.WARNING;
		} else {
			return OperationResultStatus.ERROR;
		}
	}

	public boolean isSuccess() {
		return getStatus() == OperationResultStatus.SUCCESS;
	}

	public boolean isWarning() {
		return getStatus() == OperationResultStatus.WARNING;
	}

	public String getErrorDescription() {
		if (exception != null) {
			return exception.toString();
		}
		StringBuilder sb = new StringBuilder();
		if (statusCode < 200 || statusCode >= 300) {
			sb.append(statusCode).append(": ").append(reasonPhrase);
		}
		if (operationResultStatusString != null) {
			if (sb.length() > 0) {
				sb.append("; ");
			}
			sb.append(operationResultStatusString).append(": ").append(operationResultMessage);
		}
		return sb.toString();
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
