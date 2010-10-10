package com.guaranacode.android.fastpicasabrowser.activities;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.GridView;
import android.widget.Toast;

import com.google.api.client.googleapis.GoogleTransport;
import com.guaranacode.android.fastpicasabrowser.adapters.PhotoGridAdapter;
import com.guaranacode.android.fastpicasabrowser.listeners.PhotoGridItemClickListener;
import com.guaranacode.android.fastpicasabrowser.picasa.model.PhotoEntry;
import com.guaranacode.android.fastpicasabrowser.tasks.DownloadPhotoList;
import com.guaranacode.android.fastpicasabrowser.util.AuthUtils;
import com.guaranacode.android.fastpicasabrowser.util.DialogUtil;
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
	
	public static final int PROGRESS_COMPLETE = 1;
	
	public static final int PROGRESS_NEW_PAGE = 2;

	public static final int PROGRESS_START = 3;
	
	public static final int PROGRESS_DBINSERT = 4;

	/**
	 * The amount by which we increase the album progress dialog whenever
	 * a new page is received from the Picasa API.
	 */
	private int mAmountPerPage = -1;
	
	/**
	 * The progress point where we started to add progress from inserting
	 * albums into the database.
	 */
	private int mAlbumDbInsertStart = -1;
	
	private ProgressDialog mProgressDialog;
	private Handler mProgressHandler = new Handler() {
		public void handleMessage(Message message) {
			if(null == mProgressDialog) {
				return;
			}
			
			handleProgressMessage(message);
		}
	};
	
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
		mProgressDialog = DialogUtil.createProgressDialog(getString(R.string.photos_loading), this);
		
		new DownloadPhotoList(this, mTransport, getApplicationContext(), mProgressHandler).execute(mAlbumId);
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
	
	/**
	 * Handle a message with information about the album update progress dialog.
	 * @param message
	 */
	protected void handleProgressMessage(Message message) {
		int numPages = -1;
		
		if((null != message) && (message.what != 0)) {
			switch(message.what) {
				case PROGRESS_START:
					Toast.makeText(getApplicationContext(), R.string.picasa_loading, Toast.LENGTH_LONG).show();
					mProgressDialog.incrementProgressBy(mProgressDialog.getMax() / 20);
					break;
				case PROGRESS_COMPLETE:
					DialogUtil.finishProgressDialog(mProgressDialog);
					break;
				case PROGRESS_DBINSERT:
					if(message.arg2 != 0) { // number of albums is not 0
						if(mAlbumDbInsertStart < 0) {
							mAlbumDbInsertStart = mProgressDialog.getProgress();
						}

						mProgressDialog.setProgress(mAlbumDbInsertStart + ((mAmountPerPage * message.arg1) / message.arg2));
					}
					break;
				case PROGRESS_NEW_PAGE:
					if(message.arg2 != 0) {
						if(mAmountPerPage < 0) { // Is this the first message?
							mProgressDialog.setProgress(mProgressDialog.getMax() / 3);
							
							if(numPages < 0) {
								numPages = message.arg2;
								int diff = DialogUtil.getRemainingProgress(mProgressDialog);
								mAmountPerPage = diff / 2;
							}
						}
						else { // This is neither the first nor the last message
							mProgressDialog.incrementProgressBy(mAmountPerPage);
						}
					}
					break;
			}
		}
	}
}
