package com.evolveum.midpoint.eclipse.ui.components.lensContextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensElementContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensFocusContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.LensProjectionContextType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowDiscriminatorType;

public class LensContextNode extends PrismNode {

	private final LensContextType lensContext;
	private final String label;
	
	public LensContextNode(String label, LensContextType lensContext, PrismNode parent) {
		super(parent);
		this.lensContext = lensContext;
		this.label = label;
		try {
			createChildren();
		} catch (SchemaException e) {
			e.printStackTrace();
			label += " " + e.getMessage();
		}
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue(int i) {
		return "";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
//		result = prime * result + ((objects == null) ? 0 : objects.hashCode());
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
		LensContextNode other = (LensContextNode) obj;
		return Objects.equals(getLabel(), other.getLabel());
//		if (objects == null) {
//			if (other.objects != null)
//				return false;
//		} else if (!objects.equals(other.objects))
//			return false;
//		return true;
	}

	private void createChildren() throws SchemaException {
		if (lensContext != null) {
			LensElementContextNode.create(lensContext.getFocusContext(), this);
			for (LensProjectionContextType pctx : lensContext.getProjectionContext()) {
				LensElementContextNode.create(pctx, this);
			}
		}
	}

}
