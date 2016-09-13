package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.req.CompareServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerAction;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler.RequestedAction;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Expander;
import com.evolveum.midpoint.eclipse.ui.util.Util;
import com.evolveum.midpoint.util.DOMUtil;

public class ServerRequestItem {
	
	private ServerAction serverAction;
	private SourceObject source;
	private int predefinedActionNumber;			// if this was invoked as "predefined action" (e.g. to open corresponding file)
	
	public ServerRequestItem(ServerAction action, SourceObject source, int actionNumber) {
		this.serverAction = action;
		this.source = source;
		this.predefinedActionNumber = actionNumber;
	}
	
	public ServerRequestItem(ServerAction action, SourceObject source) {
		this.serverAction = action;
		this.source = source;
	}


	public ServerAction getServerAction() {
		return serverAction;
	}
	public void setServerAction(ServerAction action) {
		this.serverAction = action;
	}
	public String getDisplayName() {
		return source.getDisplayName();
	}
	public String getExpandedContent() {
		String content = source.getContent();
		return Expander.expand(content, PluginPreferences.getSelectedServer());
	}
	public IPath getSourcePath() {
		return source.getResourcePath();
	}
	public int getPredefinedActionNumber() {
		return predefinedActionNumber;
	}
	public void setPredefinedActionNumber(int predefinedActionNumber) {
		this.predefinedActionNumber = predefinedActionNumber;
	}

	@Override
	public String toString() {
		return "ServerRequestItem [action=" + serverAction + ", source=" + source + ", predefinedActionNumber="
				+ predefinedActionNumber + "]";
	}

	public ServerRequest createServerRequest() {
		String expandedContent = getExpandedContent();
		if (expandedContent == null) {
			return null;
		}
		if (serverAction == ServerAction.COMPARE) {
			CompareServerRequest csr = new CompareServerRequest(serverAction, expandedContent);
			csr.setShowLocalToRemote(PluginPreferences.getCompareShowLocalToRemote());
			csr.setShowRemoteToLocal(PluginPreferences.getCompareShowRemoteToLocal());
			csr.setShowLocal(PluginPreferences.getCompareShowLocalNormalized());
			csr.setShowRemote(PluginPreferences.getCompareShowRemote());
			csr.setIgnoreItems(PluginPreferences.getCompareIgnoreItems());
			return csr;
		} else {
			return new ServerRequest(serverAction, expandedContent);
		}	
	}

	public static List<ServerRequestItem> fromPhysicalActionFile(String fileName, int actionNumber) {
		List<ServerRequestItem> rv = new ArrayList<>();
		List<SourceObject> sourceObjects = parsePhysicalFile(fileName, true);
		for (SourceObject so : sourceObjects) {
			rv.add(new ServerRequestItem(ServerAction.EXECUTE, so, actionNumber));
		}
		return rv;
	}

	private static List<SourceObject> parsePhysicalFile(String fileName, boolean executeOnly) {
		try {
			IFile logicalFile = Util.physicalToLogicalFile(fileName);
			IPath logicalPath = logicalFile != null ? logicalFile.getFullPath() : null;
			Document doc = DOMUtil.parseFile(fileName);
			return parseDocument(doc, executeOnly, logicalPath, logicalPath != null ? logicalPath.toPortableString() : fileName);
		} catch (RuntimeException e) {
			Console.logError("Couldn't parse file " + fileName, e);
			return new ArrayList<>();
		}
	}

	public static List<SourceObject> parseWorkspaceFile(IFile file, RequestedAction requestedAction) {
		try {
			Document doc = parseWorkspaceFileToDOM(file);
			List<SourceObject> objects = parseDocument(doc, requestedAction == RequestedAction.EXECUTE_ACTION, file.getFullPath(), file.getFullPath().toPortableString());
			if (requestedAction == RequestedAction.COMPARE) {
				if (objects.size() != 1 || !objects.get(0).isUploadable()) {
					// TODO consider adding isRoot
					Console.logWarning("File " + file + " cannot be compared because it doesn't contain exactly one midPoint object.");
					return new ArrayList<>(); 
				}
			}
			return objects;
		} catch (RuntimeException e) {
			Console.logError("Couldn't parse file " + file, e);
			return new ArrayList<>();
		}
	}

	public static List<SourceObject> parseTextFragment(String textFragment, IPath path, RequestedAction requestedAction) {
		try {
			Document doc = DOMUtil.parseDocument(textFragment);
			String displayName = path != null ? "text fragment from " + path.toPortableString() : "text fragment";
			List<SourceObject> objects = parseDocument(doc, requestedAction == RequestedAction.EXECUTE_ACTION, path, displayName);
			if (requestedAction == RequestedAction.COMPARE) {
				if (objects.size() != 1 || !objects.get(0).isUploadable()) {
					// TODO consider adding isRoot
					Console.logWarning(displayName + " cannot be compared because it doesn't contain exactly one midPoint object.");
					return new ArrayList<>(); 
				}
			}
			return objects;
		} catch (RuntimeException e) {
			Console.logError("Couldn't parse text fragment from file " + path, e);
			return new ArrayList<>();
		}
	}

	public static Document parseWorkspaceFileToDOM(IFile file) {
		InputStream is = null;
		try {
			is = file.getContents();
			return DOMUtil.parse(is);
		} catch (CoreException | IOException e) {
			Util.processUnexpectedException(e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}


	private static List<SourceObject> parseDocument(Document doc, boolean executeOnly, IPath logicalPath, String displayName) {
		List<SourceObject> rv = new ArrayList<>();
		Element root = doc.getDocumentElement();
		String localName = root.getLocalName();
		if ("actions".equals(localName) || "objects".equals(localName)) {
			for (Element child : DOMUtil.listChildElements(root)) {
				DOMUtil.fixNamespaceDeclarations(child);
				SourceObject o = parseElement(child, executeOnly);
				if (o != null) {
					o.setRoot(false);
					rv.add(o);
				}
			}
		} else {
			SourceObject o = parseElement(root, executeOnly);
			if (o != null) {
				o.setRoot(true);
				rv.add(o);
			}
		}
		
		for (int i = 0; i < rv.size(); i++) {
			SourceObject o = rv.get(i);
			o.setObjectIndex(i);
			o.setResourcePath(logicalPath);
			String name;
			if (displayName != null) {
				name = displayName;
			} else if (logicalPath != null) {
				name = logicalPath.toPortableString();
			} else {
				name = "(unknown source)";
			}
			if (rv.size() > 1) {
				name += " (object " + (i+1) + " of " + rv.size() + ")";
			}
			o.setDisplayName(name);
		}
		
		return rv;
	}

	private static SourceObject parseElement(Element element, boolean executeOnly) {
		String localName = element.getLocalName();
		boolean executable = Constants.SCRIPTING_ACTIONS.contains(localName);
		boolean uploadable = ObjectTypes.getRestTypeForElementName(localName) != null;
		
		if (executeOnly && !executable) {
			Console.logWarning("Object rooted at " + element.getNodeName() + " is not executable, skipping it.");
			return null;
		} else if (!executable && !uploadable) {
			Console.logWarning("Object rooted at " + element.getNodeName() + " is not executable nor uploadable, skipping it.");
			return null;
		}
		return new SourceObject(DOMUtil.serializeDOMToString(element), uploadable, executable);
	}
	
	
}