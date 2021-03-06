package com.evolveum.midpoint.eclipse.logviewer.tree;

import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.eclipse.logviewer.outline.TreeNode;
import com.evolveum.midpoint.eclipse.logviewer.parsing.Parser;

public class ContextNodeContent extends OutlineNodeContent {
	
	private String execWave, projWave;
	private String labelCore;
	private String labelSuffix;
	private int objectRulesTotal, objectRulesTriggered;
	private int zeroAssignments, plusAssignments, minusAssignments;
	
	public String getLabelCore() {
		return labelCore;
	}

	public void setLabelCore(String labelCore) {
		this.labelCore = labelCore;
	}

	public String getLabelSuffix() {
		return labelSuffix;
	}

	public void setLabelSuffix(String labelSuffix) {
		this.labelSuffix = labelSuffix;
	}

	@SuppressWarnings("unchecked")
	@Override
	public TreeNode createTreeNode(Parser parser) {
		List<TreeNode> children = new ArrayList<>();
		
		OutlineNode<ContextNodeContent> owner = (OutlineNode<ContextNodeContent>) getOwner();
		
		int projectionContextsCount = owner.getAllChildrenRecursive(ProjectionContextNodeContent.class).size();

		int mappingsCount = 0, executionsCount = 0;
		for (OutlineNode<?> item : owner.getAllChildrenRecursive()) {
			if (item.getContent() instanceof MappingNodeContent) {
				mappingsCount++;
			} else if (item.getContent() instanceof ExecutionNodeContent) {
				executionsCount++;
			}
		}
		
		String label = "Wave: " + execWave + " : " + projWave + 
				" - P:" + projectionContextsCount + ", M:" + mappingsCount +
				", R:" + objectRulesTriggered + "/" + objectRulesTotal + 
				", A:" + zeroAssignments + "=" + plusAssignments + "+" + minusAssignments + "-" +
				(executionsCount > 0 ? " # " : " - ") + 
				labelCore + labelSuffix;

		OutlineNode<ContextNodeContent> first = getFirstDump();
		OutlineNode<ContextNodeContent> previous = getPreviousDump();
		if (owner.getDate() != null && (previous != null || first != null)) {
			owner.setDelta(owner.computeDeltaSince(previous));
			owner.setSum(owner.computeDeltaSince(first));
		}
		
		TreeNode treeNode = new TreeNode(owner, label, owner.getRegion());

		// projection contexts first!
		for (OutlineNode<ProjectionContextNodeContent> node : owner.getAllChildren(ProjectionContextNodeContent.class)) {
			children.add(node.createTreeNode(parser));
		}
		for (OutlineNode<?> node : owner.getAllChildren()) {
			if (node.getContent() instanceof ProjectionContextNodeContent) {
				continue;
			}
			children.add(node.createTreeNode(parser));			
		}
		treeNode.addChildren(children);
		return treeNode;
	}

	@SuppressWarnings("unchecked")
	private OutlineNode<ContextNodeContent> getPreviousDump() {
		if (owner.getPreviousSibling() != null && owner.getPreviousSibling().getContent() instanceof ContextNodeContent) {
			return (OutlineNode<ContextNodeContent>) owner.getPreviousSibling();
		} else {
			return null;
		}
	}

	private OutlineNode<ContextNodeContent> getFirstDump() {
		OutlineNode<ContextNodeContent> prev = getPreviousDump();		
		if (prev == null) {
			return null;
		}
		OutlineNode<ContextNodeContent> last = prev;
		while (last.getContent().getPreviousDump() != null) {
			last = last.getContent().getPreviousDump();
		}
		return last;
	}

	// input like this: LensContext: state=SECONDARY, Wave(e=1,p=1,max=0), focus, 2 projections, 2 changes, fresh=true
	public void parseWaveInfo(String line) {
		projWave = "?";
		execWave = "?";
		
		int i = line.indexOf("Wave(e=");
		if (i < 0) {
			return;
		}
		
		int j = line.indexOf(",p=", i);
		if (j < 0) {
			return;
		}
		execWave = line.substring(i+7, j);
		
		int k = line.indexOf(",max", j);
		if (k < 0) {
			return;
		}
		projWave = line.substring(j+3, k);
	}

	// from: ---[ PROJECTOR (INITIAL) context after load ]--------------------------------
	// to: PROJECTOR (INITIAL) context after load 
	public void parseLabelCore(String line) {
		int i = line.indexOf(" ]---");
		if (i > 0) {
			line = line.substring(5, i);
		} else {
			line = line.substring(5);
		}
		setLabelCore(line);
	}

	public int getObjectRulesTotal() {
		return objectRulesTotal;
	}

	public void setObjectRulesTotal(int objectRulesTotal) {
		this.objectRulesTotal = objectRulesTotal;
	}

	public int getObjectRulesTriggered() {
		return objectRulesTriggered;
	}

	public void setObjectRulesTriggered(int objectRulesTriggered) {
		this.objectRulesTriggered = objectRulesTriggered;
	}

	public int getZeroAssignments() {
		return zeroAssignments;
	}

	public void setZeroAssignments(int zeroAssignments) {
		this.zeroAssignments = zeroAssignments;
	}

	public int getPlusAssignments() {
		return plusAssignments;
	}

	public void setPlusAssignments(int plusAssignments) {
		this.plusAssignments = plusAssignments;
	}

	public int getMinusAssignments() {
		return minusAssignments;
	}

	public void setMinusAssignments(int minusAssignments) {
		this.minusAssignments = minusAssignments;
	}
}
