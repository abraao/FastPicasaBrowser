package com.guaranacode.android.fastpicasabrowser.picasa.model;

import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.List;

/**
 * A feed of albums from the Picasa API.
 *
 * @author abe@guaranacode.com
 *
 */
public class AlbumFeed extends Feed {

	@Key("entry")
	public List<AlbumEntry> albums;

	public static AlbumFeed executeGet(GoogleTransport transport, PicasaUrl url) throws IOException {
		url.kinds = "album";
		
		return (AlbumFeed) Feed.executeGet(transport, url, AlbumFeed.class);
	}
}
