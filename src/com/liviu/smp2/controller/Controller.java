package com.liviu.smp2.controller;

import java.util.HashMap;

import android.R.integer;
import android.content.Context;
import android.util.Log;

import com.liviu.smp2.controller.interfaces.OnPlaylistStatusChanged;
import com.liviu.smp2.controller.interfaces.OnSmpPlayerCompletetionListener;
import com.liviu.smp2.controller.interfaces.OnSmpPlayerProgressChangedListener;
import com.liviu.smp2.data.Song;
import com.liviu.smp2.managers.DatabaseManager;
import com.liviu.smp2.services.SmpServiceConnection;

public class Controller {
	
	// constants	
	private final String 			TAG		= "Controller";
	private final Context			context;
	
	// flags
	public static final int			MAIN_ACTIVITY_ID		=	1;
	public static final int 		PLAYLIST_ACTIVITY_ID 	= 	2;
	
	
	// service
	private SmpServiceConnection	smpServiceConnection;
	
	// managers
	private DatabaseManager			dbManager;
	private int						activityID;


	public Controller(Context appContext_, int activityFlag_) {
		
		Log.e(TAG, "constructor: activityFlag = " + activityFlag_);

	// =========== common init =================		
		// main init
		context 			= appContext_;
		activityID 			= activityFlag_;
		
		// managers init
		dbManager			 = new DatabaseManager(context);		
		
		// service init
		smpServiceConnection = new SmpServiceConnection(context);						
		smpServiceConnection.bindSmpService();
		
		switch (activityFlag_) {
		case MAIN_ACTIVITY_ID:
								smpServiceConnection.setActivityID(MAIN_ACTIVITY_ID);
								break;
		case PLAYLIST_ACTIVITY_ID:
							    smpServiceConnection.setActivityID(PLAYLIST_ACTIVITY_ID);
							    break;

		default:
			break;
		}				
	}
	
	// player controls
	public boolean playNextSong(){
		Log.e(TAG, "playNextSong()");
		return smpServiceConnection.nextSong();
	}
	
	public boolean onPauseController(int activityFlag){
		
		Log.e(TAG, "releaseController(" + activityFlag + ")");
		smpServiceConnection.unbindSmpService();
		return false;
	}
	
	public void onDestroyController(int activityFlag){
		Log.e(TAG, "destroyController");
		smpServiceConnection.unbindSmpService();
		smpServiceConnection.releaseListeners();
	}

	public void setOnPlaylistStatusChanged(OnPlaylistStatusChanged onPlaylistStatusChanged) {
		Log.e(TAG, "setOnPlaylistStatusChanged " + onPlaylistStatusChanged);
		smpServiceConnection.setOnPlaylistStatusChanged(onPlaylistStatusChanged);
	}

	public void sendPlayerCommand(int command, int progress){
		Log.e(TAG, "sendPlayerCommand: " + command);
		smpServiceConnection.sendPlayerCommand(command, progress);
	}

	public boolean isPlaying() {
		return smpServiceConnection.isPlaying();
	}
	
	public void onResumeController(){
		smpServiceConnection.bindSmpService();
		smpServiceConnection.setActivityID(activityID);
	}

	public void setOnSmpPlayerProgressChanged(OnSmpPlayerProgressChangedListener onSmpPlayerProgressChanged) {
		smpServiceConnection.setOnSmpPlayerProgressChanged(onSmpPlayerProgressChanged);
	}

	public void setOnSmpPlayerCompletetionsListener(OnSmpPlayerCompletetionListener listener) {
		smpServiceConnection.setOnSmpPlayerCompletetionListener(listener);
	}

	public void pauseSmpPlayerProgressChangedListener(boolean pause) {
		smpServiceConnection.pauseSmpPlayerProgressChangedListener(pause);
	}

	public HashMap<Integer, Song> getCurrentPlaylistAsHashmap() {
		return smpServiceConnection.getCurrentPlaylistAsHashmap();
	}
	/*
	private void playSongWithID(int id) {
		smpServiceConnection.playSongWithID(id);
	}
	*/
}
