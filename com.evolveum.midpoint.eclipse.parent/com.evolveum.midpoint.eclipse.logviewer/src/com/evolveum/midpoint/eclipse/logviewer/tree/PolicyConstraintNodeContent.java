package com.evolveum.midpoint.eclipse.logviewer.tree;

import com.evolveum.midpoint.eclipse.logviewer.outline.TreeNode;
import com.evolveum.midpoint.eclipse.logviewer.parsing.Parser;

public class PolicyConstraintNodeContent extends OutlineNodeContent {

	PolicyConstraintNodeContent previousItem;

	@Override
	public TreeNode createTreeNode(Parser parser) {
		
		OutlineNode<PolicyConstraintNodeContent> previous = getPrevious();
		if (owner.getDate() != null && previous != null) {
			owner.setDelta(owner.computeDeltaSince(previous));
		}
		
		return super.createTreeNode(parser);
	}
	
	@SuppressWarnings("unchecked")
	private OutlineNode<PolicyConstraintNodeContent> getPrevious() {
		if (owner.getPreviousSibling() != null && owner.getPreviousSibling().getContent() instanceof PolicyConstraintNodeContent) {
			return (OutlineNode<PolicyConstraintNodeContent>) owner.getPreviousSibling();
		} else {
			return null;
		}
	}

}
