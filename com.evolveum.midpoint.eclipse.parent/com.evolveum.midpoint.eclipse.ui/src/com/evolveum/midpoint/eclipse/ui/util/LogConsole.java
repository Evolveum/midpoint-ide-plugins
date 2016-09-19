package com.evolveum.midpoint.eclipse.ui.util;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
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

import com.evolveum.midpoint.eclipse.ui.handlers.ServerLogUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.ServerLogUtils.LogFileFragment;
import com.evolveum.midpoint.eclipse.ui.internal.EclipseActivator;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;

public class LogConsole extends MessageConsole {

	public LogConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
	}

	public static final String CONSOLE_NAME_PREFIX = "MidPoint server log console: ";
	public static Color redColor;
	
	public static Thread updaterThread;
	
	private long lastPosition;
	
	static {
		updaterThread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					if (EclipseActivator.getInstance() == null) {
						return;		// we were closed
					}
					
					int interval = PluginPreferences.getLogConsoleRefreshInterval();
					try {
						Thread.sleep(Math.max(interval, 1) * 1000L);
					} catch (InterruptedException e) {
						return;
					}
					if (interval == 0) {
						continue;
					}
					ConsolePlugin plugin = ConsolePlugin.getDefault();
					IConsoleManager conMan = plugin.getConsoleManager();
					IConsole[] existing = conMan.getConsoles();
					for (int i = 0; i < existing.length; i++) {
						if (existing[i].getName() != null && existing[i].getName().startsWith(CONSOLE_NAME_PREFIX)) {
							updateConsole(existing[i]);
						}
					}
				}
			}
		});
		updaterThread.setDaemon(true);
		updaterThread.start();
	}
	
	protected static void updateConsole(IConsole console) {
		if (!(console instanceof LogConsole)) {
			return;
		}
		String url = console.getName().substring(CONSOLE_NAME_PREFIX.length());
		//System.out.println("Updating log console for " + url);
		for (ServerInfo server : PluginPreferences.getServers()) {
			if (url.equals(server.getUrl())) {
				((LogConsole) console).update(server);
				break;
			}
		}
	}

	private void update(ServerInfo server) {
		if (StringUtils.isBlank(server.getLogFile())) {			// temporary
			return;
		}
		LogFileFragment increment = ServerLogUtils.getLogFileFragment(server, lastPosition);
		addContent(increment);
	}

	public void addContent(LogFileFragment content) {
		if (content == null) {
			return;
		}

		MessageConsoleStream stream = newMessageStream();
		try {
			stream.write(content.content);
			stream.close();
		} catch (IOException e) {
			Console.logError("Couldn't write to server log console", e);
		}
		lastPosition = content.currentLogSize;
	}

	public static LogConsole findConsole(String url) {
		String consoleName = CONSOLE_NAME_PREFIX + url;
		
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (consoleName.equals(existing[i].getName()))
				return (LogConsole) existing[i];
		// no console found, so create a new one
		LogConsole myConsole = new LogConsole(consoleName, null);
		conMan.addConsoles(new IConsole[] { myConsole });

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				showConsole(url);
			}
		});

		return myConsole;
	}


	// to be called from UI thread only
	public static LogConsole showConsole(String url) {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		IConsoleView view;
		try {
			view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		} catch (PartInitException e) {
			return null;
		}
		LogConsole console = findConsole(url);
		view.display(console);
		return console;
	}

	public void setContent(LogFileFragment content) {
		clearConsole();
		addContent(content);
	}

	public void clearContent() {
		clearConsole();
		lastPosition = 0;
	}

}
