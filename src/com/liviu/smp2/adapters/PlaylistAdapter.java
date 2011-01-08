package com.liviu.smp2.adapters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.liviu.smp2.R;
import com.liviu.smp2.data.Song;
import com.liviu.smp2.ui.ViewHolder;

public class PlaylistAdapter extends BaseAdapter{
	
	// constants
	private final String 			TAG = "PlaylistAdapter";
	
	// data
	private HashMap<Integer, Song>  items;
	private HashMap<Integer, Bitmap> itemsBitmaps;
	private Context					context;
	private BitmapFactory.Options	options;
	// services
	private LayoutInflater			lf;
	
	// views
	private ViewHolder				viewHolder;
	
	
	public PlaylistAdapter(Context context_) {
		
		context		 = context_;
		lf 		 	 = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		items		 = new HashMap<Integer, Song>();
		itemsBitmaps = new HashMap<Integer, Bitmap>();
		viewHolder	 = new ViewHolder(context_);
		options		 = new BitmapFactory.Options();
		
		options.inSampleSize = 4;
	}
	
	public PlaylistAdapter	addItem(Song song){
		int currentSize = items.size();
		
		items.put(currentSize, song);
		
		if(song.getImagePath() != null){
			Log.e(TAG, "imagePath is: " + song.getImagePath() + " song: "  + song.getTitle());
			if(!itemsBitmaps.containsKey(song.getAlbumId())){
				if(song.getImagePath().length() > 0){
					Bitmap b = BitmapFactory.decodeFile(song.getImagePath(), options);
					itemsBitmaps.put((int) song.getAlbumId(), b);
				}
				else{
					Log.e(TAG, "length < 0: " + song.getImagePath().length() + " song title: " + song.getTitle());
				}
			}
			else{
				Log.e(TAG, "we already have this bitmap: " + song.getTitle());
			}
		}
		else{
			Log.e(TAG, "path is null for " + song.getTitle());
		}
		
		return this;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Song getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public PlaylistAdapter setItems(HashMap<Integer, Song> items_){
		items.clear();
		
		Set<Integer> keySet 			= items_.keySet();
		Iterator<Integer> keyIterator 	= keySet.iterator();
		int key;	
		
		while(keyIterator.hasNext()){
			key = keyIterator.next();
			addItem(items_.get(key));
		}
		
		return this;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView == null){
			convertView	= (View)lf.inflate(R.layout.playlist_item, null);
			viewHolder 	= new ViewHolder(context);
			
			viewHolder.songImage = (ImageView)convertView.findViewById(R.id.albumImageHolder);
			viewHolder.songTitle = (TextView)convertView.findViewById(R.id.titleTextHolder);
			viewHolder.songArtist = (TextView)convertView.findViewById(R.id.artistTextHolder);
			
			convertView.setTag(viewHolder);
		}
		else
			viewHolder = (ViewHolder)convertView.getTag();
		
		viewHolder.songTitle.setText(items.get(position).getTitle());
		viewHolder.songArtist.setText(items.get(position).getArtist());
		if(items.get(position).getImagePath() != null){
			if(items.get(position).getImagePath().length() > 0){
				if(itemsBitmaps.get((int)items.get(position).getAlbumId()) != null){
					Log.e(TAG, "we have image: " + items.get(position).getTitle() + " " + itemsBitmaps.get((int)items.get(position).getAlbumId()));
					viewHolder.songImage.setImageBitmap(itemsBitmaps.get((int)items.get(position).getAlbumId()));
				}
				else{
					Log.e(TAG, "we don't have a image for " + items.get(position).getTitle());
					viewHolder.songImage.setImageDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
					
				}
			}
			else{
				viewHolder.songImage.setImageDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
			}
		}
		else{
			viewHolder.songImage.setImageDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
		}
		
		return convertView;
	}
}
