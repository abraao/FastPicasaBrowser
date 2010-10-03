package com.guaranacode.android.fastpicasabrowser.database.tables;

import java.util.HashMap;

/**
 * Some useful contants for the albums table.
 *
 * @author abe@guaranacode.com
 *
 */
public class AlbumsTable implements ITable {

	private AlbumsTable() {}

	/*
	 * Column names
	 */
	public static final String ALBUM_ID = "album_id";

	public static final String THUMBNAIL_URL = "thumbnail_url";
	
	public static final String ETAG = "etag";
	
	public static final String TITLE = "title";
	
	public static final String UPDATED = "updated";
	
	public static final String THUMBNAIL_LOCAL_PATH = "thumbnail_local_path";

	/*
	 * ITable interface methods
	 */
	
	private static AlbumsTable mInstance;
	
	public static ITable getInstance() {
		if(null == mInstance) {
			mInstance = new AlbumsTable();
		}
		
		return mInstance;
	}
	
	public String getTableName() {
		return "albums";
	}
	
	public String getCreateTableSQL() {
		String sql = "CREATE TABLE " + getTableName() + " ("
			+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ ALBUM_ID + " TEXT,"
			+ ETAG + " TEXT,"
			+ TITLE + " TEXT,"
			+ UPDATED + " TEXT,"
			+ THUMBNAIL_LOCAL_PATH + " TEXT"
			+ ");";
		
		return sql;
	}
	
	private static HashMap<String, String> mProjectionMap;
	public HashMap<String, String> getProjectionMap() {
		return mProjectionMap;
	}

	static {
		mProjectionMap = new HashMap<String, String>();
		mProjectionMap.put(_ID, _ID);
		mProjectionMap.put(ALBUM_ID, ALBUM_ID);
		mProjectionMap.put(ETAG, ETAG);
		mProjectionMap.put(TITLE, TITLE);
		mProjectionMap.put(UPDATED, UPDATED);
		mProjectionMap.put(THUMBNAIL_LOCAL_PATH, THUMBNAIL_LOCAL_PATH);
	}
}
