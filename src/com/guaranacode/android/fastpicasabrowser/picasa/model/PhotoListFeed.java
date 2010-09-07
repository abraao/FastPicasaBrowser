package com.guaranacode.android.fastpicasabrowser.picasa.model;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.util.Key;

/**
 * A feed from the Picasa API containing a list of photos.
 *
 * @author abe@guaranacode.com
 *
 */
public class PhotoListFeed extends Feed {
	
	@Key("entry")
	public List<PhotoEntry> photos;

	public static PhotoListFeed executeGet(GoogleTransport transport, PicasaUrl url) throws IOException {
		url.kinds = "photo";
		
		return (PhotoListFeed) Feed.executeGet(transport, url, PhotoListFeed.class);
	}
}
