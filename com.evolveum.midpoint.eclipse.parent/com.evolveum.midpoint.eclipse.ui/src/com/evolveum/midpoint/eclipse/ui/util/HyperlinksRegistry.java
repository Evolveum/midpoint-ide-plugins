package com.evolveum.midpoint.eclipse.ui.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

public class HyperlinksRegistry {
	
	// TODO fix this ugly approach
	private static HyperlinksRegistry instance;
	
	public static HyperlinksRegistry getInstance() {
		if (instance == null) {
			instance = new HyperlinksRegistry();
		}
		return instance;
	}

	public class Entry {
		public final List<String> labels;
		public final List<IFile> files;
		public final List<String> editorIds;
		public Entry(List<String> labels, List<IFile> files, List<String> editorIds) {
			this.labels = labels;
			this.files = files;
			this.editorIds = editorIds;
		}
	}
	
	public final Map<String,Entry> entries = new HashMap<>();
	
	public synchronized Entry get(String counter) {
		Entry entry = entries.get(counter);
		entries.remove(counter);
		return entry;
	}
	
	public void registerEntry(String counter, List<String> labels, List<IFile> files, List<String> editorIds) {
		entries.put(counter, new Entry(labels, files, editorIds));
	}
	
}
