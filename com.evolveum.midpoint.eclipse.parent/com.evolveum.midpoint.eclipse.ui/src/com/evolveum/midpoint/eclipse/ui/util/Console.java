package com.evolveum.midpoint.eclipse.ui.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

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

import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.util.Util.Options;

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
	
	public static void log(Severity severity, Options options, String message, Throwable t) {
		if (message != null) {
			log(severity, options, message, false);
		}
		if (t != null) {
			log(severity, options, toString(t), true);
		}
	}
	
	private static String toString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}

	public static void log(Severity severity, Options options, String message) {
		log(severity, options, message, false);
	}
	
	public static void log(Severity severity, Options options, String message, boolean exception) {
		String formatted = formatMessage(message, options);
		switch (severity) {
		case INFO: _plain(formatted); break;
		case WARN: _colored(formatted, SWT.COLOR_DARK_YELLOW); break;
		default: _colored(formatted, exception ? SWT.COLOR_DARK_RED : SWT.COLOR_RED); break;
		}
	}

	private static String formatMessage(String message, Options options) {
		String prefix;
		if (options == Util.NO_SERVER_NAME) {
			prefix = "";
		} else {
			prefix = " " + serverPrefix();
		}
		return new Date() + prefix + ": " + message;
	}

	public static void log(String message) {
		log(Severity.INFO, null, message, null);
	}

	public static void logWarning(String message, Throwable t) {
		log(Severity.WARN, null, message, t);
	}
	
	public static void logWarning(String message) {
		log(Severity.WARN, null, message, null);
	}

	public static void logError(String message) {
		log(Severity.ERROR, null, message, null);
	}

	public static void logError(String message, Throwable t) {
		log(Severity.ERROR, null, message, t);
	}

	public static void logError(Throwable t) {
		log(Severity.ERROR, null, null, t);
	}
	
	private static void _plain(String message) {
		MessageConsole console = findConsole();
		MessageConsoleStream stream = console.newMessageStream();
		stream.println(message);
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void _colored(String message, int color) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageConsoleStream stream = findConsole().newMessageStream();
				Display display = Display.getCurrent();
				stream.setColor(display.getSystemColor(color));
				stream.println(message);
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static String serverPrefix() {
		return "[" + PluginPreferences.getSelectedServerName() + "]";
	}

}
