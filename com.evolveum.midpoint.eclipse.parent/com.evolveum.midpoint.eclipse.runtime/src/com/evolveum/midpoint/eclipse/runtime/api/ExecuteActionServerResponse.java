package com.evolveum.midpoint.eclipse.runtime.api;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.util.DOMUtil;

public class ExecuteActionServerResponse extends ServerResponse {

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

		Element xmlDataElement = getElement(root, Constants.API_TYPES_NS, "xmlData");
		if (xmlDataElement != null) {
			DOMUtil.fixNamespaceDeclarations(xmlDataElement);
			dataOutput = DOMUtil.serializeDOMToString(xmlDataElement);
		}
		
		consoleOutput = getElementTextContent(root, Constants.API_TYPES_NS, "textOutput");
		
		NodeList resultList = root.getElementsByTagNameNS(Constants.MODEL_NS, "result");
		if (resultList.getLength() > 0) {
			Element result = (Element) resultList.item(0);
			DOMUtil.fixNamespaceDeclarations(result);
			operationResultStatus = getElementTextContent(result, Constants.COMMON_NS, "status");
			operationResultMessage = getElementTextContent(result, Constants.COMMON_NS, "message");
			operationResult = DOMUtil.serializeDOMToString(result);
		}
		
		wasParsed = true;
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
