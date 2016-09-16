package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.req.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.resp.SearchObjectsServerResponse;
import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;
import com.evolveum.midpoint.eclipse.ui.handlers.ResourceUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.DownloadPreferencePage;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Util;
import com.evolveum.midpoint.util.Holder;

public class DownloadHandler extends AbstractHandler {
	
	public static final String CMD_DOWNLOAD = "com.evolveum.midpoint.eclipse.ui.command.download";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IContainer selected = getSelectedDirectory(event);
		if (selected == null) {
			return null;
		}
		System.out.println("Selected directory: " + selected);
		
		Job job = new Job("Downloading from midPoint") {
			protected IStatus run(IProgressMonitor monitor) {
				
				List<ObjectTypes> typesToDownload = determineTypesToDownload();
				Console.log("Downloading object types: " + typesToDownload.stream().map(type -> type.getElementName()).collect(Collectors.toList()));
				int limit = PluginPreferences.getDownloadedObjectsLimit();
				
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				ConnectionParameters connectionParameters = PluginPreferences.getConnectionParameters();
				
				List<ServerObject> allObjects = new ArrayList<>();
				monitor.beginTask("Downloading", typesToDownload.size());
				for (ObjectTypes type : typesToDownload) {
					if (monitor.isCanceled()) {
						break;
					}
					monitor.subTask("Downloading " + type.getRestType());
					SearchObjectsServerResponse serverResponse = runtime.downloadObjects(type, limit, connectionParameters);
					
					if (!serverResponse.isSuccess()) {
						Console.logError("Couldn't download objects of type " + type + ": " + serverResponse.getErrorDescription(), serverResponse.getException());
					} else {
						allObjects.addAll(serverResponse.getServerObjects());
					}
				}
					
				writeFiles(allObjects, selected, monitor);
				return Status.OK_STATUS;
			}

		};
		job.schedule();
		
		return null;
	}
	
	public static void writeFiles(List<ServerObject> allObjects, IContainer root, IProgressMonitor monitor) {
		monitor.beginTask("Writing files", allObjects.size());
		boolean yesToAll = false, noToAll = false;
		int count = 0;
		try {
main:		for (ServerObject object : allObjects) {
				monitor.subTask("Writing " + object.getName());
				if (monitor.isCanceled()) {
					break;
				}
				IFile file = prepareOutputFileForCreation(object, root);
				IPath path = file.getFullPath();
				if (file.exists()) {
					String overwrite = PluginPreferences.getOverwriteWhenDownloading();
					if (noToAll || DownloadPreferencePage.VALUE_NEVER.equals(overwrite)) {
						Console.logMinor("File " + path + " already exists, skipping.");
						continue;
					}
					final Holder<Integer> responseHolder = new Holder<>();
					if (DownloadPreferencePage.VALUE_ASK.equals(overwrite) && !yesToAll) {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog dialog = new MessageDialog(
										null, "Confirm overwrite", null, "Are you sure to overwrite " + path + "?",
										MessageDialog.QUESTION,
										new String[] {"Yes", "No", "Yes to all", "No to all", "Cancel"},
										0);
								responseHolder.setValue(dialog.open());
							}
						});
						switch (responseHolder.getValue()) {
						case 2: yesToAll = true;		// Yes to all
						case 0: break;					// Yes
						case 3: noToAll = true;			// No to all
						case 1: 						// No
							Console.logMinor("File " + path + " already exists, skipping.");
							continue;
						case 4: break main;				// Cancel
						}
					}
					file.delete(true, monitor);
				} else {
					ResourceUtils.createParentFolders(file.getParent());
				}
				file.create(new ByteArrayInputStream(object.getXml().getBytes("utf-8")), true, monitor);
				Console.logMinor("File " + path + " was successfully created.");
				count++;
				monitor.worked(1);
			}
		} catch (Throwable t) {
			Util.processUnexpectedException(t);
		}
		Console.log("Downloaded " + count + " object(s)");
	}

	
	private static IFile prepareOutputFileForCreation(ServerObject object, IContainer selected) {
		IPath path = computeFilePath(object, selected);
		System.out.println("Path = " + path);
		if (path == null) {
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}


	private static IPath computeFilePath(ServerObject object, IContainer selected) {
		IPath root = ResourceUtils.determineRoot(selected.getFullPath(), PluginPreferences.getDownloadedFilesRootDirectory());
		String pattern = PluginPreferences.getDownloadedFileNamePattern();
		if (StringUtils.isBlank(pattern)) {
			return null;
		}
		
		String patternResolved = pattern
				.replace("$t", fixComponent(object.getType().getElementName()))
				.replace("$T", fixComponent(object.getType().getRestType()))
				.replace("$o", fixComponent(object.getOid()))
				.replace("$n", fixComponent(object.getName()))
				.replace("$s", fixComponent(PluginPreferences.getSelectedServerShortName()));
		
		System.out.println("pattern = " + pattern + ", resolvedPattern = " + patternResolved);
		IPath rv = root.append(new Path(patternResolved));
		System.out.println("Final result = " + rv);
		return rv;
	}
	
	public static String fixComponent(String s) {
		if (s == null) {
			return null;
		}
		return s
				.replace('<', '_')
				.replace('>', '_')
				.replace(':', '_')
				.replace('"', '_')
				.replace('\'', '_')
				.replace('/', '_')
				.replace('\\', '_')
				.replace('|', '_')
				.replace('?', '_')
				.replace('*', '_');
	}

	protected List<ObjectTypes> determineTypesToDownload() {
		List<ObjectTypes> rv = new ArrayList<>();
		List<String> include = PluginPreferences.getIncludeInDownload();
		List<String> exclude = PluginPreferences.getExcludeFromDownload();

		if (include.isEmpty()) {
			rv.addAll(ObjectTypes.getConcreteTypes());
		} else {
			rv.addAll(parseTypes(include));
		}
		if (exclude != null) {
			rv.removeAll(parseTypes(exclude));
		}
		return rv;
	}

	private List<ObjectTypes> parseTypes(List<String> words) {
		List<ObjectTypes> rv = new ArrayList<>();
		for (String w : words) {
			ObjectTypes t = ObjectTypes.findByAny(w);
			if (t == null) {
				Util.showAndLogWarning("Unknown type name", "Type '" + w + "' is unknown. Supported type names are: " +
						Arrays.asList(ObjectTypes.values()).stream().map(type -> type.getElementName()).collect(Collectors.toList())		// could be shorter?
						);
			} else {
				rv.add(t);
			}
		}
		return rv;
	}

	private IContainer getSelectedDirectory(ExecutionEvent event) {
		ISelectionService selectionService = HandlerUtil.getActiveWorkbenchWindow(event).getSelectionService();
		ISelection selection = selectionService.getSelection();
		System.out.println("Current selection: " + selection.getClass());
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.size() == 1) {
				Object first = ss.getFirstElement();
				if (first instanceof IContainer) {
					return (IContainer) first;
				}
			}
		}
		MessageDialog.openWarning(null, "Wrong selection", "To start a download, a single directory or project should be selected.");
		return null;
	}


}
