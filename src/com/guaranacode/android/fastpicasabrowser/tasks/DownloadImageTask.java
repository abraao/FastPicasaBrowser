package com.guaranacode.android.fastpicasabrowser.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.guaranacode.android.fastpicasabrowser.storage.IStorableModel;
import com.guaranacode.android.fastpicasabrowser.storage.ImageStorage;

/**
 * Task to download an image asynchronously in the background.
 *
 * @author abe@guaranacode.com
 *
 */
public class DownloadImageTask extends AsyncTask<IStorableModel, Integer, Bitmap> {
	private ImageView mView;
	private String mUrl;
	
	public DownloadImageTask(ImageView view) {
		mView = view;
	}
	
	@Override
	protected Bitmap doInBackground(IStorableModel... storableList) {
		mUrl = storableList[0].getUrl();
		
		Bitmap bitmap = ImageStorage.downloadThumbnailAndStoreLocally(storableList[0]);
		return bitmap;
	}

	protected void onPostExecute(Bitmap result) {
		if((null != mView) && (null != result) && (mUrl.equals((String)mView.getTag()))) {
			mView.setImageBitmap(result);
		}
	}
}
