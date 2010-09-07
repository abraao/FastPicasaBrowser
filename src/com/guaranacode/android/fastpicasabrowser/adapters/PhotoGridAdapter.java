package com.guaranacode.android.fastpicasabrowser.adapters;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.guaranacode.android.fastpicasabrowser.picasa.model.PhotoEntry;
import com.guaranacode.android.fastpicasabrowser.tasks.DownloadImageTask;
import com.guaranacode.android.fastpicasabrowser.R;

/**
 * Displays the photos within an album as a grid.
 *
 * @author abe@guaranacode.com
 *
 */
public class PhotoGridAdapter extends BaseAdapter {
	private Context mContext;
	private List<PhotoEntry> mPhotos;
	
	public PhotoGridAdapter(Context context, List<PhotoEntry> photos) {
		this.mContext = context;
		this.mPhotos = photos;
	}
	
	public int getCount() {
		if(null == this.mPhotos) {
			return 0;
		}
		
		return this.mPhotos.size();
	}

	public PhotoEntry getItem(int position) {
		if((null == this.mPhotos) || (position > this.mPhotos.size())) {
			return null;
		}

		return this.mPhotos.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		PhotoEntry photoEntry = getItem(position);
		if(null == photoEntry) {
			return null;
		}

    	convertView = new ImageView(mContext);

    	convertView.setLayoutParams(new GridView.LayoutParams(85, 85));
    	((ImageView)convertView).setScaleType(ImageView.ScaleType.CENTER_CROP);
    	convertView.setPadding(1, 1, 1, 1);

        ((ImageView)convertView).setImageDrawable(convertView.getResources().getDrawable(R.drawable.loading));
        convertView.setTag(photoEntry.getUrl());
        new DownloadImageTask((ImageView)convertView).execute(photoEntry);
        
        return convertView;
	}

	static class ViewHolder {
		ImageView imageView;
	}
}
