package com.guaranacode.android.fastpicasabrowser.database.tables;

import java.util.HashMap;

/**
 * Some useful constants for the photos table.
 *
 * @author abe@guaranacode.com
 *
 */
public class PhotosTable implements ITable {

	private PhotosTable() {}

	/*
	 * Column names
	 */
	public static final String PHOTO_ID = "photo_id";
	
	public static final String THUMBNAIL_URL = "thumbnail_url";
	
	public static final String ETAG = "etag";
	
	public static final String THUMBNAIL_LOCAL_PATH = "thumbnail_local_path";
	
	public static final String PHOTO_URL = "photo_url";
	
	public static final String ALBUM_ID = "album_id";
	
	/*
	 * ITable interface members 
	 */
	
	private static PhotosTable mInstance;
	
	public static ITable getInstance() {
		if(null == mInstance) {
			mInstance = new PhotosTable();
		}
		
		return mInstance;
	}
	
	public String getCreateTableSQL() {
		String sql = "CREATE TABLE " + getTableName() + " ("
			+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ PHOTO_ID + " TEXT,"
			+ ETAG + " TEXT,"
			+ THUMBNAIL_LOCAL_PATH + " TEXT,"
			+ PHOTO_URL + " TEXT,"
			+ ALBUM_ID + " TEXT"
			+ ");";
	
		return sql;
	}

	public String getTableName() {
		return "photos";
	}
	
	private static HashMap<String, String> mProjectionMap;
	public HashMap<String, String> getProjectionMap() {
		return mProjectionMap;
	}

	static {
		mProjectionMap = new HashMap<String, String>();
		mProjectionMap.put(_ID, _ID);
		mProjectionMap.put(PHOTO_ID, PHOTO_ID);
		mProjectionMap.put(ETAG, ETAG);
		mProjectionMap.put(PHOTO_URL, PHOTO_URL);
		mProjectionMap.put(THUMBNAIL_LOCAL_PATH, THUMBNAIL_LOCAL_PATH);
		mProjectionMap.put(ALBUM_ID, ALBUM_ID);
	}

}
