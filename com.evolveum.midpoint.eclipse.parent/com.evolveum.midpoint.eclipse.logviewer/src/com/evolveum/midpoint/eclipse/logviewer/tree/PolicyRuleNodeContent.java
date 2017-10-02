package com.evolveum.midpoint.eclipse.logviewer.tree;

import com.evolveum.midpoint.eclipse.logviewer.outline.TreeNode;
import com.evolveum.midpoint.eclipse.logviewer.parsing.Parser;

public class PolicyRuleNodeContent extends OutlineNodeContent {

	PolicyRuleNodeContent previousItem;

	@Override
	public TreeNode createTreeNode(Parser parser) {
		
		OutlineNode<PolicyRuleNodeContent> previous = getPrevious();
		if (owner.getDate() != null && previous != null) {
			owner.setDelta(owner.computeDeltaSince(previous));
		}
		
		return super.createTreeNode(parser);
	}
	
	@SuppressWarnings("unchecked")
	private OutlineNode<PolicyRuleNodeContent> getPrevious() {
		if (owner.getPreviousSibling() != null && owner.getPreviousSibling().getContent() instanceof PolicyRuleNodeContent) {
			return (OutlineNode<PolicyRuleNodeContent>) owner.getPreviousSibling();
		} else {
			return null;
		}
	}

}
