package com.evolveum.midpoint.eclipse.logviewer.tree;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import com.evolveum.midpoint.eclipse.logviewer.config.EditorConfiguration;

public class PolicyConstraintNodeDefinition extends OutlineNodeDefinition<PolicyConstraintNodeContent> {

	private PolicyConstraintNodeDefinition(EditorConfiguration editorConfiguration) {
		super(editorConfiguration);
	}

	@Override
	public PolicyConstraintNodeContent recognize(int lineNumber, String line, String entry, String header, IRegion region, IDocument document) throws BadLocationException {
		if (!line.startsWith("---[ POLICY CONSTRAINT")) {
			return null;
		}
		PolicyConstraintNodeContent content = new PolicyConstraintNodeContent();
		content.setDefaultLabel(line.substring(5), "]---");
		return content;
	}

	@Override
	public ContentSelectionStrategy getContentSelectionStrategy() {
		return ContentSelectionStrategy.HEADER_LAST;
	}
	
	public static OutlineNodeDefinition<?> parseFromLine(EditorConfiguration editorConfiguration, String line) {
		return new PolicyConstraintNodeDefinition(editorConfiguration).parseFromLine(line);
	}
}
