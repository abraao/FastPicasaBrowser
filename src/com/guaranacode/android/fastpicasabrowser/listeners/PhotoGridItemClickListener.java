package com.guaranacode.android.fastpicasabrowser.listeners;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.api.client.googleapis.GoogleTransport;
import com.guaranacode.android.fastpicasabrowser.R;
import com.guaranacode.android.fastpicasabrowser.picasa.model.MediaGroup;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PhotoEntry;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PicasaUrl;
import com.guaranacode.android.fastpicasabrowser.picasa.model.SinglePhotoFeed;
import com.guaranacode.android.fastpicasabrowser.storage.ImageStorage;
import com.guaranacode.android.fastpicasabrowser.util.AuthUtils;

/**
 * Listen to click event on a photo in a photo grid.
 *
 * @author abe@guaranacode.com
 *
 */
public class PhotoGridItemClickListener implements OnItemClickListener {

	private List<PhotoEntry> mPhotos;
	private String mAuthToken;
	private Context mContext;
	
	public PhotoGridItemClickListener(List<PhotoEntry> photos, String authToken, Context context) {
		this.mPhotos = photos;
		this.mAuthToken = authToken;
		this.mContext = context;
	}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(null == this.mPhotos) {
			return;
		}
		
		PhotoEntry photoEntry;
		
		if(position < this.mPhotos.size()) {
			photoEntry = this.mPhotos.get(position);
		}
		else {
			return;
		}

		displayPhoto(photoEntry.photoId);
	}

	private void displayPhoto(String photoId) {
		GoogleTransport transport = AuthUtils.buildTransportWithToken(this.mAuthToken);
		
		MediaGroup photo = null;

		try {
			PicasaUrl url = PicasaUrl.fromRelativePath("feed/api/user/default/photoid/" + photoId);
			
			SinglePhotoFeed spf = SinglePhotoFeed.executeGet(transport, url);
			
			if(null != spf.photo) {
				photo = spf.photo;
			}
		} catch (Exception e) {
		}
		
		if(null == photo) {
			Toast.makeText(this.mContext.getApplicationContext(), "Error getting photo.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Bitmap photoBitmap = ImageStorage.downloadBitmap(photo.content.url, false);
		if(null == photoBitmap) {
			return;
		}
		
		File photoFile = ImageStorage.storeBitmapLocally(photo.content.url, photoBitmap, this.mContext);
		if(null == photoFile) {
			Toast.makeText(this.mContext, R.string.photo_storage_error, Toast.LENGTH_LONG);
			return;
		}
		
		Uri photoUri = Uri.fromFile(photoFile);
		if(null == photoUri) {
			return;
		}
		
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(photoUri, "image/jpg");
		this.mContext.startActivity(intent);
	}
}
