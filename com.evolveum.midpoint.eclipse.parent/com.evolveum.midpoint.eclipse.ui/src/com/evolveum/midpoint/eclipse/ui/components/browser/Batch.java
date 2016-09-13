package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.ArrayList;
import java.util.List;

import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;

public class Batch {
	
	private List<ServerObject> objects = new ArrayList<>();
	private int first;

	public List<ServerObject> getObjects() {
		return objects;
	}
	
	public void setFirst(int first) {
		this.first = first;
	}

	public int getFirst() {
		return first;
	}

	public int getLast() {
		return first + objects.size() - 1;
	}
 
}
