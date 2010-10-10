package com.guaranacode.android.fastpicasabrowser.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.ImageView;

import com.guaranacode.android.fastpicasabrowser.tasks.DownloadImageTask;
import com.guaranacode.android.fastpicasabrowser.util.FileUtils;
import com.guaranacode.android.fastpicasabrowser.util.StringUtils;

/**
 * Image downloading and storage.
 *
 * @author abe@guaranacode.com
 *
 */
public class ImageStorage {

	private static String APP_STORAGE_PATH = "com.guaranacode.fastpicasabrowser/files";
	
	private static String TEMP_PHOTO_DIR = "temp_photos";
	
	public static String getLocalPathForThumbnail(IStorableModel model, boolean includeFilename) {
		String relativePath = APP_STORAGE_PATH + "/" + model.getDir();
		
		return getLocalPathForImage(model.getUrl(), relativePath, includeFilename);
	}
	
	public static String getFilenameFromUrl(String imageUrl) {
		if(StringUtils.isNullOrEmpty(imageUrl)) {
			return null;
		}
		
		String filename = null;
		
		int lastSlashIdx = imageUrl.lastIndexOf('/');
		if(lastSlashIdx > 0) {
			filename = imageUrl.substring(lastSlashIdx + 1);
		}
		
		return filename;
	}
	
	private static Bitmap getFromLocalStorage(IStorableModel model) {
		if(null == model) {
			return null;
		}
		
		Bitmap thumbnail = null;
		
		String localPath = getLocalPathForThumbnail(model, true);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inInputShareable = false;
		
		try {
			FileInputStream fin = new FileInputStream(localPath);
			thumbnail = BitmapFactory.decodeStream(fin, null, options);
			fin.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			thumbnail = null;
		} catch (IOException e) {
			e.printStackTrace();
			thumbnail = null;
		}
		
		return thumbnail;
	}
	
	private static String getLocalPathForImage(String imageUrl, String relativePath, boolean includeFilename) {
		if(StringUtils.isNullOrEmpty(imageUrl)) {
			return null;
		}
		
		String sdDirPath = getSdDirPath();
		
		if(StringUtils.isNullOrEmpty(sdDirPath)) {
			return null;
		}
		
		String filename = getFilenameFromUrl(imageUrl);
		
		String localImagePath;
		
		if(includeFilename) {
			if(StringUtils.isNullOrEmpty(filename)) {
				return null;
			}
			
			localImagePath = String.format(
					"%s/%s/%s",
					sdDirPath,
					relativePath,
					filename);
		} else {
			localImagePath = String.format(
					"%s/%s/",
					sdDirPath,
					relativePath);
		}
		
		return localImagePath;
	}

	/**
	 * Get the path to the sd card.
	 * @return
	 */
	private static String getSdDirPath() {
		String sdDirPath;
		File sdDir = Environment.getExternalStorageDirectory();
		
		if(null == sdDir) {
			return null;
		}
		
		try {
			sdDirPath = sdDir.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			sdDirPath = null;
		}

		return sdDirPath;
	}

	private static boolean storeLocally(IStorableModel model, Bitmap thumbnail) {
		String localPath = getLocalPathForThumbnail(model, true);
		String localDir = getLocalPathForThumbnail(model, false);
		
		if(StringUtils.isNullOrEmpty(localPath)) {
			return false;
		}
		
		FileUtils.createPath(localDir);
		
		FileOutputStream fout = null;
		
		try {	
			fout = new FileOutputStream(localPath);
			
			thumbnail.compress(Bitmap.CompressFormat.JPEG, 80, fout);
			fout.flush();
			fout.close();
			
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static File storeBitmapLocally(String url, Bitmap bitmap) {
		if(StringUtils.isNullOrEmpty(url)) {
			return null;
		}
		
		if(null == bitmap) {
			return null;
		}
		
		String relativePath = APP_STORAGE_PATH + "/" + TEMP_PHOTO_DIR;
		String localDir = getLocalPathForImage(url, relativePath, false);
		String localPath = localDir + "temp_photo.jpg";
		
		FileUtils.createPath(localDir);
		
		FileOutputStream fout = null;
		
		try {	
			fout = new FileOutputStream(localPath);
			
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fout);
			fout.flush();
			fout.close();
			
			return new File(localPath);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Download a bitmap from an URL.
	 * 
	 * @param url
	 */
	public static Bitmap downloadBitmap(String url, boolean resizeBitmap) {
	    Bitmap bitmap = null;
	    InputStream in = null;

	    try {
	    	in = new URL(url).openStream();
	        bitmap = resizeThumbnail(in);
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
	
	/**
	 * Resizes a thumbnail to take up less memory.
	 * @param bitmapStream
	 * @return
	 */
	private static Bitmap resizeThumbnail(InputStream bitmapStream) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		
		Bitmap bitmap = BitmapFactory.decodeStream(bitmapStream, null, options);
		
		return bitmap;
	}
	
	public static Bitmap downloadThumbnailAndStoreLocally(IStorableModel model) {
		Bitmap thumbnail = downloadBitmap(model.getUrl(), true);
		
		if(null != thumbnail) {
			storeLocally(model, thumbnail);
		}
		
		return thumbnail;
	}

	public static Bitmap setImageThumbnail(IStorableModel model, ImageView imageView) {
		Bitmap thumbnail = null;
		
		try {
			if(null == model) {
				return null;
			}
			
			thumbnail = getFromLocalStorage(model);
			
			if(null == thumbnail) {
				new DownloadImageTask(imageView).execute(model);
			}
			else {
				imageView.setImageBitmap(thumbnail);
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return thumbnail;
	}
	
	/**
	 * Deletes all stored thumbnails from the memory card.
	 */
	public static void deleteStoredImages() {
		String sdDirPath = getSdDirPath();
		
		if(StringUtils.isNullOrEmpty(sdDirPath)) {
			return;
		}

		FileUtils.deleteFile(sdDirPath + "/" + APP_STORAGE_PATH);
	}
}
