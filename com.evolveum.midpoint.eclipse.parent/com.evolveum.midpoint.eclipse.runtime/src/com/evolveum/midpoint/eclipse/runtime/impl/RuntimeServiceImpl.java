package com.evolveum.midpoint.eclipse.runtime.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.req.CompareServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerAction;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.CompareServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ExecuteActionServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.NotApplicableServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.SearchObjectsServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.TestConnectionResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.UploadServerResponse;
import com.evolveum.midpoint.util.DOMUtil;

public class RuntimeServiceImpl implements RuntimeService {
	
	public static final String REST = "/ws/rest";

	public static final List<String> SCRIPTING_ACTIONS = Arrays.asList(
			"scriptingExpression",
			"sequence",
			"pipeline",
			"search",
			"filter",
			"select",
			"foreach",
			"action"
			);
	
	@Override
	public TestConnectionResponse testConnection(ConnectionParameters parameters) {
		
		try {
			HttpClient client = createClient(parameters);
			String url = parameters.getUrl() + REST + "/users/search";
			HttpPost request = new HttpPost(url);

			HttpEntity body = new StringEntity("<query><filter><inOid><value>none</value></inOid></filter></query>", ContentType.APPLICATION_XML);
			request.setEntity(body);
		
			HttpResponse response = client.execute(request);
		
			StatusLine statusLine = response.getStatusLine();
			if (isSuccess(statusLine)) {
				return new TestConnectionResponse(true, null, null);
			} else {
				return new TestConnectionResponse(false, "Server response: " + statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase(), null);
			}
		} catch (Throwable t) {
			return new TestConnectionResponse(false, null, t);
		}
	}

	private HttpClient createClient(ConnectionParameters parameters) {
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(parameters.getLogin(), parameters.getPassword()));
		
		HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build();
		return client;
	}

	private boolean isSuccess(StatusLine statusLine) {
		int code = statusLine.getStatusCode();
		return code >= 200 && code < 300;
	}

	@Override
	public ServerResponse executeServerRequest(ServerRequest request, ConnectionParameters connectionParameters) {
		
		String oid;
		String restType;
		boolean uploadable, executable;
		
		try {
			Document document = DOMUtil.parseDocument(request.getData());
			Element rootElement = document.getDocumentElement();
			String nodeName = rootElement.getNodeName();
			String localName = rootElement.getLocalName();
			String uri = rootElement.getNamespaceURI();
			oid = rootElement.getAttribute("oid");
			System.out.println("Node name: " + nodeName + ", localName: " + localName + ", uri: " + uri + ", oid: " + oid);

			restType = ObjectTypes.getRestTypeForElementName(localName);
			uploadable = restType != null;
			executable = SCRIPTING_ACTIONS.contains(localName);

			ServerAction action = request.getAction();
			if (action == ServerAction.EXECUTE && !executable) {
				return new NotApplicableServerResponse("Unsupported root element for an action: " + localName + "; supported ones are: " + SCRIPTING_ACTIONS);
			} else if (action == ServerAction.UPLOAD && !uploadable) {
				return new NotApplicableServerResponse("Unknown object type to upload: " + localName);
			} else if (!executable && !uploadable) {
				return new NotApplicableServerResponse("Object with root element of <" + localName + "> cannot be uploaded, executed nor compared.");
			}
		} catch (Throwable t) {
			return new ServerResponse(t);
		}
		
		ServerResponse serverResponse; 

		ServerAction finalAction;
		if (request.getAction() == ServerAction.COMPARE) {
			finalAction = ServerAction.COMPARE;
			serverResponse = new CompareServerResponse();
		} else if (uploadable) {
			finalAction = ServerAction.UPLOAD;
			serverResponse = new UploadServerResponse();
		} else {
			finalAction = ServerAction.EXECUTE;
			serverResponse = new ExecuteActionServerResponse();
		}

		try {
			HttpEntityEnclosingRequest httpRequest;
			
			if (finalAction == ServerAction.COMPARE) {
				List<String> opts = new ArrayList<>();
				CompareServerRequest csr = (CompareServerRequest) request;
				if (csr.isShowLocalToRemote()) {
					opts.add("compareOptions=computeProvidedToCurrent");
				}
				if (csr.isShowRemoteToLocal()) {
					opts.add("compareOptions=computeCurrentToProvided");
				}
				if (csr.isShowLocal()) {
					opts.add("compareOptions=returnNormalized");
				}
				if (csr.isShowRemote()) {
					opts.add("compareOptions=returnCurrent");
				}
				if (opts.isEmpty()) {
					throw new IllegalStateException("None of compare options are selected.");
				}
				opts.add("compareOptions=ignoreOperationalItems");
				String compareOptions = StringUtils.join(opts, "&");
				List<String> ignore = new ArrayList<>();
				for (String item : csr.getIgnoreItems()) {
					ignore.add("ignoreItems=" + item);
				}
				String ignoreItems = StringUtils.join(ignore, "&");
				String url = connectionParameters.getUrl() + REST + "/comparisons?readOptions=raw&" + compareOptions + "&" + ignoreItems;
				httpRequest = new HttpPost(url);
			} else if (finalAction == ServerAction.UPLOAD) {
				String url = connectionParameters.getUrl() + REST + "/" + restType;
				String suffix = "?options=raw";

				if (oid != null && !oid.isEmpty()) {
					httpRequest = new HttpPut(url + "/" + oid + suffix);
				} else {
					httpRequest = new HttpPost(url + suffix);
				}
			} else {
				String url = connectionParameters.getUrl() + REST + "/scriptExecutions";
				httpRequest = new HttpPost(url);
			}

			HttpClient client = createClient(connectionParameters);
			HttpEntity body = new StringEntity(request.getData(), ContentType.APPLICATION_XML);				// TODO charset if defined in XML?
			httpRequest.setEntity(body);

			HttpUriRequest uriRequest = (HttpUriRequest) httpRequest;
			System.out.println("Invoking " + uriRequest.getMethod() + " on " + uriRequest.getURI());
			HttpResponse response = client.execute(uriRequest);

			StatusLine statusLine = response.getStatusLine();
			System.out.println("Server response status line: " + statusLine);

			if (response.getEntity() != null) {
				// TODO encoding!
				InputStream is = response.getEntity().getContent();
				StringBuilder sb = new StringBuilder();
				if (is != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(is));
					String line;
					while ((line=br.readLine()) != null) {
						sb.append(line).append("\n");
					}
					is.close();
				}
				serverResponse.setRawResponseBody(sb.toString());
				System.out.println("Server response (raw):\n" + sb.toString() + "\n---------------------------------");
				if (serverResponse instanceof ExecuteActionServerResponse) {
					Header contentType = response.getEntity().getContentType();
					System.out.println("Content type of the response: " + contentType);
					if (contentType != null && contentType.getValue().startsWith("application/xml") && isSuccess(statusLine)) {
						((ExecuteActionServerResponse) serverResponse).parseXmlResponse(sb.toString());
					}
				} else if (serverResponse instanceof CompareServerResponse && isSuccess(statusLine)) {
					((CompareServerResponse) serverResponse).parseXmlResponse(sb.toString(), (CompareServerRequest) request);
				}
			}
			
			serverResponse.setStatusCode(statusLine.getStatusCode());
			serverResponse.setReasonPhrase(statusLine.getReasonPhrase());
			
		} catch (Throwable t) {
			serverResponse.setException(t);
		}

		return serverResponse;

	}

	@Override
	public SearchObjectsServerResponse getObjects(ObjectTypes type, int limit, ConnectionParameters connectionParameters) {
		String query = "<query><paging><orderBy>name</orderBy><maxSize>"+limit+"</maxSize></paging></query>";
		return executeQuery(type, query, connectionParameters);
	}
	
	private SearchObjectsServerResponse executeQuery(ObjectTypes type, String query, ConnectionParameters connectionParameters) {

		SearchObjectsServerResponse resp = new SearchObjectsServerResponse();

		try {
			HttpClient client = createClient(connectionParameters);

			String url = connectionParameters.getUrl() + REST + "/"+type.getRestType()+"/search?include=row&include=jpegPhoto";
			HttpPost request = new HttpPost(url);

			HttpEntity body = new StringEntity(query, ContentType.APPLICATION_XML);
			request.setEntity(body);

			System.out.println("Requesting objects from " + url);
			HttpResponse response = client.execute(request);

			StatusLine statusLine = response.getStatusLine();
			System.out.println("Server response status line: " + statusLine);

			resp.setStatusCode(statusLine.getStatusCode());
			resp.setReasonPhrase(statusLine.getReasonPhrase());

			if (!isSuccess(statusLine)) {
				return resp;
			}

			List<ServerObject> objects = new ArrayList<>();
			if (response.getEntity() != null) {
				Element root = DOMUtil.parse(response.getEntity().getContent()).getDocumentElement();
				List<Element> objectElements = DOMUtil.getChildElements(root, new QName(Constants.API_TYPES_NS, "object"));
				for (Element objectElement : objectElements) {
					fixObjectName(objectElement);
					DOMUtil.fixNamespaceDeclarations(objectElement);
					String xml = DOMUtil.serializeDOMToString(objectElement);
					Element nameElement = DOMUtil.getChildElement(objectElement, new QName(Constants.COMMON_NS, "name"));
					String oid = DOMUtil.getAttribute(objectElement, new QName("oid"));
					ServerObject object = new ServerObject(oid, nameElement != null ? nameElement.getTextContent() : null, type, xml);
					objects.add(object);
				}
			}
			resp.getServerObjects().addAll(objects);
			return resp;
		} catch (Throwable t) {
			resp.setException(t);
		}
		return resp;
	}

	public static void fixObjectName(Element objectElement) {
		QName xsitype = DOMUtil.resolveXsiType(objectElement);
		if (xsitype == null) {
			return;
		}
		String elementName = ObjectTypes.getElementNameForXsiType(xsitype.getLocalPart());
		if (elementName == null) {
			return;
		}
		objectElement.getOwnerDocument().renameNode(objectElement, Constants.COMMON_NS, elementName);
		DOMUtil.removeXsiType(objectElement);
	}

	@Override
	public ServerResponse getCurrentVersionOfObject(String data, ConnectionParameters connectionParameters) {
		try {
			Document document = DOMUtil.parseDocument(data);
			Element rootElement = document.getDocumentElement();
			String nodeName = rootElement.getNodeName();
			String localName = rootElement.getLocalName();
			String uri = rootElement.getNamespaceURI();
			String oid = rootElement.getAttribute("oid");
			System.out.println("Node name: " + nodeName + ", localName: " + localName + ", uri: " + uri + ", oid: " + oid);
			
			if (StringUtils.isNotBlank(oid)) {
				return getObject(oid, connectionParameters);
			}
			
			Element nameElement = DOMUtil.getChildElement(rootElement, "name");
			if (nameElement == null) {
				return new NotApplicableServerResponse("There is no OID nor name in this object");
			}
			String name = nameElement.getTextContent();
			System.out.println("Object name: " + name);
			ObjectTypes type = ObjectTypes.findByElementName(localName);
			if (type == null) {
				return new NotApplicableServerResponse("Object with root element of <" + localName + "> cannot be updated - unknown object type");
			}
			
			String query = "<query>"
							+ "<filter>"
								+ "<equal>"
									+ "<path>name</path>"
									+ "<value>" + name + "</value>"
								+ "</equal>"
							+ "</filter>"
						+ "</query>";
			
			return executeQuery(type, query, connectionParameters);

		} catch (Throwable t) {
			return new ServerResponse(t);
		}
	}

	@Override
	public SearchObjectsServerResponse getObject(String oid, ConnectionParameters connectionParameters) {
		return executeQuery(ObjectTypes.OBJECT, "<query><filter><inOid><value>"+oid+"</value></inOid></filter></query>", connectionParameters);
	}
	
}
