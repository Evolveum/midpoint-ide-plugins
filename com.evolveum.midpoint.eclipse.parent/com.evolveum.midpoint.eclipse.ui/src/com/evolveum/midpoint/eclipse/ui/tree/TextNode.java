package com.evolveum.midpoint.eclipse.ui.tree;

public class TextNode extends Node {

	private final String label, value;
	
	public TextNode(String label, String value, Node parent) {
		super(parent);
		this.label = label;
		this.value = value;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue() {
		return value;
	}

	public static TextNode create(String label, Object value, Node parent) {
		return new TextNode(label, value != null ? value.toString() : "", parent);
	}
}
