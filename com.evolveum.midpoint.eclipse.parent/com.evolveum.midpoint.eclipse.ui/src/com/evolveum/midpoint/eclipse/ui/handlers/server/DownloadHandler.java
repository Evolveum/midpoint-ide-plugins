package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.evolveum.midpoint.eclipse.runtime.RuntimeActivator;
import com.evolveum.midpoint.eclipse.runtime.api.ConnectionParameters;
import com.evolveum.midpoint.eclipse.runtime.api.ObjectTypes;
import com.evolveum.midpoint.eclipse.runtime.api.RuntimeService;
import com.evolveum.midpoint.eclipse.runtime.api.ServerObject;
import com.evolveum.midpoint.eclipse.runtime.api.ServerResponse;
import com.evolveum.midpoint.eclipse.ui.prefs.DownloadPreferencePage;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Util;
import com.evolveum.midpoint.util.Holder;

public class DownloadHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IContainer root = determineDownloadRoot(event);
		if (root == null) {
			return null;
		}
		System.out.println("Download root: " + root);
		
		Job job = new Job("Downloading from midPoint") {
			protected IStatus run(IProgressMonitor monitor) {
				
				List<ObjectTypes> typesToDownload = determineTypesToDownload();
				Console.log("Downloading object types: " + typesToDownload.stream().map(type -> type.getElementName()).collect(Collectors.toList()));
				int limit = PluginPreferences.getDownloadedObjectsLimit();
				int count = 0;
				
				RuntimeService runtime = RuntimeActivator.getRuntimeService();
				ConnectionParameters connectionParameters = PluginPreferences.getConnectionParameters();

				monitor.beginTask("Downloading", typesToDownload.size());
				try {
main:				for (ObjectTypes type : typesToDownload) {
						if (monitor.isCanceled()) {
							break;
						}
						monitor.subTask("Downloading " + type.getRestType());
						List<ServerObject> objects = runtime.downloadObjects(type, limit, connectionParameters);
						
						monitor.subTask("Writing " + type.getRestType());
						for (ServerObject object : objects) {
							if (monitor.isCanceled()) {
								break main;
							}
							IFile file = prepareOutputFileForCreation(object, root);
							if (file.exists()) {
								String overwrite = PluginPreferences.getOverwriteWhenDownloading();
								if (DownloadPreferencePage.VALUE_NEVER.equals(overwrite)) {
									Console.log("File " + file + " already exists, skipping.");
									continue;
								}
								final Holder<Integer> responseHolder = new Holder<>();
								if (DownloadPreferencePage.VALUE_ASK.equals(overwrite)) {
									Display.getDefault().syncExec(new Runnable() {
										public void run() {
											MessageDialog dialog = new MessageDialog(
													null, "Confirm overwrite", null, "Are you sure to overwrite " + file + "?",
													MessageDialog.QUESTION,
													new String[] {"Yes", "No", "Cancel"},
													0);
											responseHolder.setValue(dialog.open());
										}
									});
									if (responseHolder.getValue() == 2) {
										break main;
									} else if (responseHolder.getValue() == 1) {
										continue;
									}
								}
								file.delete(true, monitor);
							} else {
								createParentFolders(file.getParent());
							}
							file.create(new ByteArrayInputStream(object.getXml().getBytes()), true, monitor);
							Console.log("File " + file + " was successfully created.");
							count++;
						}
						
						monitor.worked(1);
					}
				} catch (Throwable t) {
					Util.processUnexpectedException(t);
				}
				Console.log("Downloaded " + count + " object(s)");
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		
		return null;
	}
	
	private IFile prepareOutputFileForCreation(ServerObject object, IContainer root) {
		IPath path = computeFilePath(object, root);
		System.out.println("Path = " + path);
		if (path == null) {
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}


	public void createParentFolders(IContainer container) throws CoreException {
		if (!(container instanceof IFolder)) {
			return;
		}
		IFolder folder = (IFolder) container;
	    if (!folder.exists()) {
	        createParentFolders(folder.getParent());
	        folder.create(true, true, null);
	    }
	}

	private IPath computeFilePath(ServerObject object, IContainer root) {
		String pattern = PluginPreferences.getDownloadedFileNamePattern();
		if (StringUtils.isBlank(pattern)) {
			return null;
		}

		// TODO what if OID contains '$'?
		String patternResolved = pattern
				.replace("$t", object.getType().getElementName())
				.replace("$T", object.getType().getRestType())
				.replace("$o", object.getOid())
				.replace("$n", object.getName());		// name is last; to prevent problems if it would contain '$'
		
		System.out.println("pattern = " + pattern + ", resolvedPattern = " + patternResolved);
		IPath rv = root.getFullPath().append(new Path(patternResolved));
		System.out.println("Final result = " + rv);
		return rv;
	}
	
	protected List<ObjectTypes> determineTypesToDownload() {
		List<ObjectTypes> rv = new ArrayList<>();
		String include = StringUtils.normalizeSpace(PluginPreferences.getIncludeInDownload());
		String exclude = StringUtils.normalizeSpace(PluginPreferences.getExcludeFromDownload());

		if (StringUtils.isEmpty(include)) {
			rv.addAll(Arrays.<ObjectTypes>asList(ObjectTypes.values()));
		} else {
			rv.addAll(parseTypes(include));
		}
		if (exclude != null) {
			rv.removeAll(parseTypes(exclude));
		}
		return rv;
	}

	private List<ObjectTypes> parseTypes(String string) {
		List<ObjectTypes> rv = new ArrayList<>();
		for (String w : StringUtils.split(string)) {
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

	private IContainer determineDownloadRoot(ExecutionEvent event) {
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
