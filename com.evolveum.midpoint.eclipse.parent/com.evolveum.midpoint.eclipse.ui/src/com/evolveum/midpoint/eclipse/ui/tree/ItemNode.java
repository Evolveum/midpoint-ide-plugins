package com.evolveum.midpoint.eclipse.ui.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import com.evolveum.midpoint.eclipse.ui.tracer.views.lens.Util;
import com.evolveum.midpoint.prism.Item;
import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismValue;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensElementContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

public class ItemNode extends Node {

	private final Item<?,?> item;

	public ItemNode(Item<?,?> item, Node parent) throws SchemaException {
		super(parent);
		Validate.notNull(item);
		this.item = item;
		createChildren();
	}

	@Override
	public String getLabel() {
		return item.getDefinition() != null ? item.getDefinition().getItemName().getLocalPart() : item.getElementName().getLocalPart();
	}

	@Override
	public String getValue() {
		return Util.prettyPrint(item);
	}
	
    private void createChildren() throws SchemaException {
		if (item.getValues().size() > 1) {
			for (int i = 0; i < item.getValues().size(); i++) {
				int index = i;
				PrismValueNode.create("#" + i, item.getValues().get(i), this);
			}
		} else if (item.getValues().size() == 1) {
			Node dummyRoot = PrismValueNode.create("dummy", item.getValues().get(0), null);
			for (Node child : dummyRoot.getChildren()) {
				child.parent = this;
				children.add(child);
			}
		}				
    }

	public static ItemNode create(Item<?, ?> item, Node parent) throws SchemaException {
		return new ItemNode(item, parent);
	}

	@Override
	public Object getObject() {
		return item;
	}
}
