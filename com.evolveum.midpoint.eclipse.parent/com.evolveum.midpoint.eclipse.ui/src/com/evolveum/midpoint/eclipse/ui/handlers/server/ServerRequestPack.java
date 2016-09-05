package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.runtime.api.ServerAction;

public class ServerRequestPack {
	
	public static final ServerRequestPack EMPTY = new ServerRequestPack(); 

	private List<ServerRequestItem> items = new ArrayList<>();
	
	public ServerRequestPack() {
	}
	
	public ServerRequestPack(ServerRequestItem item) {
		items.add(item);
	}

	public ServerRequestPack(List<ServerRequestItem> items) {
		this.items.addAll(items);
	}
	
	public int getItemCount() {
		return items.size();
	}

	public List<ServerRequestItem> getItems() {
		return items;
	}

	public boolean isEmpty() {
		return getItemCount() == 0;
	}

	public static ServerRequestPack fromWorkspaceFiles(List<IFile> files, ServerAction serverAction) {
		List<ServerRequestItem> items = new ArrayList<>();
		for (IFile file : files) {
			items.add(new ServerRequestItem(serverAction, new WorkspaceFileServerRequestSource(file)));
		}
		return new ServerRequestPack(items);
	}

	public static ServerRequestPack fromTextFragment(String textFragment, IPath path, ServerAction serverAction) {
		ServerRequestItem item = new ServerRequestItem(serverAction, new TextFragmentServerRequestSource(textFragment, path));
		return new ServerRequestPack(item);
	}

	public void add(List<ServerRequestItem> items) {
		this.items.addAll(items);
	}

	@Override
	public String toString() {
		return String.valueOf(items);
	}

}
