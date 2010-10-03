package com.guaranacode.android.fastpicasabrowser.util;

import java.util.Comparator;

import com.guaranacode.android.fastpicasabrowser.picasa.model.AlbumEntry;

public class AlbumTitleComparator implements Comparator<AlbumEntry> {

	public int compare(AlbumEntry object1, AlbumEntry object2) {
		return object1.title.compareTo(object2.title);
	}

}
