package com.evolveum.midpoint.eclipse.logviewer.tree;

import com.evolveum.midpoint.eclipse.logviewer.outline.TreeNode;
import com.evolveum.midpoint.eclipse.logviewer.parsing.Parser;

public class MappingNodeContent extends OutlineNodeContent {

	MappingNodeContent previousMappingItem;

	@Override
	public TreeNode createTreeNode(Parser parser) {
		
		OutlineNode<MappingNodeContent> previous = getPreviousMapping();
		if (owner.getDate() != null && previous != null) {
			owner.setDelta(owner.computeDeltaSince(previous));
		}
		
		return super.createTreeNode(parser);
	}
	
	@SuppressWarnings("unchecked")
	private OutlineNode<MappingNodeContent> getPreviousMapping() {
		if (owner.getPreviousSibling() != null && owner.getPreviousSibling().getContent() instanceof MappingNodeContent) {
			return (OutlineNode<MappingNodeContent>) owner.getPreviousSibling();
		} else {
			return null;
		}
	}



//	public MappingItemContent(IRegion region, int startLine, IDocument document, MappingItemContent previousMappingItem, String label, List<TreeNode> scriptsAndExpressions) {
//		super(region, startLine, document);
//		this.previousMappingItem = previousMappingItem;
//		
//		if (this.date != null && previousMappingItem != null && previousMappingItem.date != null) {
//			label += " [delta: " + (date.getTime() - previousMappingItem.date.getTime()) + " ms]";
//		}
//		
//		treeNode = new TreeNode(label, region);		
//		treeNode.addChildren(scriptsAndExpressions);
//	}

}
