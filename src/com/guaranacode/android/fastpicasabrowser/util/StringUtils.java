package com.guaranacode.android.fastpicasabrowser.util;

/**
 * String utilities.
 *
 * @author abe@guaranacode.com
 *
 */
public class StringUtils {

	/**
	 * Returns true if the string is null or contains only whitespace.
	 * 
	 * @param string
	 * @return
	 */
	public static boolean isNullOrEmpty(String string) {
		return (null == string) || (string.trim().length() == 0);
	}
}
