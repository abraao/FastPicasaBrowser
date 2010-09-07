package com.guaranacode.android.fastpicasabrowser.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.guaranacode.android.fastpicasabrowser.util.FileUtils;
import com.guaranacode.android.fastpicasabrowser.util.ImageUtils;
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
		
		try {
			FileInputStream fin = new FileInputStream(localPath);
			thumbnail = BitmapFactory.decodeStream(fin);
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
		
		File sdDir = Environment.getExternalStorageDirectory();
		
		if(null == sdDir) {
			return null;
		}
		
		String sdDirPath = null;
		
		try {
			sdDirPath = sdDir.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
			sdDirPath = null;
		}
		
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

	public static Bitmap getThumbnail(IStorableModel model) {
		if(null == model) {
			return null;
		}
		
		Bitmap thumbnail = getFromLocalStorage(model);
		
		if(null == thumbnail) {
			thumbnail = ImageUtils.downloadBitmap(model.getUrl());
			
			if(null != thumbnail) {
				storeLocally(model, thumbnail);
			}
		}
		
		return thumbnail;
	}
}
