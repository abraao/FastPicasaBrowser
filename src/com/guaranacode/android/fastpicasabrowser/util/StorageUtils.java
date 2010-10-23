package com.guaranacode.android.fastpicasabrowser.util;

import android.os.Environment;

/**
 * Storage related utilities.
 *
 * @author abe@guaranacode.com
 *
 */
public class StorageUtils {
	/**
	 * Returns true if the media is currently mounted and writable, false otherwise.
	 * @return
	 */
	public static boolean canWriteToExternalStorage() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}
	
	/**
	 * Returns true if media is currently mounted and readable.
	 * @return
	 */
	public static boolean canReadFromExternalStorage() {
		if(canWriteToExternalStorage()) {
			return true;
		}
		
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
	}
}
