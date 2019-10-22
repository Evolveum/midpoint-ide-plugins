package com.evolveum.midpoint.eclipse.ui.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.eclipse.ui.tracer.views.lens.Util;
import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.exception.SchemaException;

public class PrismValueNode extends Node {

	private final String label;
	private final PrismValue value;

	public PrismValueNode(String label, PrismValue value, Node parent) throws SchemaException {
		super(parent);
		this.label = label;
		this.value = value;
		createChildren();
	}

	private void createChildren() throws SchemaException {
		if (!(value instanceof PrismContainerValue)) {
			return;
		}
		PrismContainerValue<?> pcv = (PrismContainerValue<?>) value;
		List<ItemName> itemNames = getItemNames();
		for (ItemName itemName : itemNames) {
			Item<?,?> item = pcv.findItem(itemName);
			if (item != null) {
				ItemNode.create(item, this);
			}
		}
	}

	private List<ItemName> getItemNames() throws SchemaException {
		List<ItemName> rv = new ArrayList<>();
		PrismContainerValue<?> pcv = (PrismContainerValue<?>) value;
		ComplexTypeDefinition ctd = pcv.getComplexTypeDefinition();
		if (ctd != null) {
			@SuppressWarnings("rawtypes")
			List<? extends ItemDefinition> definitions = ctd.getDefinitions();
			definitions.stream().map(def -> def.getItemName()).forEach(name -> rv.add(name));
		}
		for (QName name : pcv.getItemNames()) {
			ItemName itemName = ItemName.fromQName(name);
			if (!rv.contains(itemName)) {
				rv.add(itemName);
			}
		}
		return rv; 
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue() {
		return Util.prettyPrint(value);
	}

	public static PrismValueNode create(String string, PrismValue value, Node parent) throws SchemaException {
		return new PrismValueNode(string, value, parent);		
	}

	@Override
	public Object getObject() {
		return value;
	}
}
