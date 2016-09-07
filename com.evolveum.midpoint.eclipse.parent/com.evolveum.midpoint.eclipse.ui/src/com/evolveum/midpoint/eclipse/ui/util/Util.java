package com.evolveum.midpoint.eclipse.ui.util;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;

public class Util {

	public static void showAndLog(Severity severity, Options options, String title, String message) {
		showAndLog(severity, options, title, message, null);
	}
	
	public static void showAndLog(Severity severity, Options options, String title, String message, Throwable t) {
		Console.log(severity, options, message, t);
		show(severity, options, title, message);
	}
	
	public static void showInformation(String title, String message) {
		show(Severity.INFO, null, title, message);
	}

	public static void showAndLogInformation(String title, String message) {
		showAndLog(Severity.INFO, null, title, message);
	}
	
	public static void show(Severity severity, Options options, String title, String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				String t = prepareTitle(title, options);
				switch (severity) {
				case INFO: MessageDialog.openInformation(null, t, message); break;
				case WARN: MessageDialog.openWarning(null, t, message); break;
				default: MessageDialog.openError(null, t, message); break;
				}
			}
		});
	}

	public static void showAndLogWarning(String title, String message) {
		showAndLog(Severity.WARN, null, title, message);
	}

	public static void showAndLogError(String title, String message) {
		showAndLog(Severity.ERROR, null, title, message);
	}

	public static void showAndLogError(String title, String message, Throwable t) {
		showAndLog(Severity.ERROR, null, title, message, t);
	}
	
	public static void showError(String title, String message) {
		show(Severity.ERROR, null, title, message);
	}
	
	public static void showWarning(String title, String message) {
		show(Severity.WARN, null, title, message);
	}

	protected static String prepareTitle(String title, Options options) {
		if (options == NO_SERVER_NAME) {
			return title;
		}
		String serverName;
		try {
			serverName = PluginPreferences.getSelectedServerName();
		} catch (Throwable t) {
			Console.logError("Couldn't determine selected server name", t);
			serverName = null;
		}
		if (serverName != null) {
			return title + " [" + serverName + "]";
		} else {
			return title;
		}
	}

	public static void processUnexpectedException(Throwable e) {
		// TODO implement seriously
		e.printStackTrace();
	}

	public static IFile physicalToLogicalFile(String filename) {
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(Path.fromOSString(filename)); 
		if (files.length == 0) {
			return null;
		} else if (files.length > 1) {
			Console.logWarning("More IFiles for " + filename + ": " + Arrays.<IFile>asList(files));
		}
		return files[0];
	}
	
	public static final Options NO_SERVER_NAME = new Options();
	
	public static class Options {
	}
	
}
