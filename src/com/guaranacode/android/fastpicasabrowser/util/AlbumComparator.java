package com.guaranacode.android.fastpicasabrowser.util;

import java.util.Comparator;

import com.guaranacode.android.fastpicasabrowser.picasa.model.AlbumEntry;

public class AlbumComparator implements Comparator<AlbumEntry> {
	
	public enum AlbumField { TITLE, LAST_UPDATE }
	
	private AlbumField mComparedField;
	
	public AlbumComparator(AlbumField albumField) {
		mComparedField = albumField;
	}

	public int compare(AlbumEntry object1, AlbumEntry object2) {
		switch(mComparedField) {
			case LAST_UPDATE:
				// Reverse comparison to sort in descending order
				return object2.updated.compareTo(object1.updated);
			case TITLE:
			default:
				return object1.title.compareTo(object2.title);
		}
	}

}
