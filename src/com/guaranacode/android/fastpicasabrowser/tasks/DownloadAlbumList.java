package com.guaranacode.android.fastpicasabrowser.tasks;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.google.api.client.googleapis.GoogleTransport;
import com.guaranacode.android.fastpicasabrowser.activities.FastPicasaBrowserActivity;
import com.guaranacode.android.fastpicasabrowser.datasources.AlbumDataSource;
import com.guaranacode.android.fastpicasabrowser.picasa.model.AlbumEntry;

/**
 * Task to download a list of albums asynchronously in the background.
 *
 * @author abe@guaranacode.com
 *
 */
public class DownloadAlbumList extends AsyncTask<Void, Integer, List<AlbumEntry>> {
	private GoogleTransport mTransport;
	private Context mContext;
	private FastPicasaBrowserActivity mActivity;
	private Handler mAlbumProgressHandler;
	
	public DownloadAlbumList(
			FastPicasaBrowserActivity activity,
			GoogleTransport transport,
			Context context,
			Handler albumProgressHandler) {
		mActivity = activity;
		mTransport = transport;
		mContext = context;
		mAlbumProgressHandler = albumProgressHandler;
	}
	
	@Override
	protected List<AlbumEntry> doInBackground(Void... params) {
		List<AlbumEntry> albums = AlbumDataSource.getAlbums(mTransport, mContext, mAlbumProgressHandler);
		return albums;
	}
	
	protected void onPostExecute(List<AlbumEntry> albumList) {
		if(null == albumList) {
			return;
		}
		
		mActivity.setAlbums(albumList, false);
	}

}
