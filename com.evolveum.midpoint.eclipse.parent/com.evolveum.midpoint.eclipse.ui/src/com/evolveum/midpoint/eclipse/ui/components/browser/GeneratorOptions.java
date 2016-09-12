package com.evolveum.midpoint.eclipse.ui.components.browser;

public class GeneratorOptions {
	
	private boolean symbolicReferences;
	private boolean wrapActions;
	private boolean createSuspended;
	private boolean raw;
	private boolean dryRun;
	
	public boolean isSymbolicReferences() {
		return symbolicReferences;
	}
	public void setSymbolicReferences(boolean symbolicReferences) {
		this.symbolicReferences = symbolicReferences;
	}
	public boolean isWrapActions() {
		return wrapActions;
	}
	public void setWrapActions(boolean wrapActions) {
		this.wrapActions = wrapActions;
	}
	public boolean isCreateSuspended() {
		return createSuspended;
	}
	public void setCreateSuspended(boolean createSuspended) {
		this.createSuspended = createSuspended;
	}
	public boolean isRaw() {
		return raw;
	}
	public void setRaw(boolean raw) {
		this.raw = raw;
	}
	public boolean isDryRun() {
		return dryRun;
	}
	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}
	
	

}
