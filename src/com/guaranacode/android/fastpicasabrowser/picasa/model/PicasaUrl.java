package com.guaranacode.android.fastpicasabrowser.picasa.model;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.util.Key;
import com.google.api.data.picasa.v2.PicasaWebAlbums;

/**
 * Produces URLs for usage with the Picasa API.
 *
 * @author abe@guaranacode.com
 *
 */
public class PicasaUrl extends GoogleUrl {

	@Key
	public String kinds;

	public PicasaUrl(String encodedUrl) {
		super(encodedUrl);
	}

	public static PicasaUrl fromRelativePath(String relativePath) {
		PicasaUrl result = new PicasaUrl(PicasaWebAlbums.ROOT_URL);
		result.path += relativePath;
		return result;
	}
}
