package com.guaranacode.android.fastpicasabrowser.picasa.model;

import com.google.api.client.util.Key;
import com.guaranacode.android.fastpicasabrowser.storage.IStorableModel;
import com.guaranacode.android.fastpicasabrowser.util.StringUtils;

/**
 * A photo in the photo feed.
 *
 * @author abe@guaranacode.com
 *
 */
public class PhotoEntry extends Entry implements IStorableModel {
	
	@Key("gphoto:id")
	public String photoId;
	
	@Key("media:group")
	public MediaGroup mediaGroup;
	
	public String albumId;
	
	public String thumbnailUrl;

	public String getDir() {
		return "photo_thumbnails";
	}

	public String getUrl() {
		if(!StringUtils.isNullOrEmpty(thumbnailUrl)) {
			return thumbnailUrl;
		}
		
		if(null != this.mediaGroup) {
			return this.mediaGroup.thumbnail.url;
		}
		
		return null;
	}
}
