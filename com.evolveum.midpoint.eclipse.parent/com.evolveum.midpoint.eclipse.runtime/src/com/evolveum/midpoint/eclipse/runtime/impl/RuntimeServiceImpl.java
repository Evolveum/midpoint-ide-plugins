package com.evolveum.midpoint.eclipse.runtime.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
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
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.QueryInterpretation;
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
		
		try {
			Document document = DOMUtil.parseDocument(request.getData());
			Element rootElement = document.getDocumentElement();
			String nodeName = rootElement.getNodeName();
			String localName = rootElement.getLocalName();
			String uri = rootElement.getNamespaceURI();
			oid = rootElement.getAttribute("oid");
			System.out.println("Node name: " + nodeName + ", localName: " + localName + ", uri: " + uri + ", oid: " + oid);
			restType = ObjectTypes.getRestTypeForElementName(localName);
		} catch (Throwable t) {
			return new ServerResponse(t);
		}
		
		ServerResponse serverResponse; 

		ServerAction finalAction = request.getAction();
		switch (finalAction) {
		case UPLOAD: serverResponse = new UploadServerResponse(); break;
		case EXECUTE: serverResponse = new ExecuteActionServerResponse(); break;
		case COMPARE: serverResponse = new CompareServerResponse(); break;
		default: throw new IllegalArgumentException("Unknown action: " + finalAction);
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
				String url = connectionParameters.getUrl() + REST + "/rpc/compare?readOptions=raw&" + compareOptions + "&" + ignoreItems;
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
				String url = connectionParameters.getUrl() + REST + "/rpc/executeScript";
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
	public SearchObjectsServerResponse downloadObjects(ObjectTypes type, int limit, ConnectionParameters connectionParameters) {
		String query = "<query><paging><orderBy>name</orderBy><maxSize>"+limit+"</maxSize></paging></query>";
		return executeQuery(type, query, false, connectionParameters);
	}
	
	private SearchObjectsServerResponse executeQuery(ObjectTypes type, String query, boolean shortData, ConnectionParameters connectionParameters) {

		SearchObjectsServerResponse resp = new SearchObjectsServerResponse();
		
		if (query == null) {
			resp.setStatusCode(200);
			resp.setReasonPhrase("No query, no objects");
			return resp;
		}

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
					ObjectTypes realType = determineObjectType(objectElement);
					if (realType != null) {
						fixObjectName(objectElement, realType);
					}
					DOMUtil.fixNamespaceDeclarations(objectElement);
					String xml = DOMUtil.serializeDOMToString(objectElement);
					Element nameElement = DOMUtil.getChildElement(objectElement, new QName(Constants.COMMON_NS, "name"));
					List<String> subTypeElementsValues = new ArrayList<>();
					if (realType != null && realType.getSubTypeElement() != null) {
						for (Element e : DOMUtil.getChildElements(objectElement, new QName(Constants.COMMON_NS, realType.getSubTypeElement()))) {
							subTypeElementsValues.add(e.getTextContent());							
						}
					}
					String displayName;
					if (realType != null && realType.getDisplayNameElement() != null) {
						Element dn = DOMUtil.getChildElement(objectElement, new QName(Constants.COMMON_NS, realType.getDisplayNameElement()));
						displayName = dn != null ? dn.getTextContent() : null;
					} else {
						displayName = null;
					}
					String oid = DOMUtil.getAttribute(objectElement, new QName("oid"));
					ServerObject object = new ServerObject(oid, 
							nameElement != null ? nameElement.getTextContent() : null, 
							realType != null ? realType : type,
							subTypeElementsValues,
							displayName,
							xml);
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
	
	public static ObjectTypes determineObjectType(Element objectElement) {
		QName xsitype = DOMUtil.resolveXsiType(objectElement);
		if (xsitype == null) {
			return null;
		}
		return ObjectTypes.findByXsiType(xsitype.getLocalPart());
	}

	public static void fixObjectName(Element objectElement, ObjectTypes type) {
		if (type == null) {
			return;
		}
		objectElement.getOwnerDocument().renameNode(objectElement, Constants.COMMON_NS, type.getElementName());
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
				return downloadObject(oid, connectionParameters);
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
			
			return executeQuery(type, query, false, connectionParameters);

		} catch (Throwable t) {
			return new ServerResponse(t);
		}
	}

	@Override
	public SearchObjectsServerResponse downloadObject(String oid, ConnectionParameters connectionParameters) {
		return executeQuery(ObjectTypes.OBJECT, "<query><filter><inOid><value>"+oid+"</value></inOid></filter></query>", false, connectionParameters);
	}
	
	

	@Override
	public SearchObjectsServerResponse downloadObjects(List<String> oids, ConnectionParameters connectionParameters) {
		String query = oidsQuery(oids, null, null);
		return executeQuery(ObjectTypes.OBJECT, query, false, connectionParameters);
	}

	@Override
	public SearchObjectsServerResponse listObjects(Collection<ObjectTypes> types, String query, QueryInterpretation interpretation, int limit, int offset, ConnectionParameters connectionParameters) {
		String realQuery = createQuery(types, query, interpretation, limit, offset);
		ObjectTypes realType = ObjectTypes.OBJECT;
		if (!CollectionUtils.isEmpty(types)) {
			if (types.size() == 1) {
				realType = types.iterator().next();
			} else if (interpretation == QueryInterpretation.XML_QUERY) {
				throw new IllegalArgumentException("XML Query is not compatible with more than one type");
			}
		}

		System.out.println("Query: " + realQuery);
		return executeQuery(realType, realQuery, true, connectionParameters);
	}

	@Override
	public String createQuery(Collection<ObjectTypes> types, String query, QueryInterpretation interpretation, Integer limit, Integer offset) {
		if (query == null) {
			query = "";
		}
		String realQuery;
		switch (interpretation) {
		case XML_QUERY:
			realQuery = query;
			break;
		case OIDS:
			realQuery = oidsQuery(getLines(query), limit, offset);
			break;
		case NAMES:
			realQuery = namesQuery(types, getLines(query), limit, offset);
			break;
		default:
			realQuery = namesOrOidsQuery(types, getLines(query), limit, offset);
		}
		return realQuery;
	}
	
	private String namesOrOidsQuery(Collection<ObjectTypes> types, List<String> lines, int limit, int offset) {
		return namesOidsQueryInternal(types, lines, lines, limit, offset);
	}

	private String namesQuery(Collection<ObjectTypes> types, List<String> names, int limit, int offset) {
		return namesOidsQueryInternal(types, names, Collections.emptyList(), limit, offset);
	}
	
	private String namesOidsQueryInternal(Collection<ObjectTypes> types, List<String> names, List<String> oids, Integer limit, Integer offset) {
		Document doc = DOMUtil.getDocument(Constants.Q_QUERY);
		Element query = doc.getDocumentElement();
		
		boolean typesClauseRequired = !CollectionUtils.isEmpty(types) && types.size() > 1;
		boolean dataClauseRequired = !names.isEmpty() || !oids.isEmpty();
		
		if (typesClauseRequired || dataClauseRequired) {
			Element filter = DOMUtil.createSubElement(query, Constants.Q_FILTER);

			Element dataClauseParent;
			if (typesClauseRequired) {
				Element and = DOMUtil.createSubElement(filter, Constants.Q_AND);
				Element or1 = DOMUtil.createSubElement(and, Constants.Q_OR);
				for (ObjectTypes type : types) {
					Element type1 = DOMUtil.createSubElement(or1, Constants.Q_TYPE);
					Element type2 = DOMUtil.createSubElement(type1, Constants.Q_TYPE);
					DOMUtil.setQNameValue(type2, new QName(Constants.COMMON_NS, type.getTypeName()));
				}
				dataClauseParent = and;
			} else {
				dataClauseParent = filter;
			}

			if (dataClauseRequired) {
				Element or = DOMUtil.createSubElement(dataClauseParent, Constants.Q_OR);
				for (String name : names) {
					Element substring = DOMUtil.createSubElement(or, Constants.Q_SUBSTRING);
					DOMUtil.createSubElement(substring, Constants.Q_MATCHING).setTextContent("polyStringNorm");
					DOMUtil.createSubElement(substring, Constants.Q_PATH).setTextContent("name");			
					DOMUtil.createSubElement(substring, Constants.Q_VALUE).setTextContent(name);
				}
				if (!oids.isEmpty()) {
					Element inOid = DOMUtil.createSubElement(or, Constants.Q_IN_OID);	
					for (String oid : oids) {
						DOMUtil.createSubElement(inOid, Constants.Q_VALUE).setTextContent(oid);
					}
				}
			}
		}
		if (offset != null || limit != null) {
			Element paging = DOMUtil.createSubElement(query, Constants.Q_PAGING);
			DOMUtil.createSubElement(paging, Constants.Q_ORDER_BY).setTextContent("name");
			if (offset != null) {
				DOMUtil.createSubElement(paging, Constants.Q_OFFSET).setTextContent(String.valueOf(offset));
			}
			if (limit != null) {
				DOMUtil.createSubElement(paging, Constants.Q_MAX_SIZE).setTextContent(String.valueOf(limit));
			}
		}
		return DOMUtil.serializeDOMToString(doc);	
	}

	private String oidsQuery(List<String> oids, Integer limit, Integer offset) {
		return namesOidsQueryInternal(null, Collections.emptyList(), oids, limit, offset);
	}

	private static List<String> getLines(String text) {
		List<String> rv = new ArrayList<>();
		StringReader sr = new StringReader(text);
		try {
			for (String line : IOUtils.readLines(sr)) {
				if (StringUtils.isNotBlank(line)) {
					rv.add(line);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		IOUtils.closeQuietly(sr);
		return rv;
	}
	
}
