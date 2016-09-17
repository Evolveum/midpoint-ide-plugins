package com.evolveum.midpoint.eclipse.runtime.api.resp;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
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
			operationResultMessage = getMessage(result);
			operationResult = DOMUtil.serializeDOMToString(result);
		}
		
		wasParsed = true;
	}

	public String getMessage(Element result) {
		String msg = getElementTextContent(result, Constants.COMMON_NS, "message");
		// brutal hack: resolving test resource messages
		if (msg == null || !(msg.equals("Test resource has failed"))) {
			return msg;
		}
		Element partialResult = DOMUtil.getChildElement(result, "partialResults");
		if (partialResult != null) {
			String msg2 = getMessage(partialResult);
			if (StringUtils.isNotBlank(msg2)) {
				return msg2;
			} else {
				return msg;			// should not occur
			}
		} else {
			return msg;				// should not occur
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
