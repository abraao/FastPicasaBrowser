package com.guaranacode.android.fastpicasabrowser.tasks;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

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
	private Handler mProgressHandler;
	
	public DownloadPhotoList(
			PhotoGridActivity activity,
			GoogleTransport transport,
			Context context, Handler progressHandler) {
		mActivity = activity;
		mTransport = transport;
		mContext = context;
		mProgressHandler = progressHandler;
	}
	
	@Override
	protected List<PhotoEntry> doInBackground(String... albumIds) {
		List<PhotoEntry> photos = PhotoDataSource.getPhotos(albumIds[0], mTransport, mContext, mProgressHandler);
		return photos;
	}

	protected void onPostExecute(List<PhotoEntry> photoList) {
		if(null == photoList) {
			return;
		}
		
		mActivity.setPhotos(photoList);
	}
}
