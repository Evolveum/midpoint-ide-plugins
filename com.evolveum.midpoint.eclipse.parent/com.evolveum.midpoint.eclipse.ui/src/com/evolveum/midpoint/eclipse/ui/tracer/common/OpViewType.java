package com.evolveum.midpoint.eclipse.ui.tracer.common;

import static com.evolveum.midpoint.eclipse.ui.tracer.common.OpType.CHANGE_EXECUTION_SUB;
import static com.evolveum.midpoint.eclipse.ui.tracer.common.OpType.CLOCKWORK_RUN;
import static com.evolveum.midpoint.eclipse.ui.tracer.common.OpType.FOCUS_LOAD;
import static com.evolveum.midpoint.eclipse.ui.tracer.common.OpType.MAPPING_EVALUATION;
import static com.evolveum.midpoint.eclipse.ui.tracer.common.OpType.SHADOW_LOAD;
import static java.util.Collections.emptySet;

import java.util.Arrays;
import java.util.Collection;

public enum OpViewType {
	
	ALL("All", null, null, true, true),
	OVERVIEW("Overview", Arrays.asList(CLOCKWORK_RUN, MAPPING_EVALUATION, CHANGE_EXECUTION_SUB, FOCUS_LOAD, SHADOW_LOAD), emptySet(), false, false),
	NONE("None", emptySet(), emptySet(), false, false);
	
	private final String label;
	private final Collection<OpType> types;
	private final Collection<PerformanceCategory> categories;
	private final boolean showAlsoParents;
	private final boolean showPerformanceColumns;
	
	private OpViewType(String label, Collection<OpType> types, Collection<PerformanceCategory> categories, boolean showAlsoParents, boolean showPerformanceColumns) {
		this.label = label;
		this.types = types;
		this.categories = categories;
		this.showAlsoParents = showAlsoParents;
		this.showPerformanceColumns = showPerformanceColumns;
	}

	public String getLabel() {
		return label;
	}

	public Collection<OpType> getTypes() {
		return types;
	}

	public Collection<PerformanceCategory> getCategories() {
		return categories;
	}

	public boolean isShowAlsoParents() {
		return showAlsoParents;
	}

	public boolean isShowPerformanceColumns() {
		return showPerformanceColumns;
	}

}
