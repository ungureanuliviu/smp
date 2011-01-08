package com.liviu.smp2.ui;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
	public ImageView	songImage;
	public TextView		songTitle;
	public TextView		songArtist;
	
	public ViewHolder(Context context_) {
		songImage	= new ImageView(context_);
		songTitle	= new TextView(context_);
		songArtist	= new TextView(context_);
	}
}
