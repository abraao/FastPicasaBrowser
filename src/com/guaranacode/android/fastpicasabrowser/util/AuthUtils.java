package com.guaranacode.android.fastpicasabrowser.util;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.xml.atom.AtomParser;
import com.google.api.data.picasa.v2.PicasaWebAlbums;
import com.google.api.data.picasa.v2.atom.PicasaWebAlbumsAtom;

/**
 * Utility class for authentication. 
 * 
 * @author abe@guaranacode.com
 *
 */
public class AuthUtils {

	/**
	 * Builds a GoogleTransport for interacting with the Picasa API.
	 * @return
	 */
	public static GoogleTransport buildTransport() {
		GoogleTransport transport = new GoogleTransport();
		
		transport.setVersionHeader(PicasaWebAlbums.VERSION);
		AtomParser parser = new AtomParser();
		parser.namespaceDictionary = PicasaWebAlbumsAtom.NAMESPACE_DICTIONARY;
		transport.addParser(parser);
		
		transport.applicationName = "google-picasaandroidsample-1.0";
		
		HttpTransport.setLowLevelHttpTransport(ApacheHttpTransport.INSTANCE);
		
		return transport;
	}
	
	/**
	 * Creates a transport that uses the specified authentication token.
	 * @param authToken
	 * @return
	 */
	public static GoogleTransport buildTransportWithToken(String authToken) {
		GoogleTransport transport = buildTransport();
		transport.setClientLoginToken(authToken);
		
		return transport;
	}
}
