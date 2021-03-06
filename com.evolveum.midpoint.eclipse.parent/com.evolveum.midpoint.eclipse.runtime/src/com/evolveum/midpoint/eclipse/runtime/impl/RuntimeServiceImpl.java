package com.evolveum.midpoint.eclipse.runtime.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.SSLContext;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
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
			{
				String url = parameters.getUrl() + REST + "/nodes/current";
				HttpGet request = new HttpGet(url);
				HttpResponse response = client.execute(request);
			
				StatusLine statusLine = response.getStatusLine();
				if (isSuccess(statusLine)) {
					TestConnectionResponse tcr = new TestConnectionResponse(true, null, null);
					if (response.getEntity() != null) {
						Element root = DOMUtil.parse(response.getEntity().getContent()).getDocumentElement();
						Element build = DOMUtil.getChildElement(root, "build");
						if (build != null) {
							Element version = DOMUtil.getChildElement(build, "version");
							if (version != null) {
								tcr.setVersion(version.getTextContent());
							}
							Element revision = DOMUtil.getChildElement(build, "revision");
							if (revision != null) {
								tcr.setRevision(revision.getTextContent());
							}
						}
					}
					return tcr;
				}
			}
			
			// old way
			{
				String url = parameters.getUrl() + REST + "/users/search";
				HttpPost request = new HttpPost(url);

				HttpEntity body = new StringEntity("<query><filter><inOid><value>none</value></inOid></filter></query>", createXmlContentType());
				request.setEntity(body);
		
				HttpResponse response = client.execute(request);
				StatusLine statusLine = response.getStatusLine();
				TestConnectionResponse tcr;
				if (isSuccess(statusLine)) {
					tcr = new TestConnectionResponse(true, null, null);
				} else {
					tcr = new TestConnectionResponse(false, "Server response: " + statusLine.getStatusCode() + ": " + statusLine.getReasonPhrase(), null);
				}
				tcr.setVersion("(unknown)");
				tcr.setRevision("(unknown)");
				return tcr;
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
		
		HttpClientBuilder clientBuilder = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider);
		
		if (parameters.isIgnoreSslIssues()) {
			SSLContext sslContext;
			try {
				sslContext = new SSLContextBuilder()
			        .loadTrustMaterial(null, new org.apache.http.ssl.TrustStrategy() {
			            @Override
			            public boolean isTrusted(X509Certificate[] x509CertChain, String authType) throws CertificateException {
			                return true;
			            }
			        })
			        .build();
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
			clientBuilder.setSSLContext(sslContext);
			clientBuilder.setConnectionManager(
                new PoolingHttpClientConnectionManager(
                        RegistryBuilder.<ConnectionSocketFactory>create()
                                .register("http", PlainConnectionSocketFactory.INSTANCE)
                                .register("https", new SSLConnectionSocketFactory(sslContext,
                                        NoopHostnameVerifier.INSTANCE))
                                .build()
                		));
		}
		
		return clientBuilder.build();
	}

	private boolean isSuccess(StatusLine statusLine) {
		int code = statusLine.getStatusCode();
		return code >= 200 && code < 300;
	}

	@Override
	public ServerResponse executeServerRequest(ServerRequest request, ConnectionParameters connectionParameters) {
		
		String oid;
		ObjectTypes type;
		Element rootElement;
		
		try {
			Document document = DOMUtil.parseDocument(request.getData());
			rootElement = document.getDocumentElement();
			String nodeName = rootElement.getNodeName();
			String localName = rootElement.getLocalName();
			String uri = rootElement.getNamespaceURI();
			oid = rootElement.getAttribute("oid");
			type = ObjectTypes.findByElementName(localName);
			System.out.println("Node name: " + nodeName + ", localName: " + localName + ", uri: " + uri + ", oid: " + oid +", type: " + type);
			if (type == ObjectTypes.OBJECT) {
				ObjectTypes realType = determineObjectType(rootElement);
				if (realType != null) {
					System.out.println("Found real type, using it: " + realType);
					type = realType;
				}
			}
			if (request.getAction() == ServerAction.UPLOAD && type == null) {
				throw new IllegalStateException("Unknown element " + localName);		// should be already checked
			}
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
				String url = connectionParameters.getUrl() + REST + "/" + type.getRestType();
				
				List<String> options = new ArrayList<>();
				if (type != ObjectTypes.TASK && type != ObjectTypes.SYSTEM_CONFIGURATION) {
					options.add("options=raw");
				}
				options.add("options=isImport");
//				if (type == ObjectTypes.RESOURCE && request.isValidate()) {
//					options.add("options=validate");
//				}
				
				String suffix;
				if (!options.isEmpty()) {
					suffix = "?" + StringUtils.join(options, "&");
				} else {
					suffix = "";
				}
				
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
			HttpEntity body = new ByteArrayEntity(request.getData().getBytes("utf-8"), createXmlContentType());				// TODO charset if defined in XML?
			httpRequest.setEntity(body);

			HttpUriRequest uriRequest = (HttpUriRequest) httpRequest;
			System.out.println("Invoking " + uriRequest.getMethod() + " on " + uriRequest.getURI());
			if (finalAction == ServerAction.EXECUTE) {
				System.out.println("Body:\n" + request.getData());
			}
			HttpResponse response = client.execute(uriRequest);
			
			StatusLine statusLine = response.getStatusLine();
			System.out.println("Server response status line: " + statusLine);

			String resultStatus = getHeader(response, "OperationResultStatus");
			String resultMessage = getHeader(response, "OperationResultMessage");
			System.out.println("Operation result: " + resultStatus + " (" + resultMessage + ")");

			serverResponse.setOperationResultStatusString(resultStatus);	// these will be overridden for ExecuteActionServerResponse later
			serverResponse.setOperationResultMessage(resultMessage);

			if (response.getEntity() != null) {
				String responseBody = getResponseBody(response);
				serverResponse.setRawResponseBody(responseBody);
				System.out.println("Server response (raw):\n" + responseBody + "\n---------------------------------");
				if (serverResponse instanceof ExecuteActionServerResponse) {
					Header contentType = response.getEntity().getContentType();
					System.out.println("Content type of the response: " + contentType);
					if (contentType != null && contentType.getValue().startsWith("application/xml") && isSuccess(statusLine)) {
						((ExecuteActionServerResponse) serverResponse).parseXmlResponse(responseBody);
					}
				} else if (serverResponse instanceof CompareServerResponse && isSuccess(statusLine)) {
					((CompareServerResponse) serverResponse).parseXmlResponse(responseBody, (CompareServerRequest) request);
				}
			} else {
				System.out.println("(no entity in response)");
			}
			
			serverResponse.setStatusCode(statusLine.getStatusCode());
			serverResponse.setReasonPhrase(statusLine.getReasonPhrase());
			
			if (serverResponse instanceof UploadServerResponse && statusLine.getStatusCode() == 409) {
				UploadServerResponse usr = (UploadServerResponse) serverResponse;
				Element objectNameE = DOMUtil.getChildElement(rootElement, "name");
				String objectName = objectNameE != null ? objectNameE.getTextContent() : null;
				System.out.println("Conflict detected; downloading conflicting object(s) for name '" + objectName + "' in " + type);
				if (StringUtils.isEmpty(objectName)) {
					usr.setErrorDescription("Conflict detected, but couldn't search for conflicting object as the name is not known.");
				} else {
					SearchObjectsServerResponse objects = listObjects(Collections.singletonList(type), objectName, QueryInterpretation.NAMES, 1000, 0, connectionParameters);
					System.out.println("Search success: " + objects.isSuccess() + ", count: " + objects.getServerObjects().size());
					if (!objects.isSuccess()) {
						usr.setErrorDescription("Conflict detected, but couldn't search for conflicting objects: " + objects.getErrorDescription());
					} else {
						StringBuilder sb = new StringBuilder();
						boolean first = true;
						for (ServerObject so : objects.getServerObjects()) {
							if (!first) {
								sb.append(", ");
							} else {
								first = false;
							}
							sb.append(so.getName() + " (oid " + so.getOid() + ")");
						}
						if (sb.length() == 0) {
							usr.setErrorDescription("Conflict detected but no conflicting objects with the name of '" + objectName + "' could be found.");
						} else {
							usr.setErrorDescription("Conflict detected with: " + sb.toString());
						}
					}
				}
			}
			
		} catch (Throwable t) {
			serverResponse.setException(t);
		}

		return serverResponse;

	}

	public String getResponseBody(HttpResponse response) throws IOException {
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
		return sb.toString();
	}

	private String getHeader(HttpResponse response, String name) {
		Header[] headers = response.getHeaders(name);
		if (headers.length == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();;
		boolean first = true;
		for (Header header : headers) {
			if (first) {
				first = false;
			} else {
				sb.append("; ");
			}
			sb.append(header.getValue());
		}
		return sb.toString();
	}

	public ContentType createXmlContentType() {
		//return ContentType.create("application/xml", Consts.UTF_8);
		return ContentType.create("application/xml");
	}

	@Override
	public SearchObjectsServerResponse downloadObjects(ObjectTypes type, int limit, ConnectionParameters connectionParameters) {
		String realQuery = createQuery(Collections.singletonList(type), "", QueryInterpretation.NAMES, limit, 0);
		ObjectTypes realType = type != ObjectTypes.SHADOW ? type : ObjectTypes.OBJECT;
		System.out.println("Query: " + realQuery);
		return executeQuery(realType, realQuery, false, connectionParameters);
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

			String url = connectionParameters.getUrl() + REST + "/"+type.getRestType()+"/search?resolveNames=archetypeRef" + (shortData ? "" : "&include=row&include=jpegPhoto");
			HttpPost request = new HttpPost(url);

			HttpEntity body = new ByteArrayEntity(query.getBytes("utf-8"), createXmlContentType());
			request.setEntity(body);

			System.out.println("Requesting objects from " + url);
			HttpResponse response = client.execute(request);

			StatusLine statusLine = response.getStatusLine();
			System.out.println("Server response status line: " + statusLine);

			resp.setStatusCode(statusLine.getStatusCode());
			resp.setReasonPhrase(statusLine.getReasonPhrase());

			if (!isSuccess(statusLine)) {
				if (response.getEntity() != null) {
					String responseBody = getResponseBody(response);
					System.out.println("Server response (raw):\n" + responseBody + "\n---------------------------------");
				} else {
					System.out.println("(no entity in response)");
				}
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
					for (Element e : DOMUtil.getChildElements(objectElement, new QName(Constants.COMMON_NS, "archetypeRef"))) {
						Element targetNameElement = DOMUtil.getChildElement(e, new QName(Constants.COMMON_NS, "targetName"));
						String archetypeName = targetNameElement != null ? targetNameElement.getTextContent() : null; 
						if (archetypeName != null && !archetypeName.isEmpty()) {
							subTypeElementsValues.add(archetypeName);
						} else {
							String archetypeOid = e.getAttribute("oid");
							subTypeElementsValues.add(archetypeOid);
						}
					}
					for (Element e : DOMUtil.getChildElements(objectElement, new QName(Constants.COMMON_NS, "subtype"))) {
						subTypeElementsValues.add(e.getTextContent());							
					}
					if (realType != null && realType.getSubTypeElement() != null) {
						for (Element e : DOMUtil.getChildElements(objectElement, new QName(Constants.COMMON_NS, realType.getSubTypeElement()))) {
							subTypeElementsValues.add(e.getTextContent());							
						}
					} else if (realType == ObjectTypes.SHADOW) {
						Element resourceRefE = DOMUtil.getChildElement(objectElement, "resourceRef");
						Element kindE = DOMUtil.getChildElement(objectElement, "kind");
						Element intentE = DOMUtil.getChildElement(objectElement, "intent");
						String resourceOid = resourceRefE != null ? resourceRefE.getAttribute("oid") : null;
						if (resourceOid == null) {
							resourceOid = "";
						} else {
							resourceOid = "..." + StringUtils.substring(resourceOid, -8);
						}
						String kind = kindE != null ? kindE.getTextContent() : "";
						String intent = intentE != null ? intentE.getTextContent() : "";
						subTypeElementsValues.add(resourceOid + "/" + kind + "/" + intent);
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
			
			return executeQuery(type, query, false, connectionParameters);			// TODO for shadows...

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
				ObjectTypes firstType = types.iterator().next();
				if (firstType != ObjectTypes.SHADOW) {
					realType = firstType;
				}
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
		
		boolean typesClauseRequired = !CollectionUtils.isEmpty(types) && (types.size() > 1 || types.size() == 1 && types.iterator().next() == ObjectTypes.SHADOW);
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
					DOMUtil.setQNameValue(type2, new QName(Constants.COMMON_NS, type.getTypeName(), "c"));
				}
				dataClauseParent = and;
			} else {
				dataClauseParent = filter;
			}

			if (dataClauseRequired) {
				Element or = DOMUtil.createSubElement(dataClauseParent, Constants.Q_OR);
				if (!oids.isEmpty()) {
					Element inOid = DOMUtil.createSubElement(or, Constants.Q_IN_OID);	
					for (String oid : oids) {
						DOMUtil.createSubElement(inOid, Constants.Q_VALUE).setTextContent(oid);
					}
				}
				for (String name : names) {
					Element substring = DOMUtil.createSubElement(or, Constants.Q_SUBSTRING);
					DOMUtil.createSubElement(substring, Constants.Q_MATCHING).setTextContent("polyStringNorm");
					DOMUtil.createSubElement(substring, Constants.Q_PATH).setTextContent("name");			
					DOMUtil.createSubElement(substring, Constants.Q_VALUE).setTextContent(name);
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

	public static List<String> getLines(String text) {
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
