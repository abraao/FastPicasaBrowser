package com.guaranacode.android.fastpicasabrowser.picasa.model;

import com.google.api.client.util.Key;

public class Thumbnail {
	
	@Key("@url")
	public String url;
	
	@Key("@height")
	public int height;
	
	@Key("@width")
	public int width;
}
