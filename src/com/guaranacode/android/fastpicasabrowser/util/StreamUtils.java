package com.guaranacode.android.fastpicasabrowser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utilities for working with streams.
 *
 * @author abe@guaranacode.com
 *
 */
public class StreamUtils {
	
	/**
	 * Reads a input stream to a string. Useful for reading the gzipped output
	 * of the Picasa API into a string.
	 * 
	 * @param inputStream
	 * @return
	 */
	public static String inputStreamToString(InputStream inputStream) {
		StringBuilder sb = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		String line;
		while(true) {
			try {
				line = reader.readLine();
			} catch (IOException e) {
				break;
			}
			
			if(line == null) {
				break;
			}
			
			sb.append(line);
		}
		
		return sb.toString();
	}
}
