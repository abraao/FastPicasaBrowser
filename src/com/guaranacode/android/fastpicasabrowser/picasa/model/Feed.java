package com.guaranacode.android.fastpicasabrowser.picasa.model;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.xml.atom.GData;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.Key;

/**
 * A feed from the Picasa API.
 *
 * @author abe@guaranacode.com
 *
 */
public class Feed {

	@Key("link")
	public List<Link> links;
	
	@Key("openSearch:totalResults")
	public int numEntries;
	
	@Key("openSearch:startIndex")
	public int entryIndex;
	
	@Key("openSearch:itemsPerPage")
	public int maxEntriesPerPage;

	public String getNextLink() {
		return Link.find(links, "next");
	}
	
	public int numPages() {
		return (int)Math.ceil(((double)numEntries) / ((double)maxEntriesPerPage));
	}
	
	/**
	 * The current page, using 0 based indexing.
	 * @return
	 */
	public int currentPage() {
		return ((int)Math.ceil(((double)entryIndex) / ((double)maxEntriesPerPage))) - 1;
	}

	static Feed executeGet(GoogleTransport transport, PicasaUrl url,
			Class<? extends Feed> feedClass) throws IOException {
		
		url.fields = GData.getFieldsFor(feedClass);
		
		HttpRequest request = transport.buildGetRequest();
		request.url = url;
		
		HttpResponse httpResponse = request.execute();

		Feed result = httpResponse.parseAs(feedClass);
		
		return result;
	}
}
