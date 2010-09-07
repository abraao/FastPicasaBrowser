package com.guaranacode.android.fastpicasabrowser.picasa.model;

import com.google.api.client.util.Key;

public class MediaGroup {

	@Key("media:title")
	public String title;
	
	@Key("media:description")
	public String description;
	
	@Key("media:content")
	public MediaContent content;
	
	@Key("media:thumbnail")
	public Thumbnail thumbnail;
}
