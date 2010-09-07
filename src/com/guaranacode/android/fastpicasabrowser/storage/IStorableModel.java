package com.guaranacode.android.fastpicasabrowser.storage;

/**
 * Implemented by classes that can be stored in local storage.
 *
 * @author abe@guaranacode.com
 *
 */
public interface IStorableModel {
	
	/**
	 * The URL to the resource.
	 * @return
	 */
	String getUrl();
	
	/**
	 * Get the directory in the application's path where we store files.
	 * @return
	 */
	String getDir();
}
