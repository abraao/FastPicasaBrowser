package com.guaranacode.android.fastpicasabrowser.util;

import java.io.File;

/**
 * Utilities for dealing with files and directories.
 *
 * @author abe@guaranacode.com
 *
 */
public class FileUtils {
	
	/**
	 * Creates all directories in the specified path if they don't already exist. Returns
	 * true if successful, false otherwise.
	 * @param path
	 * @return
	 */
	public static boolean createPath(String path) {
		File dirs = new File(path);
		return dirs.mkdirs();
	}
}
