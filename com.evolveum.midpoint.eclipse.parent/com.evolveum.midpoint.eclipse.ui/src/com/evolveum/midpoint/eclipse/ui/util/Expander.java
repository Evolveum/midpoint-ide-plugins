package com.evolveum.midpoint.eclipse.ui.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.evolveum.midpoint.eclipse.ui.prefs.ServerInfo;

public class Expander {

	public static boolean checkPropertiesFile(ServerInfo server) {
		if (server == null) {
			return false;
		} else if (StringUtils.isBlank(server.getPropertiesFile())) {
			return true;
		} else {
			return getMacros(server) != null;
		}
	}

	public static String expand(String content, ServerInfo server) {
		if (server == null || StringUtils.isBlank(server.getPropertiesFile())) {
			return content;
		}
		Properties macros = getMacros(server);
		if (macros == null) {
			return null;
		}

        String patternString = "\\$\\{(\\S*)\\}";

        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(content);

        StringBuilder result = new StringBuilder();
        int lastCopied = 0;
        
        boolean missing = false;
        int replaced = 0;
        while(matcher.find()) {
        	result.append(content.substring(lastCopied, matcher.start()));
        	String symbol = content.substring(matcher.start()+2, matcher.end()-1);
        	String replacement;
        	if ("$".equals(symbol)) {
        		replacement = "$";
        	} else {
        		replacement = macros.getProperty(symbol);
        	}
        	if (replacement == null) {
        		Console.logError("No value for replacement property '" + symbol + "' in file " + server.getPropertiesFile());
        		missing = true;
        	} else {
        		result.append(replacement);
        		replaced++;
        	}
        	lastCopied = matcher.end();
        }
        result.append(content.substring(lastCopied));
        
        if (missing) {
        	Util.showAndLogError("Error", "Some of replacement properties couldn't be found. Please see the console window.");
        	return null;
        }
        System.out.println(replaced + " symbol(s) replaced.");
        System.out.println("Result: " + result);
        return result.toString();
	}

	public static Properties getMacros(ServerInfo server) {
		Properties macros = new Properties();
		FileReader reader = null;
		try {
			reader = new FileReader(server.getPropertiesFile());
			macros.load(reader);
			return macros;
		} catch (IOException e) {
			Util.showAndLogError("Error", "Couldn't read server properties file '" + server.getPropertiesFile() + "': " + e.getMessage(), e);
			return null;
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

}
