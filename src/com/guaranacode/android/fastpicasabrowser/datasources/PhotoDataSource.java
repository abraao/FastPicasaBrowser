package com.guaranacode.android.fastpicasabrowser.datasources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.google.api.client.googleapis.GoogleTransport;
import com.guaranacode.android.fastpicasabrowser.database.DatabaseHelper;
import com.guaranacode.android.fastpicasabrowser.database.tables.PhotosTable;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PhotoEntry;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PhotoListFeed;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PicasaUrl;

/**
 * Class to fetch photo list from the internet and cache it locally,
 * or to fetch it from the local cache.
 *
 * @author abe@guaranacode.com
 *
 */
public class PhotoDataSource {

	private PhotoDataSource() {
		
	}
	
	/**
	 * 
	 * @return
	 */
	private static SQLiteQueryBuilder getQueryBuilder() {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(PhotosTable.getInstance().getTableName());
		qb.setProjectionMap(PhotosTable.getInstance().getProjectionMap());
		
		return qb;
	}
	
	/**
	 * 
	 * @param photo
	 * @return
	 */
	private static ContentValues getContentValuesForPhoto(PhotoEntry photo) {
		if(null == photo) {
			return null;
		}
		
		ContentValues values = new ContentValues();
		values.put(PhotosTable.PHOTO_ID, photo.photoId);
		values.put(PhotosTable.ETAG, photo.etag);
		values.put(PhotosTable.THUMBNAIL_LOCAL_PATH, photo.getUrl());
		values.put(PhotosTable.ALBUM_ID, photo.albumId);
		
		return values;
	}
	
	/**
	 * Returns the list of all albums from either the database or a picasa feed.
	 * @param transport
	 * @return
	 */
	public static List<PhotoEntry> getPhotos(String albumId, GoogleTransport transport, Context context) {
		List<PhotoEntry> photos = getPhotosFromDatabase(albumId, context);
		
		if(null == photos) {
			photos = getPhotosFromPicasa(albumId, transport);
			insertPhotosIntoDatabase(photos, context);
		}
		
		return photos;
	}
	
	/**
	 * 
	 * @param albumId
	 * @param context
	 * @return
	 */
	private static List<PhotoEntry> getPhotosFromDatabase(String albumId, Context context) {
		List<PhotoEntry> photos = new ArrayList<PhotoEntry>();

		SQLiteQueryBuilder qb = getQueryBuilder();
		
		DatabaseHelper dbh = new DatabaseHelper(context);

		/*SQLiteDatabase db2 = dbh.getWritableDatabase();
		db2.execSQL("DROP TABLE " + AlbumsTable.getInstance().getTableName());
		db2.execSQL("DROP TABLE " + PhotosTable.getInstance().getTableName());
		dbh.onCreate(db2);*/
		
		SQLiteDatabase db = dbh.getReadableDatabase();
		
		Collection<String> photoColumns = PhotosTable.getInstance().getProjectionMap().values();
		String[] photoColsArray = (String[]) photoColumns.toArray(new String[photoColumns.size()]);

		String[] whereArgs = new String[1];
		whereArgs[0] = albumId;

		Cursor cur = qb.query(db, photoColsArray, "album_id = ?", whereArgs, null, null, null);

		cur.moveToFirst();
		
		while(!cur.isAfterLast()) {
			// TODO: associate column names with column numbers.
			PhotoEntry photoEntry = new PhotoEntry();
			photoEntry.photoId = cur.getString(0);
			photoEntry.etag = cur.getString(1);
			photoEntry.thumbnailUrl = cur.getString(3);
			photoEntry.albumId = cur.getString(4);
			
			photos.add(photoEntry);
			
			cur.moveToNext();
		}

		cur.close();

		if(0 == photos.size()) {
			return null;
		}
		
		return photos;
	}
	
	/**
	 * 
	 * @param albumId
	 * @param transport
	 * @return
	 */
	private static List<PhotoEntry> getPhotosFromPicasa(String albumId, GoogleTransport transport) {
		List<PhotoEntry> photos = new ArrayList<PhotoEntry>();
		
		try {
			PicasaUrl url = PicasaUrl.fromRelativePath("feed/api/user/default/albumid/" + albumId);
			
			// page through results
			while (true) {
				PhotoListFeed photoFeed = PhotoListFeed.executeGet(transport, url);
				
				if(null != photoFeed.photos) {
					photos.addAll(photoFeed.photos);
				}

				String nextLink = photoFeed.getNextLink();
				
				if (nextLink == null) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for(PhotoEntry photo : photos) {
			photo.albumId = albumId;
		}
		
		return photos;
	}
	
	/**
	 * 
	 * @param albumId
	 * @param photos
	 * @param context
	 */
	private static void insertPhotosIntoDatabase(List<PhotoEntry> photos, Context context) {
		DatabaseHelper dbh = new DatabaseHelper(context);
		SQLiteDatabase db = dbh.getWritableDatabase();
		
        String nullColumnHack = null;

        for(PhotoEntry photo : photos) {
        	ContentValues values = getContentValuesForPhoto(photo);
        	
        	if(null != values) {
        		db.insert(PhotosTable.getInstance().getTableName(), nullColumnHack, values);
        	}
        }
	}
}
