package com.evolveum.midpoint.eclipse.ui.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.evolveum.midpoint.eclipse.ui.prefs.MidPointPreferencePage;
import com.evolveum.midpoint.eclipse.ui.util.Util;

public class ResourceUtils {

	public static IPath determineRoot(IPath source, String rootSpec) {
		if (MidPointPreferencePage.VALUE_CURRENT_DIRECTORY.equals(rootSpec)) {
			return goUp(source, 1);
		} else if (MidPointPreferencePage.VALUE_CURRENT_DIRECTORY_PLUS_1.equals(rootSpec)) {
			return goUp(source, 2);
		} else if (MidPointPreferencePage.VALUE_CURRENT_DIRECTORY_PLUS_2.equals(rootSpec)) {
			return goUp(source, 3);
		} else if (MidPointPreferencePage.VALUE_CURRENT_DIRECTORY_PLUS_3.equals(rootSpec)) {
			return goUp(source, 4);
		} 
		int keep;
		if (StringUtils.isBlank(rootSpec) || MidPointPreferencePage.VALUE_CURRENT_PROJECT.equals(rootSpec)) {
			keep = 1;
		} else if (MidPointPreferencePage.VALUE_CURRENT_PROJECT_MINUS_1.equals(rootSpec)) {
			keep = 2;
		} else if (MidPointPreferencePage.VALUE_CURRENT_PROJECT_MINUS_2.equals(rootSpec)) {
			keep = 3;
		} else if (MidPointPreferencePage.VALUE_CURRENT_PROJECT_MINUS_3.equals(rootSpec)) {
			keep = 4;
		} else {
			throw new IllegalStateException("Invalid root specification: " + rootSpec);
		}
		if (source.segmentCount() <= keep) {
			return source;
		} else {
			return source.uptoSegment(keep);
		}
	}

	public static IPath goUp(IPath source, int levels) {
		while (source.segmentCount() > 1 && levels > 0) {
			source = source.removeLastSegments(1);
			levels--;
		}
		return source;
	}

	public static void prepare(IContainer container) throws CoreException {
		if (!(container instanceof IFolder)) {
			return;
		}
		IFolder folder = (IFolder) container;
	    if (!folder.exists()) {
	        prepare(folder.getParent());
	        folder.create(true, true, null);
	    }
	}

	public static IFile createOutputFile(IFile file, byte[] content) {
		if (file == null || content == null) {
			return null;
		}
		try {
			InputStream source = new ByteArrayInputStream(content);
			prepare(file.getParent());
			file.create(source, true, null);
			return file;
		} catch (CoreException e) {
			Util.showAndLogError("Error creating output file", "Output file " + file.getFullPath() + "couldn't be created: " + e, e);
			return null;
		}
	}

	public static IFile createOutputFile(IFile file, String content) {
		return createOutputFile(file, content != null ? content.getBytes() : null);
	}

	public static void createParentFolders(IContainer container) throws CoreException {
		if (!(container instanceof IFolder)) {
			return;
		}
		IFolder folder = (IFolder) container;
	    if (!folder.exists()) {
	        createParentFolders(folder.getParent());
	        folder.create(true, true, null);
	    }
	}

	public static IFile getFileForPhysicalPath(IPath path) {
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}

	public static IFile getFileForLogicalPath(IPath path) {
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}

	public static String formatActionCounter(int counter) {
		return String.format("%05d", counter);
	}

	public static String fixComponent(String s) {
		if (s == null) {
			return null;
		}
		return s
				.replace('<', '_')
				.replace('>', '_')
				.replace(':', '_')
				.replace('"', '_')
				.replace('\'', '_')
				.replace('/', '_')
				.replace('\\', '_')
				.replace('|', '_')
				.replace('?', '_')
				.replace('*', '_');
	}

}
