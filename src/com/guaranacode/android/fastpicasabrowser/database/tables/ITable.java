package com.guaranacode.android.fastpicasabrowser.database.tables;

import java.util.HashMap;

import android.provider.BaseColumns;

/**
 * Interface for all tables.
 *
 * @author abe@guaranacode.com
 *
 */
public interface ITable extends BaseColumns {

	String getTableName();

	String getCreateTableSQL();
	
	HashMap<String, String> getProjectionMap();
}
