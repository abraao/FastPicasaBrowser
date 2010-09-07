package com.guaranacode.android.fastpicasabrowser.picasa.model;

import com.google.api.client.util.Key;

public class Category {

	@Key("@scheme")
	public String scheme;

	@Key("@term")
	public String term;

	public static Category newKind(String kind) {
		Category category = new Category();
		category.scheme = "http://schemas.google.com/g/2005#kind";
		category.term = "http://schemas.google.com/photos/2007#" + kind;
		return category;
	}
}