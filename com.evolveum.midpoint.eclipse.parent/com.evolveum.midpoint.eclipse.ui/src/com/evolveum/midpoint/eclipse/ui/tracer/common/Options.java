package com.evolveum.midpoint.eclipse.ui.tracer.common;

import java.util.HashSet;
import java.util.Set;

public class Options {
	private final Set<OpType> typesToShow = new HashSet<>();
	private final Set<PerformanceCategory> categoriesToShow = new HashSet<>();
	private boolean showAlsoParents;
	private boolean showPerformanceColumns;
	
	public boolean isShowAlsoParents() {
		return showAlsoParents;
	}
	public void setShowAlsoParents(boolean showAlsoParents) {
		this.showAlsoParents = showAlsoParents;
	}
	public Set<OpType> getTypesToShow() {
		return typesToShow;
	}
	public Set<PerformanceCategory> getCategoriesToShow() {
		return categoriesToShow;
	}
	public boolean isShowPerformanceColumns() {
		return showPerformanceColumns;
	}
	public void setShowPerformanceColumns(boolean showPerformanceColumns) {
		this.showPerformanceColumns = showPerformanceColumns;
	}
}
