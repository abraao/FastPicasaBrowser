package com.guaranacode.android.fastpicasabrowser.datasources;

import java.io.IOException;
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
import com.guaranacode.android.fastpicasabrowser.database.tables.AlbumsTable;
import com.guaranacode.android.fastpicasabrowser.picasa.model.AlbumEntry;
import com.guaranacode.android.fastpicasabrowser.picasa.model.AlbumFeed;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PicasaUrl;

/**
 * Class to fetch album list from the internet and cache it locally,
 * or to fetch it from the local cache.
 *
 * @author abe@guaranacode.com
 *
 */
public class AlbumDataSource {
	
	private AlbumDataSource() {
		
	}
	
	/**
	 * Returns the list of all albums from either the database or a picasa feed.
	 * @param transport
	 * @return
	 */
	public static List<AlbumEntry> getAlbums(GoogleTransport transport, Context context) {
		List<AlbumEntry> albums = null;
		
		// If the data is not available locally
		albums = getAlbumsFromDatabase(context);
		
		// get it from Picasa
		if(null == albums) {
			albums = getAlbumsFromPicasa(transport);
			// cache the data locally in the database
			insertAlbumsIntoDatabase(albums, context);
		}
		
		return albums;
	}

	/**
	 * Get the albums from Picasa.
	 * @param transport
	 * @return
	 */
	private static List<AlbumEntry> getAlbumsFromPicasa(GoogleTransport transport) {
		List<AlbumEntry> albums = new ArrayList<AlbumEntry>();
		
		try {
			PicasaUrl url = PicasaUrl.fromRelativePath("feed/api/user/default");
			
			// page through results
			while (true) {
				AlbumFeed albumFeed = AlbumFeed.executeGet(transport, url);

				if (albumFeed.albums != null) {
					albums.addAll(albumFeed.albums);
				}
				
				String nextLink = albumFeed.getNextLink();
				
				if (nextLink == null) {
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return albums;
	}
	
	private static SQLiteQueryBuilder getQueryBuilder() {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(AlbumsTable.getInstance().getTableName());
		qb.setProjectionMap(AlbumsTable.getInstance().getProjectionMap());
		
		return qb;
	}
	
	/**
	 * Cache the data locally in the database.
	 * @param context
	 */
	private static void insertAlbumsIntoDatabase(List<AlbumEntry> albums, Context context) {
		DatabaseHelper dbh = new DatabaseHelper(context);
		SQLiteDatabase db = dbh.getWritableDatabase();
		
        String nullColumnHack = null;

        for(AlbumEntry album : albums) {
        	ContentValues values = getContentValuesForAlbum(album);
        	
        	if(null != values) {
        		db.insert(AlbumsTable.getInstance().getTableName(), nullColumnHack, values);
        	}
        }
	}
	
	/**
	 * 
	 * @param album
	 * @return
	 */
	private static ContentValues getContentValuesForAlbum(AlbumEntry album) {
		if(null == album) {
			return null;
		}
		
		ContentValues values = new ContentValues();
		values.put(AlbumsTable.ALBUM_ID, album.albumId);
		values.put(AlbumsTable.ETAG, album.etag);
		values.put(AlbumsTable.TITLE, album.title);
		values.put(AlbumsTable.THUMBNAIL_LOCAL_PATH, album.getUrl());
		
		return values;
	}
	
	/**
	 * Get album data from the embedded database.
	 * @param context
	 * @return
	 */
	private static List<AlbumEntry> getAlbumsFromDatabase(Context context) {
		List<AlbumEntry> albums = new ArrayList<AlbumEntry>();
		
		SQLiteQueryBuilder qb = getQueryBuilder();
		
		DatabaseHelper dbh = new DatabaseHelper(context);

		/*SQLiteDatabase db2 = dbh.getWritableDatabase();
		db2.execSQL("DROP TABLE " + AlbumsTable.getInstance().getTableName());
		db2.execSQL("DROP TABLE " + PhotosTable.getInstance().getTableName());
		dbh.onCreate(db2);*/
		
		SQLiteDatabase db = dbh.getReadableDatabase();
		
		Collection<String> albumColumns = AlbumsTable.getInstance().getProjectionMap().values();
		String[] albumColsArray = (String[]) albumColumns.toArray(new String[albumColumns.size()]);
		
		Cursor cur = qb.query(db, albumColsArray, null, null, null, null, null);
		
		cur.moveToFirst();
		
		while(!cur.isAfterLast()) {
			// TODO: associate column names with column numbers.
			AlbumEntry albumEntry = new AlbumEntry();
			albumEntry.albumId = cur.getString(0);
			albumEntry.etag = cur.getString(2);
			albumEntry.title = cur.getString(1);
			albumEntry.thumbnailUrl = cur.getString(4);
			
			albums.add(albumEntry);
			
			cur.moveToNext();
		}
		
		cur.close();
		
		if(0 == albums.size()) {
			return null;
		}
		
		return albums;
	}
}
