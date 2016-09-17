package com.evolveum.midpoint.eclipse.ui.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.evolveum.midpoint.eclipse.ui.handlers.ResourceUtils;
import com.evolveum.midpoint.eclipse.ui.handlers.sources.SourceObject;
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

	public static String expand(String content, SourceObject sourceObject, ServerInfo server) {
		if (server == null) {
			return content;
		}
		Properties macros;
		if (StringUtils.isBlank(server.getPropertiesFile())) {
			macros = null;				// OK, no properties
		} else {
			macros = getMacros(server);
			if (macros == null) {
				return null;			// error reading file
			}
		}

        String patternString = "\\$\\((\\S*)\\)";

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
        	if (symbol.startsWith("$")) {
        		replacement = symbol;
        	} else if (symbol.startsWith("#")) {
        		replacement = getPredefined(symbol, sourceObject, server);
        		if (replacement == null) {
            		Console.logError("No value for predefined property '" + symbol + "')");
            	}
        	} else if (symbol.startsWith("@")) {
        		String filename = symbol.substring(1);
        		String expanded = expand(filename, sourceObject, server);
    			System.out.println("Filename: " + filename + ", expanded: " + expanded);
        		if (expanded != null) {
        			IPath path = new Path(expanded);
        			if (!path.isAbsolute() && sourceObject.getFile() != null) {
        				path = sourceObject.getFile().getFullPath().removeLastSegments(1).append(path);
        				System.out.println("Path made absolute: " + path);
        			}
        			IFile file = ResourceUtils.getFileForLogicalPath(path);
        			if (file == null) {
        				Console.logError("Couldn't locate the file: " + path);
        				replacement = null;
        			} else {
        				InputStream is = null;
        				try {
        					is = file.getContents();
        					replacement = IOUtils.toString(is, file.getCharset());
        				} catch (CoreException | IOException e) {
        					Console.logError("Couldn't read from file: " + path + ": " + e.getMessage(), e);
        					replacement = null;
        				} finally {
        					IOUtils.closeQuietly(is);
        				}
        			}
        		} else {
        			replacement = null;
        		}
        	} else {
        		if (macros == null) {
        			Console.logError("Unable to resolve symbol " + symbol + ", because replacement property file was not specified.");
        			replacement = null;
        		} else {
        			replacement = macros.getProperty(symbol);
        			if (replacement == null) {
        				Console.logError("No value for replacement property '" + symbol + "' in file " + server.getPropertiesFile());
        			}
        		}
        	}
        	if (replacement == null) {
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

	private static String getPredefined(String symbol, SourceObject sourceObject, ServerInfo server) {
		System.out.println("Resolving predefined symbol: " + symbol);
		switch (symbol) {
		case "#project.name":
		{
			IProject project = getProject(sourceObject);
			if (project == null) {
				return null;
			}
			return project.getName();
		}
		case "#project.dir":
		{
			IProject project = getProject(sourceObject);
			if (project == null) {
				return null;
			}
			IPath location = project.getLocation();
			if (location == null) {
				return null;
			}
			return location.toOSString();
		}
		case "#server.displayName":
		{
			return server != null ? server.getDisplayName() : null;
		}
		}
		return null;
	}

	private static IProject getProject(SourceObject sourceObject) {
		if (sourceObject == null || sourceObject.getFile() == null) {
			Console.logError("No source object file.");
			return null;
		}
		return sourceObject.getFile().getProject();
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
