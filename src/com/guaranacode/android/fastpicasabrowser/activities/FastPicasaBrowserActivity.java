package com.guaranacode.android.fastpicasabrowser.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpResponseException;
import com.google.api.data.picasa.v2.PicasaWebAlbums;
import com.guaranacode.android.fastpicasabrowser.R;
import com.guaranacode.android.fastpicasabrowser.adapters.AlbumListAdapter;
import com.guaranacode.android.fastpicasabrowser.database.DatabaseHelper;
import com.guaranacode.android.fastpicasabrowser.picasa.model.AlbumEntry;
import com.guaranacode.android.fastpicasabrowser.storage.ImageStorage;
import com.guaranacode.android.fastpicasabrowser.tasks.DownloadAlbumList;
import com.guaranacode.android.fastpicasabrowser.util.AlbumComparator;
import com.guaranacode.android.fastpicasabrowser.util.AuthUtils;
import com.guaranacode.android.fastpicasabrowser.util.DialogUtil;
import com.guaranacode.android.fastpicasabrowser.util.AlbumComparator.AlbumField;

/**
 * Main activity for the application. Authenticates against Picasa API.
 *
 * @author abe@guaranacode.com
 *
 */
public final class FastPicasaBrowserActivity extends ListActivity {

	private static final int REQUEST_AUTHENTICATE = 0;
	private static final String PREF = "FastPicasaBrowserPrefs";
	private static final int GOOGLE_ACCOUNTS_DIALOG = 0;

	private static GoogleTransport transport;
	private String authToken;
	private final List<AlbumEntry> albums = new ArrayList<AlbumEntry>();

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

	/**
	 * Set up the HTTP transport to use the Atom parser.
	 */
	public FastPicasaBrowserActivity() {
		transport = AuthUtils.buildTransport();
	}

	/**
	 * This is the second thing called (after the constructor) once the application is started.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getAccount(false);
	}

	/**
	 * Show the dialog to select the account to use.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case GOOGLE_ACCOUNTS_DIALOG:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Select a Google account");
	
				final AccountManager manager = AccountManager.get(this);
				final Account[] accounts = manager.getAccountsByType("com.google");
	
				final int size = accounts.length;
				String[] names = new String[size];
	
				for (int i = 0; i < size; i++) {
					names[i] = accounts[i].name;
				}
	
				builder.setItems(names, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						gotAccount(manager, accounts[which]);
					}
				});
	
				return builder.create();
		}

		return null;
	}

	/**
	 * This is better named as "getAccount". This gets the google account and authenticates it.
	 * 
	 * @param tokenExpired
	 */
	private void getAccount(boolean tokenExpired) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		String accountName = settings.getString("accountName", null);

		this.setTitle(accountName);

		if (accountName != null) {
			AccountManager manager = AccountManager.get(this);
			Account[] accounts = manager.getAccountsByType("com.google");

			int size = accounts.length;

			for (int i = 0; i < size; i++) {
				Account account = accounts[i];

				if (accountName.equals(account.name)) {
					if (tokenExpired) {
						manager.invalidateAuthToken("com.google", this.authToken);
					}

					gotAccount(manager, account);

					return;
				}
			}
		}

		showDialog(GOOGLE_ACCOUNTS_DIALOG);
	}

	/**
	 * Once we have the account, store the name of the account used in the preferences for this application
	 * and authenticate the account.
	 * 
	 * @param manager
	 * @param account
	 */
	private void gotAccount(AccountManager manager, Account account) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString("accountName", account.name);
		editor.commit();

		try {
			// This breaks in Android 2.2. That version of Android forbids blocking calls in the main thread of the application.
			Bundle bundle = manager.getAuthToken(account, PicasaWebAlbums.AUTH_TOKEN_TYPE, true, null, null).getResult();

			if (bundle.containsKey(AccountManager.KEY_INTENT)) {
				Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);

				int flags = intent.getFlags();
				flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;

				intent.setFlags(flags);

				startActivityForResult(intent, REQUEST_AUTHENTICATE);
			} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
				authenticatedClientLogin(bundle.getString(AccountManager.KEY_AUTHTOKEN));
			}
		} catch (Exception e) {
			handleException(e);
			return;
		}
	}

	/**
	 * Once authenticated, add the token to the transport.
	 * 
	 * @param authToken
	 */
	private void authenticatedClientLogin(String authToken) {
		this.authToken = authToken;
		transport.setClientLoginToken(authToken);

		displayAlbums();
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);

		AlbumEntry albumEntry;

		if(position < this.albums.size()) {
			albumEntry = this.albums.get(position);
		}
		else {
			return;
		}

		Bundle extraData = new Bundle();
		extraData.putString("authToken", this.authToken);
		extraData.putString("albumId", albumEntry.albumId);
		extraData.putString("albumTitle", albumEntry.title);

		Intent displayPhotosIntent = new Intent(this.getApplicationContext(), PhotoGridActivity.class);	
		displayPhotosIntent.putExtras(extraData);

		this.startActivity(displayPhotosIntent);
	}

	/**
	 * Get the list of albums.
	 */
	private void displayAlbums() {
		mProgressDialog = DialogUtil.createProgressDialog(getString(R.string.albums_loading), this);

		new DownloadAlbumList(this, transport, getApplicationContext(), mProgressHandler).execute();
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

	/**
	 * Set the albums displayed in this activity.
	 * @param albumList
	 * @param isRefresh	True if the albumList is the same list as the albums instance variable.
	 */
	public void setAlbums(List<AlbumEntry> albumList, boolean isRefresh) {
		if(!isRefresh && (null != albumList)) {
			this.albums.clear();
			this.albums.addAll(albumList);
		}

		setListAdapter(new AlbumListAdapter(this, this.albums));
	}

	private void handleException(Exception e) {
		e.printStackTrace();

		if (e instanceof HttpResponseException) {
			int statusCode = ((HttpResponseException) e).response.statusCode;

			if (statusCode == 401 || statusCode == 403) {
				getAccount(true);
			}

			return;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.album_browser_menu, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_cache:
			clearCache();
			return true;
		case R.id.sort_albums_by_title:
			sortAlbumsByTitle();
			return true;
		case R.id.sort_albums_by_update:
			sortAlbumsByLastUpdate();
			return true;
		case R.id.quit_app:
			quitApp();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Clear the photo thumbnails cache and the list of albums/photos cached in the database.
	 */
	private void clearCache() {
		try {
			// Clear thumbnails
			ImageStorage.deleteStoredImages();

			// Clear database
			DatabaseHelper dbh = new DatabaseHelper(this.getApplicationContext());
			dbh.clearDatabase(dbh.getWritableDatabase());
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

		quitApp();
	}

	/**
	 * Sort the albums in the browser by name.
	 */
	private void sortAlbumsByTitle() {
		Collections.sort(this.albums, new AlbumComparator(AlbumField.TITLE));
		this.setAlbums(this.albums, true);
	}

	/**
	 * Sort albums by the date of last update.
	 */
	private void sortAlbumsByLastUpdate() {
		Collections.sort(this.albums, new AlbumComparator(AlbumField.LAST_UPDATE));
		this.setAlbums(this.albums, true);
	}

	/**
	 * Exit the application.
	 */
	private void quitApp() {
		this.finish();
	}
}
