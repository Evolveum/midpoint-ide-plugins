package com.evolveum.midpoint.eclipse.ui.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.w3c.dom.Element;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.Constants;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerAction;
import com.evolveum.midpoint.eclipse.runtime.api.req.ServerRequest;
import com.evolveum.midpoint.eclipse.runtime.api.resp.SearchObjectsServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.UploadServerResponse;
import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.menus.MenuUtil;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Util;
import com.evolveum.midpoint.util.DOMUtil;

public class SetLogLevelHandler extends AbstractHandler {
	
	private static Map<String,String> packages;
	static {
		packages = new HashMap<>();
		packages.put(PluginConstants.VALUE_MODEL, "com.evolveum.midpoint.model");
		packages.put(PluginConstants.VALUE_PROVISIONING, "com.evolveum.midpoint.provisioning");
		packages.put(PluginConstants.VALUE_REPOSITORY, "com.evolveum.midpoint.repo");		
		packages.put(PluginConstants.VALUE_GUI, "com.evolveum.midpoint.web");
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		String component = event.getParameter(PluginConstants.PARAM_COMPONENT);
		String level = event.getParameter(PluginConstants.PARAM_LEVEL);
		if (StringUtils.isBlank(component) || StringUtils.isBlank(level)) {
			return null;
		}
		ServerInfo server = PluginPreferences.getSelectedServer();
		if (server == null) {
			return null;
		}
		
		Job job = new Job("Setting log level") {
			protected IStatus run(IProgressMonitor monitor) {
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				SearchObjectsServerResponse downloadResp = runtime.downloadObject(Constants.SYSCONFIG_OID, server.getConnectionParameters());
				if (!downloadResp.isSuccess()) {
					Util.showAndLogError("Download problem", "Couldn't download system configuration: " + downloadResp.getErrorDescription(), downloadResp.getException());
					return Status.OK_STATUS;
				}
				if (downloadResp.getServerObjects().isEmpty()) {
					Util.showAndLogError("Download problem", "The system configuration is not readable");
					return Status.OK_STATUS;
				}
				ServerObject config = downloadResp.getServerObjects().get(0);
				
				String newconfig;
				try {
					Element root = DOMUtil.parseDocument(config.getXml()).getDocumentElement();
					Element logging = DOMUtil.getChildElement(root, "logging");
					if (logging == null) {
						Util.showAndLogError("Couldn't set logging level", "There is no logging configuration in system configuration object.");
						return Status.OK_STATUS;
					}
					if (PluginConstants.VALUE_ALL.equals(component)) {
						removeExisting(logging, PluginConstants.VALUE_MODEL);
						removeExisting(logging, PluginConstants.VALUE_PROVISIONING);
						removeExisting(logging, PluginConstants.VALUE_REPOSITORY);
						removeExisting(logging, PluginConstants.VALUE_GUI);
					} else {
						removeExisting(logging, component);
					}
					
					switch (component) {
					case PluginConstants.VALUE_MODEL:
						createModelLoggers(logging, level);
						break;
					case PluginConstants.VALUE_ALL:
						createLogger(logging, packages.get(PluginConstants.VALUE_MODEL), level.toUpperCase());
						createLogger(logging, packages.get(PluginConstants.VALUE_PROVISIONING), level.toUpperCase());
						createLogger(logging, packages.get(PluginConstants.VALUE_REPOSITORY), level.toUpperCase());
						createLogger(logging, packages.get(PluginConstants.VALUE_GUI), level.toUpperCase());
						break;
					default:
						createLogger(logging, packages.get(component), level.toUpperCase());
						break;
					}
					newconfig = DOMUtil.serializeDOMToString(root);
					
				} catch (RuntimeException e) {
					Util.showAndLogError("Couldn't set logging level", "Unexpected exception while setting log level: " + e.getMessage(), e);
					return Status.OK_STATUS;
				}
				
				System.out.println("Uploading new system configuration:\n" + newconfig);
				ServerRequest uploadRequest = new ServerRequest(ServerAction.UPLOAD, newconfig);
				ServerResponse uploadResp = runtime.executeServerRequest(uploadRequest, server.getConnectionParameters());
				if (!uploadResp.isSuccess()) {
					Util.showAndLogError("Upload problem", "Couldn't upload changed system configuration: " + uploadResp.getErrorDescription(), uploadResp.getException());
					return Status.OK_STATUS;
				}
				if (!(uploadResp instanceof UploadServerResponse)) {
					Util.showAndLogError("Upload problem", "Couldn't upload changed system configuration: " + uploadResp.getClass());
					return Status.OK_STATUS;
				}
				Console.log("Logging for '" + component + "' was successfully set to '" + level + "'.");
				return Status.OK_STATUS;
			}

			public String removeExisting(Element logging, String component) {
				String componentPackage = packages.get(component);
				List<Element> loggers = DOMUtil.getChildElements(logging, new QName(Constants.COMMON_NS, "classLogger"));
				for (Element logger : loggers) {
					Element pkg = DOMUtil.getChildElement(logger, "package");
					if (pkg != null && pkg.getTextContent() != null && pkg.getTextContent().startsWith(componentPackage)) {
						logging.removeChild(logger);
					}
				}
				return componentPackage;
			}
		};
		job.schedule();
		return null;
	}

	protected void createModelLoggers(Element logging, String level) {
		switch (level) {
		case PluginConstants.VALUE_INFO:
		case PluginConstants.VALUE_DEBUG:
		case PluginConstants.VALUE_TRACE:
			createLogger(logging, packages.get(PluginConstants.VALUE_MODEL), level.toUpperCase());
			return;
			
		case PluginConstants.VALUE_LENS_TRACE:
			createLogger(logging, "com.evolveum.midpoint.model.impl.lens", "TRACE");
		case PluginConstants.VALUE_PROJECTOR_TRACE:
			createLogger(logging, "com.evolveum.midpoint.model.impl.lens.projector", "TRACE");
		case PluginConstants.VALUE_EXPRESSION_TRACE:
			createLogger(logging, "com.evolveum.midpoint.model.common.expression.Expression", "TRACE");
		case PluginConstants.VALUE_MAPPING_TRACE:
			createLogger(logging, "com.evolveum.midpoint.model.common.mapping.Mapping", "TRACE");
		case PluginConstants.VALUE_PROJECTOR_SUMMARY:
			createLogger(logging, "com.evolveum.midpoint.model.impl.lens.projector.Projector", "TRACE");
		case PluginConstants.VALUE_CLOCKWORK_SUMMARY:
			createLogger(logging, "com.evolveum.midpoint.model.impl.lens.Clockwork", "DEBUG");
			return;
		}
	}

	protected void createLogger(Element logging, String packageName, String loggingLevel) {
		Element e = DOMUtil.createSubElement(logging, new QName(Constants.COMMON_NS, "classLogger", "c"));
		DOMUtil.createSubElement(e, new QName(Constants.COMMON_NS, "level", "c")).setTextContent(loggingLevel);
		DOMUtil.createSubElement(e, new QName(Constants.COMMON_NS, "package", "c")).setTextContent(packageName);
	}


}
