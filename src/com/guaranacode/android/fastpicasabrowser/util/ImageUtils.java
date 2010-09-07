package com.guaranacode.android.fastpicasabrowser.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Utilities for working with images.
 *
 * @author abe@guaranacode.com
 *
 */
public class ImageUtils {
	
	/**
	 * Download a bitmap from an URL.
	 * 
	 * @param url
	 */
	public static Bitmap downloadBitmap(String url) {
	    Bitmap bitmap = null;
	    InputStream in = null;

	    try {
	    	in = new URL(url).openStream();
	        bitmap = BitmapFactory.decodeStream(in);
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	    	if(null != in) {
	    		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    }

	    return bitmap;
	}
}
