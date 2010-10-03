package com.guaranacode.android.fastpicasabrowser.database;

import java.util.ArrayList;
import java.util.List;

import com.guaranacode.android.fastpicasabrowser.database.tables.AlbumsTable;
import com.guaranacode.android.fastpicasabrowser.database.tables.ITable;
import com.guaranacode.android.fastpicasabrowser.database.tables.PhotosTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper for the databased used by the application.
 *
 * @author abe@guaranacode.com
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private List<ITable> mAllTables;
	
	public DatabaseHelper(Context context) {
		super(context, DbConstants.DATABASE_NAME, null, DbConstants.DATABASE_VERSION);
		
		mAllTables = new ArrayList<ITable>();
		//JAVASUCKS: no collection initializers.
		mAllTables.add(AlbumsTable.getInstance());
		mAllTables.add(PhotosTable.getInstance());
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for(ITable table : mAllTables) {
			db.execSQL(table.getCreateTableSQL());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		clearDatabase(db);
		onCreate(db);
	}

	/**
	 * Deletes all data present in the database.
	 * @param db
	 */
	public void clearDatabase(SQLiteDatabase db) {
		for(ITable table : mAllTables) {
			db.delete(table.getTableName(), null, null);
		}
	}
}
