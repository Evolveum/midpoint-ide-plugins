package com.evolveum.midpoint.eclipse.ui.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console {

	public static final String CONSOLE_NAME = "midPoint console";
	public static Color redColor;

	public static MessageConsole findConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (CONSOLE_NAME.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(CONSOLE_NAME, null);
		conMan.addConsoles(new IConsole[] { myConsole });

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				showConsole();
			}
		});

		return myConsole;
	}

	// to be called from UI thread only
	public static void showConsole() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IConsoleView view;
		try {
			view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		} catch (PartInitException e) {

			return;
		}
		view.display(findConsole());
	}

	public static void log(String message) {

		MessageConsole console = findConsole();
		
		int start = console.getDocument().getLength();
		
		MessageConsoleStream stream = console.newMessageStream();
		stream.println(formatMessage(message));
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		IPath p = new Path("/T2/aaa.txt");
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(p);
		
		int end = console.getDocument().getLength();
		
//		System.out.println("Start: " + start + ", end: " + end);
//		FileLink link = new FileLink(file, null, -1, -1, -1);
//		try {
//			console.addHyperlink(link, 10, 5);
//		} catch (BadLocationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

	private static String formatMessage(String message) {
		return new Date() + ": " + message;
	}

	public static void logWarning(String message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageConsoleStream stream = findConsole().newMessageStream();
				Display display = Display.getCurrent();
				stream.setColor(display.getSystemColor(SWT.COLOR_DARK_YELLOW));
				stream.println(formatMessage(message));
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static void logError(String message, Throwable t) {
		logError(message);
		logError(t);
	}

	public static void logError(Throwable t) {
		if (t == null) {
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		logError(sw.toString());
		pw.close();
	}

	public static void logError(String message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageConsoleStream stream = findConsole().newMessageStream();
				Display display = Display.getCurrent();
				stream.setColor(display.getSystemColor(SWT.COLOR_RED));
				stream.println(formatMessage(message));
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
	}

	public static void registerHyperlink(String line, IFile logFile, IFile dataFile, IFile consoleFile, IFile opResultFile) {
		MessageConsole console = findConsole();
		int length = console.getDocument().getLength();
		HyperlinksRegistry.getInstance().registerEntry(line, length, logFile, dataFile, consoleFile, opResultFile);
		System.out.println("Registered " + logFile + " et al @" + length);
	}

}
