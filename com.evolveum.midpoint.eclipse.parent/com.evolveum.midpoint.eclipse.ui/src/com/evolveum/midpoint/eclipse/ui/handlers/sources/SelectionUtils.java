package com.evolveum.midpoint.eclipse.ui.handlers.sources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

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
	
	public static List<IFile> getSelectedXmlFiles(ISelection selection) {
		if (selection instanceof ITextSelection) {
			IFile openFile = getFileInActiveEditor(selection);
			if (openFile != null) {
				return Collections.singletonList(openFile);
			}
			return new ArrayList<>();
		}
		if (!(selection instanceof IStructuredSelection)) {
			return new ArrayList<>();
		}
		IStructuredSelection ss = (IStructuredSelection) selection;
		return getXmlFiles(ss);
	}

	public static void addXmlFiles(List<IFile> files, IResource resource) {
		if (resource instanceof IProject && !((IProject) resource).isOpen()) {
			return;
		}
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
	
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow win = getActiveWindow();
		return win != null ? win.getActivePage() : null;
	}
	
	public static IEditorPart getActiveEditor() {
		IWorkbenchPage activePage = getActivePage();
		return activePage != null ? activePage.getActiveEditor() : null;
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

	public static IProject guessSelectedProjectFromExplorerOrNavigator() {
        ISelectionService ss = getSelectionServiceFromActiveWindow();
        if (ss == null) {
        	return null;
        }
        
        IResource resExplorer = getResourceFromSelection(ss.getSelection("org.eclipse.ui.navigator.ProjectExplorer"));
        IResource resNavigator = getResourceFromSelection(ss.getSelection("org.eclipse.ui.views.ResourceNavigator"));
        IProject projExplorer = resExplorer != null ? resExplorer.getProject() : null;
        IProject projNavigator = resNavigator != null ? resNavigator.getProject() : null;
        if (projExplorer == null) {
        	return projNavigator;
        } else if (projNavigator == null) {
        	return projExplorer;
        } else if (projExplorer.equals(projNavigator)) {
        	return projExplorer;
        } else {
        	System.out.println("Conflict: " + projExplorer + " vs " + projNavigator);
        	return null;
        }
	}

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
	
	
	public static IFile getFileInActiveEditor(ISelection selection) {
		if (!(selection instanceof ITextSelection)) {
			return null;
		}
		String text = ((ITextSelection) selection).getText();
		IEditorPart activeEditor = getActiveEditor();
		if (activeEditor == null) {
			return null;
		}
		IEditorInput editorInput = activeEditor.getEditorInput();
		if (!(editorInput instanceof FileEditorInput)) {
			return null;
		}
		IFile file = ((FileEditorInput) editorInput).getFile();
		if (StringUtils.isEmpty(text)) {
			return file;
		}
		IDocument doc = (IDocument)activeEditor.getAdapter(IDocument.class);
		if (doc == null) {
			return null;
		}
		String allText = doc.get();
		if (text.equals(allText)) {
			return file;
		}
		// only part of file is selected
		return null;
	}

	public static IProject guessSelectedProject(ISelection selection, String defaultName) {
		List<IFile> files = getSelectedXmlFiles(selection);
		if (!files.isEmpty()) {
			return files.get(0).getProject();
		}
		IResource resource = getResourceFromActiveWindow();
		if (resource != null) {
			return resource.getProject();
		}
		IProject project = guessSelectedProjectFromExplorerOrNavigator();
		if (project != null) {
			return project;
		}
		if (StringUtils.isNotBlank(defaultName)) {
			if ("*".equals(defaultName)) {
				IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
				for (IProject p : projects) {
					if (p.isOpen()) {
						return p;
					}
				}
			} else {
				IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(defaultName);
				if (p != null && p.exists()) {
					return p;
				}
			}
		}
		return null;
	}
	
	public static class CursorPosition {
		public final int line, column;
		public CursorPosition(int line, int column) {
			this.line = line;
			this.column = column;
		}
		@Override
		public String toString() {
			return "CursorPosition [line=" + line + ", column=" + column + "]";
		}
	}
	
	public static CursorPosition getCursorPosition() {
		// TODO ok?
		IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
        IWorkbenchPage page = win != null ? win.getActivePage() : null;
        IEditorPart editor = page != null ? page.getActiveEditor() : null;
        Control control = editor.getAdapter(Control.class);
        if (editor instanceof ITextEditor && control instanceof StyledText) {
        	IDocumentProvider provider = ((ITextEditor)editor).getDocumentProvider();
        	IDocument document = provider.getDocument(editor.getEditorInput());
        	try {
        		StyledText styledText = (StyledText) control;
        		int caret = styledText.getCaretOffset();
        		int line = document.getLineOfOffset(caret);
        		int lineOffset = document.getLineOffset(line);
       			int tabWidth = styledText.getTabs();
       			int column = 0;
       			for (int i = lineOffset; i < caret; i++) {
       				if (document.getChar(i) == '\t') {
        				column += tabWidth - (tabWidth == 0 ? 0 : column % tabWidth);
       				} else {
        				column++;
       				}
       			}
                return new CursorPosition(line, column); 
        	} catch (BadLocationException e) {
                e.printStackTrace();
            }
        } else {
        	System.out.println("Couldn't determine column, for editor=" + editor + ", control=" + control);
        }

        return null;
	}

}
