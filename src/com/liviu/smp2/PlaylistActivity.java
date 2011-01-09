package com.liviu.smp2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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
														  OnItemLongClickListener,
														  OnSmpPlayerCompletetionListener,
														  OnGestureListener,
														  OnDoubleTapListener{

	// constants
	private final String 			TAG			= "PlaylistActivity";
	
	// menu ids
	private final int				MENU_PLAY	= 1;
	private final int				MENU_RATE	= 2;
	private final int				MENU_FAV	= 3;
	private final int				MENU_IGNORE = 4;
	private final int				MENU_DELETE	= 5;
	
	// data
	private Controller				controller;
	private PlaylistAdapter			adapter;
	private int						adapterPosition;			
	
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
		adapterPosition = 0;
		
		
		// setting up
		loadingDialog.setTitle(getText(R.string.loading_bar_text));
		loadingDialog.setCancelable(false);
		loadingDialog.setCanceledOnTouchOutside(false);
		gestureDetector.setOnDoubleTapListener(this);
		adapter.setGestureDetector(gestureDetector);
		
		// actions
		loadingDialog.show();
		
		controller.setOnPlaylistStatusChanged(this);
		controller.setOnSmpPlayerCompletetionsListener(Controller.PLAYLIST_ACTIVITY_ID, this);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		registerForContextMenu(listView);
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
	public void onItemClick(AdapterView<?> arg0, View convertView, int position, long itemID) {
		Log.e(TAG, "onItemClick");		
		
		controller.sendPlayerCommand(SmpPlayer.COMMAND_PLAY_BY_ID, adapter.getItem(position).getId());
		for(int i = 0; i < adapter.getCount(); i++){
			if(adapter.getItem(i).isPlaying() && adapter.getItem(i).getId() != adapter.getItem(position).getId()){
				adapter.getItem(i).setPlaying(false);
			}
		}
		
		adapter.getItem(position).setPlaying(true);
		adapter.notifyDataSetChanged();
		adapterPosition = position;
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View convertView, int position, long arg3) {
		adapterPosition = position;
		return false;
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
		Log.e(TAG, "onSingleTapConfirmed");

		
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
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Song selectedSong = (Song)adapter.getItem(adapterPosition);
		
		if(selectedSong != null){
			menu.setHeaderTitle(selectedSong.getTitle());		
			menu.add(0, MENU_PLAY,   0, getText(R.string.context_menu_play));					
			menu.add(0, MENU_FAV,    1, (selectedSong.isFavorite() == true ? getText(R.string.context_menu_remove_from_fav) : getText(R.string.context_menu_add_to_fav)));
			menu.add(0, MENU_RATE,   2, getText(R.string.context_menu_rate));
			menu.add(0, MENU_IGNORE, 3, (selectedSong.isIgnored() == true ? getText(R.string.context_menu_remove_ignore) : getText(R.string.context_menu_add_to_ignore)));
			menu.add(0, MENU_DELETE, 4, getText(R.string.context_menu_delete));
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {	
		
		Log.e(TAG, "selected menu ID: " + item.getItemId());
		return super.onContextItemSelected(item);
	}


	
}
