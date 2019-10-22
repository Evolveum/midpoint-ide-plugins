package com.evolveum.midpoint.eclipse.ui.tracer.views.lens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.ComplexTypeDefinition;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.ItemDefinition;
import com.evolveum.midpoint.prism.PrismContainer;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.exception.SchemaException;

public class PrismValueNode extends PrismNode {

	private final String label;
	private final List<? extends PrismValue> values;

	public PrismValueNode(String label, List<? extends PrismValue> values, PrismNode parent) throws SchemaException {
		super(parent);
		this.label = label;
		this.values = values;
		createChildren();
	}

	private void createChildren() throws SchemaException {
		List<ItemName> itemNames = getItemNames();
		for (ItemName itemName : itemNames) {
			List<Item<?,?>> items = values.stream()
					.map(value -> value instanceof PrismContainerValue ? (Item<?,?>) ((PrismContainerValue<?>) value).findItem(itemName) : null)
					.collect(Collectors.toList());
			Item<?,?> firstItem = items.stream().filter(i -> i != null).findFirst().orElse(null);
			if (firstItem != null) {
				PrismItemNode.create(itemName, items, this);
			}
		}
	}

	private List<ItemName> getItemNames() throws SchemaException {
		List<ItemName> rv = new ArrayList<>();
		PrismContainerValue<?> firstPcv = values.stream()
				.filter(v -> v instanceof PrismContainerValue)
				.map(v -> (PrismContainerValue<?>) v)
				.findFirst().orElse(null);
		if (firstPcv == null) {
			return Collections.emptyList();
		}
		
		ComplexTypeDefinition ctd = firstPcv.getComplexTypeDefinition();
		if (ctd != null) {
			@SuppressWarnings("rawtypes")
			List<? extends ItemDefinition> definitions = ctd.getDefinitions();
			definitions.stream().map(def -> def.getItemName()).forEach(name -> rv.add(name));
		}
		for (int i = 0; i < values.size(); i++) {
			PrismValue v = values.get(i);
			if (v instanceof PrismContainerValue) {
				for (QName name : ((PrismContainerValue<?>) v).getItemNames()) {
					ItemName itemName = ItemName.fromQName(name);
					if (!rv.contains(itemName)) {
						rv.add(itemName);
					}
				}
			}
		}
		return rv; 
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue(int i) {
		return Util.prettyPrint(values.get(i));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrismValueNode other = (PrismValueNode) obj;
		if (Objects.equals(label, other.getLabel()))
			return false;
//		if (values == null) {
//			if (other.values != null)
//				return false;
//		} else if (!values.equals(other.values))
//			return false;
		return true;
	}

	public static PrismValueNode create(String string, List<? extends PrismValue> values, PrismNode parent) throws SchemaException {
		return new PrismValueNode(string, values, parent);		
	}
}
