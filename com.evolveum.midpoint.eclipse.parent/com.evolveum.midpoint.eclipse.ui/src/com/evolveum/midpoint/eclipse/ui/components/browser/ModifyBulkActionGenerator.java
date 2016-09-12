package com.evolveum.midpoint.eclipse.ui.components.browser;

public class ModifyBulkActionGenerator extends BulkActionGenerator {

	public ModifyBulkActionGenerator() {
		super("modify");
	}

	@Override
	public boolean supportsRawOption() {
		return true;
	}

}
