package com.evolveum.midpoint.eclipse.logviewer.tree;

import com.evolveum.midpoint.eclipse.logviewer.outline.TreeNode;
import com.evolveum.midpoint.eclipse.logviewer.parsing.Parser;

public class GenericNodeContent extends OutlineNodeContent {
	
	public GenericNodeContent(String label) {
		setDefaultLabel(label);
	}

	public String toString() {
		return super.toString() + ": " + getDefaultLabel();
	}
}
