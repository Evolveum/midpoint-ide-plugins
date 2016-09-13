package com.evolveum.midpoint.eclipse.ui.handlers.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.runtime.api.req.ServerAction;
import com.evolveum.midpoint.eclipse.ui.handlers.server.FileRequestHandler.RequestedAction;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.TextFragmentSource;

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


	public static ServerRequestPack fromTextFragment(String textFragment, IPath path, RequestedAction requestedAction) {
		List<ServerRequestItem> items = new ArrayList<>();
		List<SourceObject> objects = ServerRequestItem.parseTextFragment(textFragment, path, requestedAction); 
		for (SourceObject object : objects) {
			ServerAction action;
			switch (requestedAction) {
			case COMPARE: action = ServerAction.COMPARE; break;
			case EXECUTE_ACTION: action = ServerAction.EXECUTE; break;
			default: action = object.isUploadable() ? ServerAction.UPLOAD : ServerAction.EXECUTE;
			}
			items.add(new ServerRequestItem(action, object));
		}
		return new ServerRequestPack(items);
	}

	public void add(List<ServerRequestItem> items) {
		this.items.addAll(items);
	}

	@Override
	public String toString() {
		return String.valueOf(items);
	}

	public static ServerRequestPack fromPhysicalActionFile(String fileName, int actionNumber) {
		List<ServerRequestItem> items = ServerRequestItem.fromPhysicalActionFile(fileName, actionNumber);
		return new ServerRequestPack(items);
	}
	
	public static ServerRequestPack fromWorkspaceFiles(List<IFile> files, RequestedAction requestedAction) {
		List<ServerRequestItem> items = new ArrayList<>();
		for (IFile file : files) {
			List<SourceObject> objects = ServerRequestItem.parseWorkspaceFile(file, requestedAction); 
			for (SourceObject object : objects) {
				ServerAction action;
				switch (requestedAction) {
				case COMPARE: action = ServerAction.COMPARE; break;
				case EXECUTE_ACTION: action = ServerAction.EXECUTE; break;
				default: action = object.isUploadable() ? ServerAction.UPLOAD : ServerAction.EXECUTE;
				}
				items.add(new ServerRequestItem(action, object));
			}
		}
		return new ServerRequestPack(items);
	}
}
