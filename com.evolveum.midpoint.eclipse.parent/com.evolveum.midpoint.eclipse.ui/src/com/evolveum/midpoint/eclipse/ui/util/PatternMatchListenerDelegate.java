package com.evolveum.midpoint.eclipse.ui.util;

import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

import com.evolveum.midpoint.eclipse.ui.handlers.server.UploadOrExecuteHandler;

/**
 * TODO fix this brutally hacked class...
 * 
 * @author Pavol
 */

public class PatternMatchListenerDelegate implements IPatternMatchListenerDelegate {

	private boolean enabled;
	private TextConsole console;

	@Override
	public void connect(TextConsole console) {
		System.out.println("AddLinkLineTracker init for " + console.getName());
		enabled = Console.CONSOLE_NAME.equals(console.getName());
		this.console = console;
	}

	@Override
	public void disconnect() {
	}
	
	public static final int SERVER_LOG_START = UploadOrExecuteHandler.CONSOLE_REFERENCE_TEXT.indexOf(UploadOrExecuteHandler.SERVER_LOG);
	public static final int SERVER_LOG_LENGTH = UploadOrExecuteHandler.SERVER_LOG.length();
	public static final int DATA_OUTPUT_START = UploadOrExecuteHandler.CONSOLE_REFERENCE_TEXT.indexOf(UploadOrExecuteHandler.DATA_OUTPUT);
	public static final int DATA_OUTPUT_LENGTH = UploadOrExecuteHandler.DATA_OUTPUT.length();
	public static final int CONSOLE_OUTPUT_START = UploadOrExecuteHandler.CONSOLE_REFERENCE_TEXT.indexOf(UploadOrExecuteHandler.CONSOLE_OUTPUT);
	public static final int CONSOLE_OUTPUT_LENGTH = UploadOrExecuteHandler.CONSOLE_OUTPUT.length();
	public static final int OP_RESULT_START = UploadOrExecuteHandler.CONSOLE_REFERENCE_TEXT.indexOf(UploadOrExecuteHandler.OP_RESULT);
	public static final int OP_RESULT_LENGTH = UploadOrExecuteHandler.OP_RESULT.length();

	@Override
	public void matchFound(PatternMatchEvent event) {
		
		if (!enabled) {
			return;
		}

		HyperlinksRegistry registry = HyperlinksRegistry.getInstance();
		
		HyperlinksRegistry.Entry entry = registry.peek();
		System.out.println("Head of hyperlink queue = " + entry);
		
		if (entry == null || entry.getLineOffset() > event.getOffset()) {
			System.out.println("No hyperlink entry for event at " + event.getOffset() + " (head is " + entry + ")");
			return;
		}
		
		registry.poll();
		
		try {
			console.addHyperlink(new FileLink(entry.getLogFile(), UploadOrExecuteHandler.getLogViewerEditorId(), -1, -1, -1), event.getOffset() + SERVER_LOG_START, SERVER_LOG_LENGTH);
			console.addHyperlink(new FileLink(entry.getDataFile(), UploadOrExecuteHandler.getTextEditorId(), -1, -1, -1), event.getOffset() + DATA_OUTPUT_START, DATA_OUTPUT_LENGTH);
			console.addHyperlink(new FileLink(entry.getConsoleFile(), UploadOrExecuteHandler.getTextEditorId(), -1, -1, -1), event.getOffset() + CONSOLE_OUTPUT_START, CONSOLE_OUTPUT_LENGTH);
			console.addHyperlink(new FileLink(entry.getResultFile(), UploadOrExecuteHandler.getTextEditorId(), -1, -1, -1), event.getOffset() + OP_RESULT_START, OP_RESULT_LENGTH);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
