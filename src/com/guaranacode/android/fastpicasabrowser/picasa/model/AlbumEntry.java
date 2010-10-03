package com.guaranacode.android.fastpicasabrowser.picasa.model;

import com.google.api.client.util.Key;
import com.guaranacode.android.fastpicasabrowser.storage.IStorableModel;
import com.guaranacode.android.fastpicasabrowser.util.StringUtils;

/**
 * An album from the Picasa API feed.
 *
 * @author abe@guaranacode.com
 *
 */
public class AlbumEntry extends Entry implements IStorableModel {

	@Key("gphoto:access")
	public String access;

	@Key
	public Category category = Category.newKind("album");
	
	@Key("media:group")
	public MediaGroup mediaGroup;
	
	@Key("gphoto:id")
	public String albumId;
	
	public String thumbnailUrl;
	
	@Key
	public String updated;

	public String getUrl() {
		if(!StringUtils.isNullOrEmpty(thumbnailUrl)) {
			return thumbnailUrl;
		}
		
		if(null != this.mediaGroup) {
			return this.mediaGroup.thumbnail.url;
		}
		
		return null;
	}
	
	public String getDir() {
		return "album_thumbnails";
	}
}
