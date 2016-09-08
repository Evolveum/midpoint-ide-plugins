package com.evolveum.midpoint.eclipse.ui.menus;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;

public class MainMenuContributor extends CompoundContributionItem implements IWorkbenchContribution {

	private IServiceLocator serviceLocator;

	public MainMenuContributor() {
	}

	public MainMenuContributor(String id) {
		super(id);
	}

	@Override
	protected IContributionItem[] getContributionItems() {
		List<IContributionItem> items = new ArrayList<>();
		addPopupMenu(items);
		return items.toArray(new IContributionItem[0]);
	}

	public void addPopupMenu(List<IContributionItem> items) {
		boolean serverSelected = PluginPreferences.isServerSelected();
		System.out.println("addPopupMenu; serverSelected = " + serverSelected);
		if (serverSelected) {
			
			MenuUtil.addServerNameLabel(items, serviceLocator);

			items.add(new Separator());

			MenuUtil.addUploadOrExecute(items, serviceLocator);
			MenuUtil.addUploadOrExecuteWithAction(items, serviceLocator);
			MenuUtil.addDownload(items, serviceLocator);
			MenuUtil.addReloadFromServer(items, serviceLocator);
			MenuUtil.addComputeDifferences(items, serviceLocator);		
			
			
			List<IContributionItem> actionItems = new ArrayList<>();
			MenuUtil.addExecuteAction(actionItems, serviceLocator, 1);
			MenuUtil.addExecuteAction(actionItems, serviceLocator, 2);
			MenuUtil.addExecuteAction(actionItems, serviceLocator, 3);
			
			if (!actionItems.isEmpty()) {
				items.add(new Separator());
				items.addAll(actionItems);
			}
			
			List<IContributionItem> setAsActionItems = new ArrayList<>();
			MenuUtil.addSetAsAction(setAsActionItems, serviceLocator, 1);
			MenuUtil.addSetAsAction(setAsActionItems, serviceLocator, 2);
			MenuUtil.addSetAsAction(setAsActionItems, serviceLocator, 3);
			if (!setAsActionItems.isEmpty()) {
				items.add(new Separator());
				items.addAll(setAsActionItems);
			}
			
			items.add(new Separator());

			MenuUtil.addTestConnections(items, serviceLocator);

			items.add(new Separator());
			
			MenuUtil.addShowConsole(items, serviceLocator);
			MenuUtil.addEditPreferences(items, serviceLocator);
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
