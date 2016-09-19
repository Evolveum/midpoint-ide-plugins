package com.evolveum.midpoint.eclipse.ui.handlers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang.StringUtils;

import com.evolveum.midpoint.eclipse.ui.prefs.PluginPreferences;
import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;
import com.evolveum.midpoint.eclipse.ui.util.Console;

public class ServerLogUtils {

	public static Long getLogPosition() {
		String filename = PluginPreferences.getLogfile();
		if (StringUtils.isEmpty(filename)) {
			return null;
		}
		File file = new File(filename);
		return file.length();
		
	}

	public static LogFileFragment getLogFileFragment(String logfilename, Long logPosition, boolean fromEnd) {
		if (StringUtils.isBlank(logfilename)) {
			return new LogFileFragment("Path to midPoint server log file (idm.log) is not specified. You can use Preferences page to enter it."); 
		}
		if (logPosition == null) {
			return new LogFileFragment("Log file (idm.log) couldn't be read.");
		}
		try {
			RandomAccessFile log = new RandomAccessFile(logfilename, "r");
			long currentLength = log.length();
			if (fromEnd) {
				//System.out.println("logPosition = " + logPosition + ", currentLength = " + currentLength);
				logPosition = currentLength - logPosition;
				if (logPosition < 0) {
					logPosition = 0L;
				}
			}
			//System.out.println("Seeking to " + logPosition);
			log.seek(logPosition);
			byte[] buffer = new byte[10240];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int n;
			while ((n=log.read(buffer)) >= 0) {
				baos.write(buffer, 0, n);
			}
			log.close();
			baos.close();
			return new LogFileFragment(baos.toByteArray(), currentLength);
		} catch (IOException e) {
			Console.logError("Couldn't read from midPoint log", e);
			return null;
		}
	}
	
	public static class LogFileFragment {
		public final byte[] content;
		public final long currentLogSize;
		
		public LogFileFragment(String fixedText) {
			content = fixedText.getBytes();
			currentLogSize = content.length;
		}
		
		public LogFileFragment(byte[] fragment, long currentLogSize) {
			this.content = fragment;
			this.currentLogSize = currentLogSize;
		}
		
	}

	public static LogFileFragment getLogFileFragment(ServerInfo server, long start) {
		return getLogFileFragment(server.getLogFile(), start, false);
	}

	public static LogFileFragment getLogFileFragment(ServerInfo server, long start, boolean fromEnd) {
		return getLogFileFragment(server.getLogFile(), start, fromEnd);
	}

	public static void clearLog(ServerInfo server) {
		if (server == null || StringUtils.isEmpty(server.getLogFile())) {
			return;
		}
		try {
			RandomAccessFile log = new RandomAccessFile(server.getLogFile(), "rw");
			log.setLength(0);
			log.close();
			Console.logMinor("Server log cleared.");
		} catch (IOException e) {
			Console.logError("Couldn't clear server log file: " + e.getMessage(), e);
		}
	}

}
