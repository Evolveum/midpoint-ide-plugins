package com.evolveum.midpoint.eclipse.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

import com.evolveum.midpoint.eclipse.ui.handlers.server.DownloadHandler;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler;
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
			String serverName = PluginPreferences.getSelectedServerName();
			items.add(new ContributionItem() {
				@Override
				public void fill(Menu menu, int index) {
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setEnabled(false);
					item.setText("Selected server: " + serverName);
				}

				@Override
				public boolean isEnabled() {
					return false;
				}
			});
			
			items.add(new Separator());
			
			items.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
							serviceLocator, null, FileRequestHandler.CMD_UPLOAD_OR_EXECUTE, null, 
							null, null, null, 
							"Upload/execute", 
							null, null, CommandContributionItem.STYLE_PUSH, null, true)));
			
			Map<String,String> uploadActionParameters = new HashMap<>();
			uploadActionParameters.put(FileRequestHandler.PARAM_WITH_ACTION, "true");
			items.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
							serviceLocator, null, FileRequestHandler.CMD_UPLOAD_OR_EXECUTE, uploadActionParameters, 
							null, null, null, 
							"Upload/execute with action (midPoint server " + serverName + ")", 
							null, null, CommandContributionItem.STYLE_PUSH, null, true))); 
			
			items.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
							serviceLocator, null, DownloadHandler.CMD_DOWNLOAD, null, 
							null, null, null, 
							"Download from midPoint server " + serverName, 
							null, null, CommandContributionItem.STYLE_PUSH, null, true)));
			
			items.add(new CommandContributionItem( 
					new CommandContributionItemParameter(
							serviceLocator, null, FileRequestHandler.CMD_COMPUTE_DIFFERENCE, null, 
							null, null, null, 
							"Compute difference with midPoint server " + serverName, 
							null, null, CommandContributionItem.STYLE_PUSH, null, true)));
			
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
