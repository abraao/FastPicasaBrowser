package com.guaranacode.android.fastpicasabrowser.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.Toast;

import com.google.api.client.googleapis.GoogleTransport;
import com.guaranacode.android.fastpicasabrowser.adapters.PhotoGridAdapter;
import com.guaranacode.android.fastpicasabrowser.listeners.PhotoGridItemClickListener;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PhotoEntry;
import com.guaranacode.android.fastpicasabrowser.tasks.DownloadPhotoList;
import com.guaranacode.android.fastpicasabrowser.util.AuthUtils;
import com.guaranacode.android.fastpicasabrowser.R;

/**
 * This activity displays the photos associated with an album.
 * 
 * @author abe@guaranacode.com
 *
 */
public class PhotoGridActivity extends Activity {

	private GoogleTransport mTransport;
	private String mAuthToken;
	private String mAlbumId;
	private String mAlbumTitle;

	private List<PhotoEntry> photos;
	
	public PhotoGridActivity() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_grid);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		
		this.mAuthToken = extras.getString("authToken");
		this.mAlbumId = extras.getString("albumId");
		this.mAlbumTitle = extras.getString("albumTitle");
		
		this.setTitle(this.mAlbumTitle);
		
		this.mTransport = AuthUtils.buildTransportWithToken(this.mAuthToken);
		
		displayPhotos();
	}

	private void displayPhotos() {
		Toast.makeText(getApplicationContext(), "Loading all photos for album", Toast.LENGTH_LONG).show();

		new DownloadPhotoList(this, mTransport, getApplicationContext()).execute(mAlbumId);
	}

	public void setPhotos(List<PhotoEntry> photoList) {
		if(null == photoList) {
			return;
		}

		this.photos = photoList; 

		GridView gridView = (GridView) findViewById(R.id.photo_gridview);
		gridView.setAdapter(new PhotoGridAdapter(this, this.photos));

		gridView.setOnItemClickListener(new PhotoGridItemClickListener(this.photos, this.mAuthToken, this));
	}
}
