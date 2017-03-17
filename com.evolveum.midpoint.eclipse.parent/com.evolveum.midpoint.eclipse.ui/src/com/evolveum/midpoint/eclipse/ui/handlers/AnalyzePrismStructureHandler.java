package com.evolveum.midpoint.eclipse.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import com.evolveum.midpoint.eclipse.ui.handlers.sources.SelectionUtils;
import com.evolveum.midpoint.eclipse.ui.prismAnalyzer.PrismAnalyzer;
import com.evolveum.midpoint.eclipse.ui.util.Console;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class AnalyzePrismStructureHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Display display = Display.getCurrent();
		if (display == null) {
			Console.logError("No display.");
			return null;
		}
		Clipboard cb = new Clipboard(display);
        TextTransfer textTransfer = TextTransfer.getInstance();
        
        Object contents = cb.getContents(textTransfer);
        if (contents == null) {
        	Util.showAndLogWarning("No clipboard content", "No prism content to analyze. Please cut or copy something into clipboard and try again.");
        	return null;
        }
        SelectionUtils.CursorPosition position = SelectionUtils.getCursorPosition();
        System.out.println("Current cursor position: " + position);
        int column = position != null ? position.column : 0;
        System.out.println("Input to parse:\n" + contents);
        String output = PrismAnalyzer.processInput(contents.toString(), column);
        System.out.println("Output:\n" + output);
        cb.setContents(new Object[] { output }, new Transfer[] { textTransfer });
        Console.logMinor("Prism structure was parsed and result was stored into clipboard.");
		return null;
	}

}
