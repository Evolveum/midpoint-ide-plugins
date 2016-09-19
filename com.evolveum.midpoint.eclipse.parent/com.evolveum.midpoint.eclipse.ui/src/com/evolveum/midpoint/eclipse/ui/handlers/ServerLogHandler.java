package com.evolveum.midpoint.eclipse.ui.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.evolveum.midpoint.eclipse.ui.PluginConstants;
import com.evolveum.midpoint.eclipse.ui.handlers.ServerLogUtils.LogFileFragment;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.LogConsole;

public class ServerLogHandler extends AbstractHandler {
	
	private static Map<String,Long> positionMarks = new HashMap<>();
	
	public static Long getCurrentMark() {
		ServerInfo server = PluginPreferences.getSelectedServer();
		return server != null ? positionMarks.get(server.getUrl()) : null;
	}

	public static void setCurrentMark(ServerInfo server, Long mark) {
		positionMarks.put(server.getUrl(), mark);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ServerInfo server = PluginPreferences.getSelectedServer();
		if (server == null) {
			Console.logWarning("No server is selected");
			return null;
		}
		
		switch (event.getCommand().getId()) {
		case PluginConstants.CMD_MARK_CURRENT_LOG_POSITION:
			markCurrentPosition(server);
			break;
		case PluginConstants.CMD_SHOW_LOG_IN_CONSOLE:
			showLogInConsole(event, server);
			break;
		case PluginConstants.CMD_SHOW_LOG_IN_EDITOR:
			showLogInViewer(event, server);
			break;
		case PluginConstants.CMD_CLEAR_SERVER_LOG:
			clearServerLog(server);
			break;
		}
		return null;
	}

	public void clearServerLog(ServerInfo server) {
		MessageDialog dialog = new MessageDialog(
				null, "Confirm clear log", null, 
				"Are you sure to clear the log on server " + server.getDisplayName() + "?",
				MessageDialog.QUESTION,
				new String[] {"Yes", "Cancel"},
				0);
		if (dialog.open() != 0) {
			return;
		}
		Job job = new Job("Clearing server log") {
			protected IStatus run(IProgressMonitor monitor) {
				ServerLogUtils.clearLog(server);
				LogConsole console = LogConsole.findConsole(server.getUrl());
				if (console != null) {
					console.clearContent();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public void showLogInConsole(ExecutionEvent event, ServerInfo server) {
		LogConsole console = LogConsole.showConsole(server.getUrl());
		if (console == null) {
			return;
		}
		Job job = new Job("Reading server log") {
			protected IStatus run(IProgressMonitor monitor) {
				console.setContent(getContent(server, event));
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}

	public void showLogInViewer(ExecutionEvent event, ServerInfo server) {
		ISelection selection = SelectionUtils.getSelection(event);
		IProject project = SelectionUtils.guessSelectedProject(selection, PluginPreferences.getLogFileDefaultProject());
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		if (PluginConstants.VALUE_CONSOLE.equals(event.getParameter(PluginConstants.PARAM_FROM))) {
			LogConsole console = LogConsole.findConsole(server.getUrl());
			if (console == null) {
				return;
			}
			createAndOpenFile(event, console.getDocument().get().getBytes(), project, page);
			return;
		}
		Job job = new Job("Reading server log") {
			protected IStatus run(IProgressMonitor monitor) {
				LogFileFragment fragment = getContent(server, event);
				if (fragment != null) {
					createAndOpenFile(event, fragment.content, project, page);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
	}
	
	private void createAndOpenFile(ExecutionEvent event, byte[] content, IProject project, IWorkbenchPage page) {
		String pattern = PluginPreferences.getLogFileNamePattern();
		if (pattern == null || pattern.trim().isEmpty()) {
			return;
		}

		String patternResolved = pattern
				.replace("$n", ResourceUtils.formatActionCounter(PluginPreferences.getAndIncrementLogCounter()))
				.replace("$s", ResourceUtils.fixComponent(PluginPreferences.getSelectedServerShortName())); 

		System.out.println("patternResolved = " + patternResolved);
		IPath resolvedPath = new Path(patternResolved);
		if (!resolvedPath.isAbsolute()) {
			if (project == null) {
				Console.logWarning("No project to generate file with the log content in. Please provide one.");
				return;
			}
			resolvedPath = project.getFullPath().append(resolvedPath);
		}
		System.out.println("Full resolvedPath = " + resolvedPath);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(resolvedPath);
		if (file.exists()) {
			try {
				file.delete(true, null);
			} catch (CoreException e) {
				Console.logWarning("Couldn't delete file " + file.getFullPath() + ": " + e.getMessage(), e);
			}
		}
		if (ResourceUtils.createOutputFile(file, content) == null) {
			return;
		}
		
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					IDE.openEditor(page, file, FileRequestHandler.getLogViewerEditorId(content));
				} catch (PartInitException e) {
					Console.logError("Couldn't open log file viewer: " + e.getMessage(), e);
				}
			}
		});
	}


	private LogFileFragment getContent(ServerInfo server, ExecutionEvent event) {
		long start;
		boolean fromEnd;
		switch (event.getParameter(PluginConstants.PARAM_FROM)) {
		case PluginConstants.VALUE_START:
			start = 0; 
			fromEnd = false;
			break;
		case PluginConstants.VALUE_BACK_N:
			start = 1024 * PluginPreferences.getLogGoBackN();
			fromEnd = true;
			break;
		case PluginConstants.VALUE_MARK:
			start = getCurrentMark();		// server is non-null, so start is non-null
			fromEnd = false;
			break;
		case PluginConstants.VALUE_NOW:
			start = 0;
			fromEnd = true;
			break;
		default:
			Console.logError("Unknown 'from' parameter: " + event.getParameter(PluginConstants.PARAM_FROM));
			return new LogFileFragment("");
		}
		
		return ServerLogUtils.getLogFileFragment(server, start, fromEnd);
	}

	public void markCurrentPosition(ServerInfo server) {
		Long position = ServerLogUtils.getLogPosition();
		System.out.println("Position = " + position);
		if (position == null) {
			Console.logWarning("Log file size couldn't be determined.");
		} else {
			setCurrentMark(server, position);
			Console.logMinor("Log file position was remembered at point " + position);
		}
	}


}
