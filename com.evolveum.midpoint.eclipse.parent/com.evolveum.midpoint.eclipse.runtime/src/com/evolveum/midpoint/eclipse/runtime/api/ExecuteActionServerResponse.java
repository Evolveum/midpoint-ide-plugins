package com.evolveum.midpoint.eclipse.runtime.api;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.util.DOMUtil;

public class ExecuteActionServerResponse extends ServerResponse {

	public static final String MODEL_NS = "http://midpoint.evolveum.com/xml/ns/public/model/model-3";
	public static final String API_TYPES_NS = "http://midpoint.evolveum.com/xml/ns/public/common/api-types-3";
	public static final String COMMON_NS = "http://midpoint.evolveum.com/xml/ns/public/common/common-3";
	
	protected boolean wasParsed;
	
	protected String dataOutput;
	protected String consoleOutput;
	protected String operationResult;
	protected String operationResultStatus;
	protected String operationResultMessage;
	
	public ExecuteActionServerResponse() {
	}
	
	public ExecuteActionServerResponse(Throwable t) {
		super(t);
	}
	
	@Override
	public boolean isSuccess() {
		return super.isSuccess() && (operationResultStatus == null || "success".equals(operationResultStatus));
	}
	
	public String getDataOutput() {
		return dataOutput;
	}

	public String getConsoleOutput() {
		return consoleOutput;
	}

	public String getOperationResult() {
		return operationResult;
	}

	public String getOperationResultStatus() {
		return operationResultStatus;
	}

	public String getOperationResultMessage() {
		return operationResultMessage;
	}

	public void parseXmlResponse(String string) {
		Element root = DOMUtil.parseDocument(rawResponseBody).getDocumentElement();

		NodeList xmlDataList = root.getElementsByTagNameNS(API_TYPES_NS, "xmlData");
		if (xmlDataList.getLength() > 0) {
			dataOutput = DOMUtil.serializeDOMToString(xmlDataList.item(0));
		}
		
		consoleOutput = getElementTextContent(root, API_TYPES_NS, "textOutput");
		
		NodeList resultList = root.getElementsByTagNameNS(MODEL_NS, "result");
		if (resultList.getLength() > 0) {
			Element result = (Element) resultList.item(0);
			operationResultStatus = getElementTextContent(result, COMMON_NS, "status");
			operationResultMessage = getElementTextContent(result, COMMON_NS, "message");
			operationResult = DOMUtil.serializeDOMToString(result);
		}
		
		wasParsed = true;
	}

	private String getElementTextContent(Element root, String ns, String localName) {
		NodeList nodes = root.getElementsByTagNameNS(ns, localName);
		if (nodes.getLength() > 0) {
			return ((Element) (nodes.item(0))).getTextContent();
		} else {
			return null;
		}
	}

	public boolean wasParsed() {
		return wasParsed;
	}

	@Override
	public String getErrorDescription() {
		if (StringUtils.isNotBlank(operationResultMessage)) {
			return operationResultMessage;
		}
		return super.getErrorDescription();
	}
}
