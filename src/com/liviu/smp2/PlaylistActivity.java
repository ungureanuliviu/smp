package com.liviu.smp2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.liviu.smp2.adapters.PlaylistAdapter;
import com.liviu.smp2.controller.Controller;
import com.liviu.smp2.controller.interfaces.OnPlaylistStatusChanged;
import com.liviu.smp2.data.Playlist;
import com.liviu.smp2.services.SmpPlayer;

public class PlaylistActivity extends Activity implements OnPlaylistStatusChanged,
														  OnItemClickListener{

	// constants
	private final String 			TAG			= "PlaylistActivity";
	
	// data
	private Controller				controller;
	private PlaylistAdapter			adapter;
	
	// views
	private ListView				listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate()");
		
        requestWindowFeature(Window.FEATURE_NO_TITLE);    
        setContentView(R.layout.main_playlist);
		
		controller		= new Controller(this, Controller.PLAYLIST_ACTIVITY_ID);
		adapter			= new PlaylistAdapter(this);
		listView		= (ListView)findViewById(R.id.songs_list);
		
		controller.setOnPlaylistStatusChanged(this);
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
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View convertView, int position, long itemID) {
		Log.e(TAG, "position: " + position + " " + adapter.getItem(position));
		controller.sendPlayerCommand(SmpPlayer.COMMAND_PLAY_BY_ID, adapter.getItem(position).getId());
	}
	
}
