package com.evolveum.midpoint.eclipse.ui.tracer.views.lens;

import java.util.List;
import java.util.stream.Collectors;

import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.prism.path.ItemName;
import com.evolveum.midpoint.util.exception.SchemaException;

public class PrismItemNode extends PrismNode {

	private ItemName itemName;
	private List<Item<?,?>> items;
	
	public PrismItemNode(ItemName itemName, List<Item<?,?>> items, PrismNode parent) throws SchemaException {
		super(parent);
		this.itemName = itemName;
		this.items = items;
		createChildren();
	}

	@Override
	public String getLabel() {
		return itemName.getLocalPart();
	}


	@Override
	public String getValue(int i) {
		return Util.prettyPrint(items.get(i));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
//		result = prime * result + ((itemName == null) ? 0 : itemName.hashCode());
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
		PrismItemNode other = (PrismItemNode) obj;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
			return false;
//		if (items == null) {
//			if (other.items != null)
//				return false;
//		} else if (!items.equals(other.items))
//			return false;
		return true;
	}

	public static PrismItemNode create(ItemName itemName, List<Item<?, ?>> items, PrismNode parent) throws SchemaException {
		return new PrismItemNode(itemName, items, parent);		
	}

    private void createChildren() throws SchemaException {
		int maxValues = items.stream().filter(item -> item != null).mapToInt(item -> item.size()).max().orElse(0);
		if (maxValues > 1) {
			for (int i = 0; i < maxValues; i++) {
				int index = i;
				List<PrismValue> values = items.stream().map(item -> getValue(item, index)).collect(Collectors.toList());
				PrismValueNode.create("#" + i, values, this);
			}
		} else if (maxValues == 1) {
			List<PrismValue> values = items.stream().map(item -> getValue(item, 0)).collect(Collectors.toList());
			PrismNode dummyRoot = PrismValueNode.create("dummy", values, null);
			for (PrismNode child : dummyRoot.getChildren()) {
				child.parent = this;
				children.add(child);
			}
		}				
    }
	
	private PrismValue getValue(Item<?, ?> item, int index) {
		return item != null && index < item.size() ? item.getValues().get(index) : null;
	}
}
