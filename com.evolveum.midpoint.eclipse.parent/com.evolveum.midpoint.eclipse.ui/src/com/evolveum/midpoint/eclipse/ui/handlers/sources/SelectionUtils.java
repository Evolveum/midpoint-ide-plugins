package com.evolveum.midpoint.eclipse.ui.handlers.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class SelectionUtils {

	public static List<IFile> getXmlFiles(IStructuredSelection selection) {
		List<IFile> files = new ArrayList<>();
		for (Object item : selection.toList()) {
			if (item instanceof IResource) {
				SelectionUtils.addXmlFiles(files, (IResource) item);
			}
		}
		return files;
	}

	public static void addXmlFiles(List<IFile> files, IResource resource) {
		if (resource instanceof IContainer) {
			try {
				for (IResource member : ((IContainer) resource).members()) {
					addXmlFiles(files, member);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			if ("XML".equalsIgnoreCase(file.getFileExtension())) {
				files.add(file);
			}
		} else {
			// TODO issue warning
		}
	}

	public static ISelection getSelection(ExecutionEvent event) {
		ISelectionService selectionService = HandlerUtil.getActiveWorkbenchWindow(event).getSelectionService();
		ISelection selection = selectionService.getSelection();
		System.out.println("Current selection: " + selection.getClass());
		return selection;
	}

	// not null
	public static List<IFile> getXmlFiles(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			return getXmlFiles((IStructuredSelection) selection);
		} else {
			return Collections.emptyList();
		}
	}

	public static ISelection getWorkbenchSelection() {
		IWorkbenchWindow win = getActiveWindow();
		if (win == null) {
			return null;
		}
		IWorkbenchPage page = win.getActivePage();
		if (page == null) {
			return null;
		}
		ISelection selection = page.getSelection();
		return selection;
	}

	public static IWorkbenchWindow getActiveWindow() {
		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb == null) {
			return null;
		}
		return wb.getActiveWorkbenchWindow();
	}
	
	public static ISelectionService getSelectionServiceFromActiveWindow() {
		IWorkbenchWindow win = getActiveWindow();
		return win != null ? win.getSelectionService() : null;
	}
	
	public static IResource getResourceFromActiveWindow() {
		ISelectionService ss = getSelectionServiceFromActiveWindow();
		if (ss == null) {
			return null;
		}
		return getResourceFromSelection(ss.getSelection());
	}

	// FIXME might or might not work...
//	public static IResource getSelectedResource() {
//        ISelectionService ss = getSelectionServiceFromActiveWindow();
//        
//        IResource res = getResourceFromSelection(ss.getSelection("org.eclipse.ui.navigator.ProjectExplorer"));
//        if (res != null) {
//        	return res;
//        }
//        return getResourceFromSelection(ss.getSelection("org.eclipse.ui.views.ResourceNavigator"));
//	}

	public static IResource getResourceFromSelection(ISelection sel) {
		Object selectedObject = sel;
        if (sel instanceof IStructuredSelection) {
        	selectedObject = ((IStructuredSelection)sel).getFirstElement();
        }
        if (selectedObject instanceof IAdaptable) {
        	return (IResource) ((IAdaptable) selectedObject).getAdapter(IResource.class);
        } else {
        	return null;
        }
	}
	
	public static List<IProject> getProjects() {
		List<IProject> rv = new ArrayList<>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (project.isOpen()) {
				rv.add(project);
			}
		}
		return rv;
	}

}
