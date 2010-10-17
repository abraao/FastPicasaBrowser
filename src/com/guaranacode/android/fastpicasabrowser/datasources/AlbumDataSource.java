package com.guaranacode.android.fastpicasabrowser.datasources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Handler;
import android.os.Message;

import com.google.api.client.googleapis.GoogleTransport;
import com.guaranacode.android.fastpicasabrowser.activities.FastPicasaBrowserActivity;
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
	 * @param progressHandler 
	 * @return
	 */
	public static List<AlbumEntry> getAlbums(
			GoogleTransport transport,
			Context context,
			Handler progressHandler) {

		// Check if the data is available locally
		List<AlbumEntry> albums = getAlbumsFromDatabase(context);
		
		// get it from Picasa
		if(null == albums) {
			Message msg = new Message();
			msg.what = FastPicasaBrowserActivity.PROGRESS_START;
			progressHandler.sendMessage(msg);

			albums = getAlbumsFromPicasa(transport, progressHandler);
			// cache the data locally in the database
			insertAlbumsIntoDatabase(albums, context, progressHandler);
		}

		Message msg = new Message();
		msg.what = FastPicasaBrowserActivity.PROGRESS_COMPLETE;
		progressHandler.sendMessage(msg);
		
		return albums;
	}

	/**
	 * Get the albums from Picasa.
	 * @param transport
	 * @param progressHandler 
	 * @return
	 */
	private static List<AlbumEntry> getAlbumsFromPicasa(GoogleTransport transport, Handler progressHandler) {
		List<AlbumEntry> albums = new ArrayList<AlbumEntry>();
		
		try {
			PicasaUrl url = PicasaUrl.fromRelativePath("feed/api/user/default");
			
			// page through results
			while (true) {
				AlbumFeed albumFeed = AlbumFeed.executeGet(transport, url);
				
				// Dispatch a message with the max and how many we've displayed so far
				Message msg = new Message();
				msg.what = FastPicasaBrowserActivity.PROGRESS_NEW_PAGE;
				msg.arg1 = albumFeed.currentPage();
				msg.arg2 = albumFeed.numPages();
				progressHandler.sendMessage(msg);

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
	 * @param progressHandler 
	 */
	private static void insertAlbumsIntoDatabase(List<AlbumEntry> albums, Context context, Handler progressHandler) {
		DatabaseHelper dbh = new DatabaseHelper(context);
		SQLiteDatabase db = dbh.getWritableDatabase();
		
        String nullColumnHack = null;

        int count = 0;
        int numAlbums = albums.size();
        
        try{
        	db.beginTransaction();
        	
	        for(AlbumEntry album : albums) {
	        	ContentValues values = getContentValuesForAlbum(album);
	        	
	        	if(null != values) {
	        		db.insert(AlbumsTable.getInstance().getTableName(), nullColumnHack, values);
	        		count++;
	
					Message msg = new Message();
					msg.what = FastPicasaBrowserActivity.PROGRESS_DBINSERT;
					msg.arg1 = count;
					msg.arg2 = numAlbums;
					progressHandler.sendMessage(msg);
	        	}
	        }
	        
	        db.setTransactionSuccessful();
        } catch(SQLException ex) {
        } finally {
        	db.endTransaction();
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
		values.put(AlbumsTable.UPDATED, album.updated);
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
			albumEntry.updated = cur.getString(5);
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
