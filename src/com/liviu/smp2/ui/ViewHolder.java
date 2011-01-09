package com.liviu.smp2.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
	public ImageView	songImage;
	public TextView		songTitle;
	public TextView		songArtist;
	public View			songIsPlaying;
	
	public ViewHolder(Context context_) {
		songImage	   = new ImageView(context_);
		songTitle	   = new TextView(context_);
		songArtist	   = new TextView(context_);
		songIsPlaying  = new View(context_);
	}
}
