package com.guaranacode.android.fastpicasabrowser.activities;

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.http.HttpResponseException;
import com.google.api.data.picasa.v2.PicasaWebAlbums;
import com.guaranacode.android.fastpicasabrowser.adapters.AlbumListAdapter;
import com.guaranacode.android.fastpicasabrowser.picasa.model.AlbumEntry;
import com.guaranacode.android.fastpicasabrowser.tasks.DownloadAlbumList;
import com.guaranacode.android.fastpicasabrowser.util.AuthUtils;

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
		Toast.makeText(getApplicationContext(), "Loading all albums", Toast.LENGTH_LONG).show();

		new DownloadAlbumList(this, transport, getApplicationContext()).execute();
	}
	
	/**
	 * Set the albums displayed in this activity.
	 * @param albumList
	 */
	public void setAlbums(List<AlbumEntry> albumList) {
		List<AlbumEntry> albums = albumList;

		if(null != albums) {
			this.albums.clear();
			this.albums.addAll(albums);
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
}