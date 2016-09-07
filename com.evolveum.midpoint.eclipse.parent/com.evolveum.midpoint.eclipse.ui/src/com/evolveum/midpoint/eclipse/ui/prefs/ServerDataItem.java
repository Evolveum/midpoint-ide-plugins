package com.evolveum.midpoint.eclipse.ui.prefs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.ConnectionParameters;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.util.DOMUtil;

public class ServerDataItem implements DataItem {
	
	private static final String E_SELECTED = "selected";
	private static final String E_LOG_FILE = "logFile";
	private static final String E_PROPERTIES_FILE = "propertiesFile";
	private static final String E_PASSWORD = "password";
	private static final String E_LOGIN = "login";
	private static final String E_URL = "url";
	private static final String E_NAME = "name";

	private boolean selected;
	private String name;
	private String url;
	private String login;
	private String password;
	private String propertiesFile;
	private String logFile;
	
	public ServerDataItem(boolean selected, String name, String url, String login, String password, String propertiesFile,
			String logFile) {
		this.selected = selected;
		this.name = name;
		this.url = url;
		this.login = login;
		this.password = password;
		this.propertiesFile = propertiesFile;
		this.logFile = logFile;
	}

	public ServerDataItem() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String[] getColumnValues() {
		return new String[] { name, url, login, propertiesFile, logFile }; 
	}

	@Override
	public DataItem clone() {
		ServerDataItem clone = new ServerDataItem(selected, name, url, login, password, propertiesFile, logFile);
		return clone;
	}
	
	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPropertiesFile() {
		return propertiesFile;
	}

	public void setPropertiesFile(String propertiesFile) {
		this.propertiesFile = propertiesFile;
	}

	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public static ServerDataItem createDefault() {
		return new ServerDataItem(false, "", "http://localhost:8080/midpoint", "administrator", "5ecr3t", "", "");
	}

	public static String createDefaultXml() {
		ServerDataItem theOne = createDefault();
		theOne.setSelected(true);
		theOne.setName("Default");
		return toXml(Collections.singletonList(theOne));
	}

	public static String toXml(List<ServerDataItem> items) {
		Document doc = DOMUtil.getDocument(new QName("servers"));
		Element servers = doc.getDocumentElement();
		for (ServerDataItem item : items) {
			Element server = DOMUtil.createSubElement(servers, new QName("server"));
			item.toXml(server);
		}
		String xml = DOMUtil.printDom(servers, false, true).toString();
		System.out.println("XML = " + xml);
		return xml;
	}

	private void toXml(Element server) {
		DOMUtil.createSubElement(server, new QName(E_NAME)).setTextContent(name);
		DOMUtil.createSubElement(server, new QName(E_URL)).setTextContent(url);
		DOMUtil.createSubElement(server, new QName(E_LOGIN)).setTextContent(login);
		DOMUtil.createSubElement(server, new QName(E_PASSWORD)).setTextContent(password);
		DOMUtil.createSubElement(server, new QName(E_PROPERTIES_FILE)).setTextContent(propertiesFile);
		DOMUtil.createSubElement(server, new QName(E_LOG_FILE)).setTextContent(logFile);
		DOMUtil.createSubElement(server, new QName(E_SELECTED)).setTextContent(String.valueOf(selected));
	}

	public static List<ServerDataItem> fromXml(String string) {
		System.out.println("fromXml called with " + string);
		List<ServerDataItem> rv = new ArrayList<>();
		if (StringUtils.isBlank(string)) {
			return rv;
		}
		try {
			Document doc = DOMUtil.parseDocument(string);
			Element serversElement = doc.getDocumentElement();
			List<Element> serverElements = DOMUtil.listChildElements(serversElement);		// TODO only 'server' elements
			for (Element serverElement : serverElements) {
				rv.add(ServerDataItem.fromXml(serverElement));
			}
		} catch (Throwable t) {
			Console.logError("Couldn't parse servers list '" + string + "'", t);
		}
		System.out.println("fromXml returning a list of " + rv.size());
		return rv;
	}

	private static ServerDataItem fromXml(Element e) {
		ServerDataItem rv = new ServerDataItem(
				getBoolean(e, E_SELECTED),
				get(e, E_NAME),
				get(e, E_URL),
				get(e, E_LOGIN),
				get(e, E_PASSWORD),
				get(e, E_PROPERTIES_FILE),
				get(e, E_LOG_FILE));
		
		return rv;
	}

	private static String get(Element e, String elementName) {
		Element sub = DOMUtil.getChildElement(e, elementName);
		return sub != null ? sub.getTextContent() : null;
	}
	
	private static boolean getBoolean(Element e, String elementName) {
		Element sub = DOMUtil.getChildElement(e, elementName);
		return sub != null ? Boolean.valueOf(sub.getTextContent()) : false;
	}

	public ConnectionParameters getConnectionParameters() {
		return new ConnectionParameters(name, url, login, password);
	}

	public String getDisplayName() {
		return StringUtils.isNotBlank(name) ? name : url;
	}
	
}