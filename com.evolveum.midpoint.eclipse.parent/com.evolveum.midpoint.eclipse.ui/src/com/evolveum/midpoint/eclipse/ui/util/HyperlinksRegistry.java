package com.evolveum.midpoint.eclipse.ui.util;

import java.util.PriorityQueue;

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

	public class Entry implements Comparable<Entry> {
		String line;
		int lineOffset;
		IFile logFile, dataFile, consoleFile, resultFile;
		public Entry(String line, int lineOffset, IFile logFile, IFile dataFile, IFile consoleFile, IFile resultFile) {
			super();
			this.line = line;
			this.lineOffset = lineOffset;
			this.logFile = logFile;
			this.dataFile = dataFile;
			this.consoleFile = consoleFile;
			this.resultFile = resultFile;
		}
		@Override
		public int compareTo(Entry o) {
			return Integer.compare(this.lineOffset, o.lineOffset);
		}
		public String getLine() {
			return line;
		}
		public int getLineOffset() {
			return lineOffset;
		}
		public IFile getLogFile() {
			return logFile;
		}
		public IFile getDataFile() {
			return dataFile;
		}
		public IFile getConsoleFile() {
			return consoleFile;
		}
		public IFile getResultFile() {
			return resultFile;
		}
		@Override
		public String toString() {
			return "Entry [lineOffset=" + lineOffset + ", logFile=" + logFile + "]";
		}
	}
	
	public final PriorityQueue<Entry> entries = new PriorityQueue<>();
	
	public synchronized void registerEntry(String line, int lineOffset, IFile logFile, IFile dataFile, IFile consoleFile, IFile resultFile) {
		entries.add(new Entry(line, lineOffset, logFile, dataFile, consoleFile, resultFile));
	}
	
	public synchronized Entry poll() {
		return entries.poll();
	}
	
	public synchronized Entry peek() {
		return entries.peek();
	}
	
}
