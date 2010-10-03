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
	
	/**
	 * Deletes a file or a directory and everything in it.
	 * @param path
	 */
	public static void deleteFile(String path) {
		File dir = new File(path);
		deleteFile(dir);
	}
	
	/**
	 * Deletes a file or a directory and everything in it.
	 * @param file
	 */
	public static void deleteFile(File file) {
		if((null == file)) {
			return;
		}
		
		if(file.isFile()) {
			file.delete();
		}
		else if(file.isDirectory()) {
			for(String child : file.list()) {
				deleteFile(file.getAbsolutePath() + "/" + child);
			}
			
			file.delete();
		}
	}
}
