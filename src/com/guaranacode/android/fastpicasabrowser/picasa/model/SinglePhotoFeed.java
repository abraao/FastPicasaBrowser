package com.guaranacode.android.fastpicasabrowser.picasa.model;

import java.io.IOException;

import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.util.Key;

/**
 * A feed from the Picasa API containing a single photo.
 *
 * @author abe@guaranacode.com
 *
 */
public class SinglePhotoFeed extends Feed {

	@Key("media:group")
	public MediaGroup photo;

	public static SinglePhotoFeed executeGet(GoogleTransport transport, PicasaUrl url) throws IOException {
		url.kinds = "photo";
		
		return (SinglePhotoFeed) Feed.executeGet(transport, url, SinglePhotoFeed.class);
	}
}
