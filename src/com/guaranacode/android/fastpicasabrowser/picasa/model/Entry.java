package com.guaranacode.android.fastpicasabrowser.picasa.model;

import java.util.List;

import com.google.api.client.util.Key;

/**
 * An entry in a feed from the Picasa API.
 *
 * @author abe@guaranacode.com
 *
 */
public class Entry implements Cloneable {

	@Key("@gd:etag")
	public String etag;

	@Key("link")
	public List<Link> links;

	@Key
	public String title;
}