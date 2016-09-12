package com.evolveum.midpoint.eclipse.ui.components.browser;

import java.util.List;

import com.evolveum.midpoint.eclipse.runtime.api.resp.ServerObject;

public class Batch {
	
	private List<ServerObject> objects;
	private int first, last;

	public Batch(List<ServerObject> objects, int first, int last) {
		this.objects = objects.subList(first, last+1);
		this.first = first;
		this.last = last;
	}

	public List<ServerObject> getObjects() {
		return objects;
	}

	public int getFirst() {
		return first;
	}

	public int getLast() {
		return last;
	}
	
	
	
	
 
}
