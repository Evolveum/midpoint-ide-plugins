package com.evolveum.midpoint.eclipse.ui.util;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.widgets.Display;

public class Util {

	public static void showAndLogInformation(String title, String message) {
		//logInformation(message);
		Console.log(message);
		showInformation(title, message);
	}
	
	public static void showInformation(String title, String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(null, title, message);
			}
		});
	}

	public static void showAndLogWarning(String title, String message) {
		Console.logWarning(message);
		showWarning(title, message);
	}

	public static void showAndLogError(String title, String message) {
		Console.logError(message);
		showError(title, message);
	}

	public static void showAndLogError(String title, String message, Throwable t) {
		Console.logError(message);
		Console.logError(t);
		showError(title, message);
	}
	
	public static void showError(String title, String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(null, title, message);
			}
		});
	}
	
	public static void showWarning(String title, String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openWarning(null, title, message);
			}
		});
	}

	public static void processUnexpectedException(Throwable e) {
		// TODO implement seriously
		e.printStackTrace();
	}

	public static IPath physicalToLogicalPath(String filename) {
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(Path.fromOSString(filename)); 
		if (files.length == 0) {
			return null;
		} else if (files.length > 1) {
			Console.logWarning("More IFiles for " + filename + ": " + Arrays.<IFile>asList(files));
		}
		return files[0].getFullPath();
	}
	
	

}
