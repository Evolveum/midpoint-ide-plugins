package com.evolveum.midpoint.eclipse.ui.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

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
	
	@Override
	public void matchFound(PatternMatchEvent event) {
		
		if (!enabled) {
			return;
		}
		
		String text; 
        try { 
            text = console.getDocument().get(event.getOffset(), event.getLength()); 
        } catch (BadLocationException e) { 
        	Util.processUnexpectedException(e);
            return; 
        }
        
        String a = StringUtils.substringAfterLast(text, "(#");
        String seq = StringUtils.substringBefore(a, ")");
        System.out.println("text = "+text+", seq=" + seq);

		HyperlinksRegistry registry = HyperlinksRegistry.getInstance();
		
		HyperlinksRegistry.Entry entry = registry.get(seq);
		if (entry == null) {
			return;
		}
		
		try {
			for (int i = 0; i < entry.labels.size(); i++) {
				String label = entry.labels.get(i);
				IFile file = entry.files.get(i);
				String editorId = entry.editorIds.get(i);
				int start = text.indexOf(label);
				if (start >= 0) {
					console.addHyperlink(new FileLink(file, editorId, -1, -1, -1), event.getOffset() + start, label.length());					
				}
			}
		} catch (BadLocationException e) {
			Util.processUnexpectedException(e);
		}
	}

}
