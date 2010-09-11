package com.guaranacode.android.fastpicasabrowser.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.guaranacode.android.fastpicasabrowser.picasa.model.AlbumEntry;
import com.guaranacode.android.fastpicasabrowser.storage.ImageStorage;
import com.guaranacode.android.fastpicasabrowser.R;

/**
 * Adapter for displaying a list of albums. Each album in the list
 * is represented as a row with its thumbnail and title.
 *
 * @author abe@guaranacode.com
 *
 */
public class AlbumListAdapter extends BaseAdapter {
	private List<AlbumEntry> mAlbums;
	private Context mContext;
	
	public AlbumListAdapter(Context context, List<AlbumEntry> albums) {
		super();
		this.mContext = context;
		this.mAlbums = albums;
	}

	public int getCount() {
		if(null == mAlbums) {
			return 0;
		}
		
		return mAlbums.size();
	}

	public AlbumEntry getItem(int position) {
		if((null == mAlbums) || (position > (mAlbums.size() - 1))) {
			return null;
		}
		
		return mAlbums.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		AlbumEntry albumEntry = getItem(position);
		if(null == albumEntry) {
			return null;
		}
		
		ViewHolder viewHolder;
		
		if(null == convertView) {
			convertView = View.inflate(this.mContext, R.layout.album_entry_view, null);
			
			viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) convertView.findViewById(android.R.id.text1);
			viewHolder.imageView = (ImageView) convertView.findViewById(R.id.image1);
			
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		viewHolder.textView.setText(albumEntry.title);
		viewHolder.imageView.setImageDrawable(convertView.getResources().getDrawable(R.drawable.loading));
		viewHolder.imageView.setTag(albumEntry.getUrl());

		ImageStorage.setImageThumbnail(albumEntry, viewHolder.imageView);

		return convertView;
	}
	
	static class ViewHolder {
		TextView textView;
		ImageView imageView;
	}
}
