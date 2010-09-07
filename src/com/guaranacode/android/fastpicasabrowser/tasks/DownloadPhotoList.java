package com.guaranacode.android.fastpicasabrowser.tasks;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.googleapis.GoogleTransport;
import com.guaranacode.android.fastpicasabrowser.activities.PhotoGridActivity;
import com.guaranacode.android.fastpicasabrowser.datasources.PhotoDataSource;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PhotoEntry;

/**
 * Task to download a list of photos asynchronously in the background.
 *
 * @author abe@guaranacode.com
 *
 */
public class DownloadPhotoList extends AsyncTask<String, Integer, List<PhotoEntry>> {
	private GoogleTransport mTransport;
	private Context mContext;
	private PhotoGridActivity mActivity;
	
	public DownloadPhotoList(PhotoGridActivity activity, GoogleTransport transport, Context context) {
		mActivity = activity;
		mTransport = transport;
		mContext = context;
	}
	
	@Override
	protected List<PhotoEntry> doInBackground(String... albumIds) {
		List<PhotoEntry> photos = PhotoDataSource.getPhotos(albumIds[0], mTransport, mContext);
		return photos;
	}

	protected void onPostExecute(List<PhotoEntry> photoList) {
		if(null == photoList) {
			return;
		}
		
		mActivity.setPhotos(photoList);
	}
}
