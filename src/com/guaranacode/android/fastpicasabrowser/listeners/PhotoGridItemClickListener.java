package com.guaranacode.android.fastpicasabrowser.listeners;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.api.client.googleapis.GoogleTransport;
import com.guaranacode.android.fastpicasabrowser.picasa.model.MediaGroup;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PhotoEntry;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PicasaUrl;
import com.guaranacode.android.fastpicasabrowser.picasa.model.SinglePhotoFeed;
import com.guaranacode.android.fastpicasabrowser.storage.ImageStorage;
import com.guaranacode.android.fastpicasabrowser.util.AuthUtils;
import com.guaranacode.android.fastpicasabrowser.util.ImageUtils;

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
			e.printStackTrace();
		}
		
		Bitmap photoBitmap = ImageUtils.downloadBitmap(photo.content.url);
		if(null == photoBitmap) {
			return;
		}
		
		File photoFile = ImageStorage.storeBitmapLocally(photo.content.url, photoBitmap);
		if(null == photoFile) {
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
