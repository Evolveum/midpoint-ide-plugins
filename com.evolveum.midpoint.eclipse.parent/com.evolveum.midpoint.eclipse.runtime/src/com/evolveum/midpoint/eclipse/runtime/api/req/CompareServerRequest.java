package com.evolveum.midpoint.eclipse.runtime.api.req;

import java.util.List;

public class CompareServerRequest extends ServerRequest {
	
	boolean showLocalToRemote, showRemoteToLocal, showLocal, showRemote;
	List<String> ignoreItems;

	public CompareServerRequest(ServerAction action, String data) {
		super(action, data);
	}

	public boolean isShowLocalToRemote() {
		return showLocalToRemote;
	}

	public void setShowLocalToRemote(boolean showLocalToRemote) {
		this.showLocalToRemote = showLocalToRemote;
	}

	public boolean isShowRemoteToLocal() {
		return showRemoteToLocal;
	}

	public void setShowRemoteToLocal(boolean showRemoteToLocal) {
		this.showRemoteToLocal = showRemoteToLocal;
	}

	public boolean isShowLocal() {
		return showLocal;
	}

	public void setShowLocal(boolean showLocal) {
		this.showLocal = showLocal;
	}

	public boolean isShowRemote() {
		return showRemote;
	}

	public void setShowRemote(boolean showRemote) {
		this.showRemote = showRemote;
	}

	public List<String> getIgnoreItems() {
		return ignoreItems;
	}

	public void setIgnoreItems(List<String> ignoreItems) {
		this.ignoreItems = ignoreItems;
	}
	
	

}
