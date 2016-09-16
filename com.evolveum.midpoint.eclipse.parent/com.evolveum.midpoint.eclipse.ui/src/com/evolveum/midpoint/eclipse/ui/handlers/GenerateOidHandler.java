package com.evolveum.midpoint.eclipse.ui.handlers;

import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import com.evolveum.midpoint.eclipse.ui.util.Console;

public class GenerateOidHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Display display = Display.getCurrent();
		if (display == null) {
			Console.logError("No display.");
			return null;
		}
		Clipboard cb = new Clipboard(display);
        TextTransfer textTransfer = TextTransfer.getInstance();
        
		UUID oid = UUID.randomUUID();
        cb.setContents(new Object[] { oid.toString() }, new Transfer[] { textTransfer });
        Console.logMinor("OID of " + oid + " was generated and stored into clipboard.");
		return null;
	}

}
