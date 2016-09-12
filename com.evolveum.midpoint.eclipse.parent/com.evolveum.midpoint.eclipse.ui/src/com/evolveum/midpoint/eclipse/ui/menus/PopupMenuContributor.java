package com.evolveum.midpoint.eclipse.ui.menus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;

public class PopupMenuContributor extends CompoundContributionItem implements IWorkbenchContribution {

	private IServiceLocator serviceLocator;

	public PopupMenuContributor() {
	}

	public PopupMenuContributor(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> items = new ArrayList<>();
		addPopupMenu(items);
		return items.toArray(new IContributionItem[0]);
	}

	public void addPopupMenu(List<IContributionItem> items) {
		items.add(new Separator());
		
		MenuUtil.addServerNameLabel(items, serviceLocator);

		if (PluginPreferences.isServerSelected()) {

			MenuUtil.addBrowse(items, serviceLocator);
			MenuUtil.addTransferMenu(items, serviceLocator);
			
			items.add(new Separator());
		} 
	}

	@Override
	public void initialize(IServiceLocator serviceLocator) {
		this.serviceLocator = serviceLocator;
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
