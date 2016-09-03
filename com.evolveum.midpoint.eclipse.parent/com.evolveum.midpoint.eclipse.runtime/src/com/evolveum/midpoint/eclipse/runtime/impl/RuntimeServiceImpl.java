package com.evolveum.midpoint.eclipse.runtime.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

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
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ExecuteActionServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.ServerAction;
import com.evolveum.midpoint.eclipse.runtime.api.ServerObject;
import com.evolveum.midpoint.eclipse.runtime.api.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.ServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.UploadServerResponse;
import com.evolveum.midpoint.util.DOMUtil;

public class RuntimeServiceImpl implements RuntimeService {

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
	public void testConnection(ConnectionParameters parameters) throws IOException {
		
		HttpClient client = createClient(parameters);

		String url = parameters.getUrl() + "/users/search";
		HttpPost request = new HttpPost(url);

		HttpEntity body = new StringEntity("<query><filter><inOid><value>none</value></inOid></filter></query>", ContentType.APPLICATION_XML);
		request.setEntity(body);
		
		HttpResponse response = client.execute(request);
		
		StatusLine statusLine = response.getStatusLine();
		if (!isSuccess(statusLine)) {
			throw new IOException("Server response: " + statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
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
			Document document = DOMUtil.parse(new ByteArrayInputStream(request.getData()));
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
				throw new IllegalArgumentException("Unsupported root element for an action: " + localName + "; supported ones are: " + SCRIPTING_ACTIONS);
			} else if (action == ServerAction.UPLOAD && !uploadable) {
				throw new IllegalArgumentException("Unknown object type to upload: " + localName);
			} else if (!executable && !uploadable) {
				throw new IllegalArgumentException("Object with root element of <" + localName + "> cannot be uploaded nor executed.");
			}
		} catch (Throwable t) {
			return new ServerResponse(t);
		}
		
		ServerResponse serverResponse = executable ? new ExecuteActionServerResponse() : new UploadServerResponse(); 

		try {
			HttpEntityEnclosingRequest httpRequest;
			if (uploadable) {
				String url = connectionParameters.getUrl() + "/" + restType;
				String suffix = "?options=raw";

				if (oid != null && !oid.isEmpty()) {
					httpRequest = new HttpPut(url + "/" + oid + suffix);
				} else {
					httpRequest = new HttpPost(url + suffix);
				}
			} else {
				String url = connectionParameters.getUrl() + "/scriptExecutions";
				httpRequest = new HttpPost(url);
			}

			HttpClient client = createClient(connectionParameters);
			HttpEntity body = new ByteArrayEntity(request.getData(), ContentType.APPLICATION_XML);
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
				if (serverResponse instanceof ExecuteActionServerResponse) {
					Header contentType = response.getEntity().getContentType();
					System.out.println("Content type of the response: " + contentType);
					if (contentType != null && contentType.getValue().startsWith("application/xml")) {
						((ExecuteActionServerResponse) serverResponse).parseXmlResponse(sb.toString());
					}
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
	public List<ServerObject> downloadObjects(ObjectTypes type, int limit, ConnectionParameters connectionParameters) throws IOException {

		HttpClient client = createClient(connectionParameters);

		String url = connectionParameters.getUrl() + "/"+type.getRestType()+"/search";
		HttpPost request = new HttpPost(url);

		HttpEntity body = new StringEntity("<query><paging><maxSize>"+limit+"</maxSize></paging></query>", ContentType.APPLICATION_XML);
		request.setEntity(body);
		
		System.out.println("Requesting objects from " + url);
		HttpResponse response = client.execute(request);

		StatusLine statusLine = response.getStatusLine();
		System.out.println("Server response status line: " + statusLine);
		if (!isSuccess(statusLine)) {
			throw new IOException("Server response: " + statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase());
		}

		List<ServerObject> rv = new ArrayList<>();
		if (response.getEntity() != null) {
			Element root = DOMUtil.parse(response.getEntity().getContent()).getDocumentElement();
			List<Element> objectElements = DOMUtil.getChildElements(root, new QName(Constants.API_TYPES_NS, "object"));
			for (Element objectElement : objectElements) {
				String xml = DOMUtil.serializeDOMToString(objectElement);
				Element nameElement = DOMUtil.getChildElement(objectElement, new QName(Constants.COMMON_NS, "name"));
				String oid = DOMUtil.getAttribute(objectElement, new QName("oid"));
				ServerObject object = new ServerObject(oid, nameElement != null ? nameElement.getTextContent() : null, type, xml);
				rv.add(object);
			}
		}
		return rv;
	}
	
	
}
