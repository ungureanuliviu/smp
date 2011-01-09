package com.liviu.smp2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.liviu.smp2.adapters.PlaylistAdapter;
import com.liviu.smp2.controller.Controller;
import com.liviu.smp2.controller.interfaces.OnPlaylistStatusChanged;
import com.liviu.smp2.controller.interfaces.OnSmpPlayerCompletetionListener;
import com.liviu.smp2.data.Playlist;
import com.liviu.smp2.data.Song;
import com.liviu.smp2.services.SmpPlayer;

public class PlaylistActivity extends Activity implements OnPlaylistStatusChanged,
														  OnItemClickListener,
														  OnSmpPlayerCompletetionListener,
														  OnGestureListener,
														  OnDoubleTapListener{

	// constants
	private final String 			TAG			= "PlaylistActivity";
	
	// data
	private Controller				controller;
	private PlaylistAdapter			adapter;
	private int						position;
	
	// views
	private ListView				listView;
	private ProgressDialog			loadingDialog;
	
	// listeners
	private GestureDetector			gestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate()");
		
        requestWindowFeature(Window.FEATURE_NO_TITLE);    
        setContentView(R.layout.main_playlist);
		
		controller		= new Controller(this, Controller.PLAYLIST_ACTIVITY_ID);
		adapter			= new PlaylistAdapter(this);
		listView		= (ListView)findViewById(R.id.songs_list);
		loadingDialog	= new ProgressDialog(this);
		gestureDetector	= new GestureDetector(this);
		position	 	= 0;
		
		
		// setting up
		loadingDialog.setTitle(getText(R.string.loading_bar_text));
		loadingDialog.setCancelable(false);
		loadingDialog.setCanceledOnTouchOutside(false);
		gestureDetector.setOnDoubleTapListener(this);
		adapter.setGestureDetector(gestureDetector);
		
		// actions
		loadingDialog.show();
		
		controller.setOnPlaylistStatusChanged(this);
		controller.setOnSmpPlayerCompletetionsListener(this);
		listView.setOnItemClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG, "onDestroy()");
		controller.onDestroyController(Controller.PLAYLIST_ACTIVITY_ID);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.e(TAG, "onPause()");
		controller.onPauseController(Controller.PLAYLIST_ACTIVITY_ID);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume()");
		controller.onResumeController();
	}

	@Override
	public void onPlaylistStatusChanged(Playlist playlist, int status) {
		Log.e(TAG, "onPlaylistStatusChangeed: " + status);		
		switch (status) {
		case Playlist.STATUS_PLAYLIST_LOAD_FINISHED:
						adapter.setItems(controller.getCurrentPlaylistAsHashmap());
						listView.setAdapter(adapter);
						loadingDialog.dismiss();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View convertView, int position_, long itemID) {
		Log.e(TAG, "onItemClick");
		position = position_;
	}

	@Override
	public void onPlayerComplete(MediaPlayer mp, Song song) {
		
		Log.e(TAG, "onPlayerComplete: " + song);
		Song tempSong = controller.getCurrentSong();
		Log.e(TAG, "tempSong: " + tempSong);
		
		for(int i = 0; i < adapter.getCount(); i++){
			if(adapter.getItem(i).isPlaying()){				
				adapter.getItem(i).setPlaying(false);					
			}
						
			if(tempSong != null)
				if(adapter.getItem(i).getId() == tempSong.getId())
					adapter.getItem(i).setPlaying(true);
		}
		
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.e(TAG, "onDown");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		Log.e(TAG, "onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Log.e(TAG, "onSingleTapUp");
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.e(TAG, "onScroll");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		Log.e(TAG, "onLongPress");
		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.e(TAG, "onFling");
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.e(TAG, "onSingleTapConfirmed: position " + position + " " + adapter.getItem(position));

		controller.sendPlayerCommand(SmpPlayer.COMMAND_PLAY_BY_ID, adapter.getItem(position).getId());
		for(int i = 0; i < adapter.getCount(); i++){
			if(adapter.getItem(i).isPlaying() && adapter.getItem(i).getId() != adapter.getItem(position).getId()){
				adapter.getItem(i).setPlaying(false);
			}
		}
		
		adapter.getItem(position).setPlaying(true);
		adapter.notifyDataSetChanged();		
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		Log.e(TAG, "onDoubleTap");
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		Log.e(TAG, "onDoubleTapEvent");
		return false;
	}
	
}
