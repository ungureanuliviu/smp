package com.liviu.smp2.managers;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.liviu.smp2.controller.Controller;
import com.liviu.smp2.controller.interfaces.OnPlaylistStatusChanged;
import com.liviu.smp2.data.Playlist;
import com.liviu.smp2.data.Song;
import com.liviu.smp2.util.Constants;

public class PlaylistManager {
	
	// constants
	private final String		TAG							= "PlaylistManager";
	private final int 			MSG_PLAYLIST_LOADED			= 1;
	
	
	// data
	private int   						currentPlaylistPosition		= 0;
	private Playlist					currentPlaylist;
	private Context 					context;
	private ContentResolver				contentResolver;
	private DatabaseManager				dbManager;
	private Handler						handler;
	private int							activityID;
	
	// listeners
	private HashMap<Integer, OnPlaylistStatusChanged> onPlaylistStatusChangedListenersMap;
	
	public PlaylistManager(Context context_) {
	
		Log.e(TAG, "constructor");
		
		context 								= context_;
		contentResolver							= context.getContentResolver();
		currentPlaylist							= new Playlist();
		currentPlaylistPosition					= 0;
		dbManager								= new DatabaseManager(context);
		onPlaylistStatusChangedListenersMap 	= new HashMap<Integer, OnPlaylistStatusChanged>();
		activityID								= Controller.MAIN_ACTIVITY_ID;
		
		handler						= new Handler(){
			public void handleMessage(android.os.Message msg) {
				switch (msg.what) {
				case MSG_PLAYLIST_LOADED:
										if(onPlaylistStatusChangedListenersMap.get(activityID) != null)
											onPlaylistStatusChangedListenersMap.get(activityID).onPlaylistStatusChanged(currentPlaylist, Playlist.STATUS_PLAYLIST_LOAD_FINISHED);
										resetCurrentPlaylistPosition(currentPlaylist.getID());
										break;

				default:
					break;
				}
			}
		};
		loadInitialPlaylist(-1);
	}
	
	public void printAllPlaylists(){
		dbManager.crGetAllPlaylists();
	}

	public Song getNextSong() {
		Log.e(TAG, "getNextSong: " + currentPlaylist.isEmpty() + " " + currentPlaylist.getCount() + " position: " + currentPlaylistPosition);
		if(currentPlaylist.isEmpty()){
			currentPlaylist.setName(Playlist.PLAYLIST_ALL);
			if(onPlaylistStatusChangedListenersMap.get(activityID) != null){
				onPlaylistStatusChangedListenersMap.get(activityID).onPlaylistStatusChanged(currentPlaylist, Playlist.STATUS_PLAYLIST_IS_LOADING);
			}
			
			Thread loadedPlaylist = new Thread(new Runnable() {				
				@Override
				public void run() {
					synchronized (dbManager) {
						currentPlaylist.setSongsData(dbManager.getAllSongs());						
						handler.sendEmptyMessage(MSG_PLAYLIST_LOADED);
					}
				}
			});
			loadedPlaylist.start();		
			return null;
		}
		else{
			if((currentPlaylistPosition + 1) > currentPlaylist.getCount() - 1)
				currentPlaylistPosition = 0;
			else
				currentPlaylistPosition++;
			
			return currentPlaylist.getSongAt(currentPlaylistPosition);
		}				
	}

	public Song getPrevSong() {
		Log.e(TAG, "getPrevSong: " + currentPlaylist.isEmpty() + " " + currentPlaylist.getCount() + " position: " + currentPlaylistPosition);
		if(currentPlaylist.isEmpty()){
			currentPlaylist.setName(Playlist.PLAYLIST_ALL);
			if(onPlaylistStatusChangedListenersMap.get(activityID) != null){
				onPlaylistStatusChangedListenersMap.get(activityID).onPlaylistStatusChanged(currentPlaylist, Playlist.STATUS_PLAYLIST_IS_LOADING);
			}
			
			Thread loadedPlaylist = new Thread(new Runnable() {				
				@Override
				public void run() {
					synchronized (dbManager) {
						currentPlaylist.setSongsData(dbManager.getAllSongs());						
						handler.sendEmptyMessage(MSG_PLAYLIST_LOADED);
					}
				}
			});
			loadedPlaylist.start();		
			return null;
		}
		else{
			if((currentPlaylistPosition - 1) < 0)
				currentPlaylistPosition = currentPlaylist.getCount() - 1;
			else
				currentPlaylistPosition--;
			
			return currentPlaylist.getSongAt(currentPlaylistPosition);
		}				
	}	
	
	/*
	 * aceasta pozitie trebuie citita dintr-un sharedpreference 
	 * ea este relativa la id-ul playlist-ului incarcat
	 */
	private void resetCurrentPlaylistPosition(int playlistID) {
		currentPlaylistPosition = 0;
	};

	public void setOnPlaylistStatusChanged(OnPlaylistStatusChanged onPlaylistStatusChanged_) {
		onPlaylistStatusChangedListenersMap.put(activityID, onPlaylistStatusChanged_);
		Log.e(TAG, "setOnPlaylistStatusChangedListener: " + onPlaylistStatusChanged_ + " currentPlaylist: " + currentPlaylist + " activityID: " + activityID);
		if(currentPlaylist != null)
			if(!currentPlaylist.isEmpty())
				if(onPlaylistStatusChangedListenersMap.get(activityID) != null){
					onPlaylistStatusChangedListenersMap.get(activityID).onPlaylistStatusChanged(currentPlaylist, Playlist.STATUS_PLAYLIST_LOAD_FINISHED);
					return;
				}
	}

	public Song getCurrentSong(){
		if(currentPlaylist.isEmpty()){
			currentPlaylist.setName(Playlist.PLAYLIST_ALL);
			if(onPlaylistStatusChangedListenersMap.get(activityID) != null){
				onPlaylistStatusChangedListenersMap.get(activityID).onPlaylistStatusChanged(currentPlaylist, Playlist.STATUS_PLAYLIST_IS_LOADING);
			}
			
			Thread loadPlaylistThread = new Thread(new Runnable() {				
				@Override
				public void run() {
					synchronized (dbManager) {
						currentPlaylist.setSongsData(dbManager.getAllSongs());						
						handler.sendEmptyMessage(MSG_PLAYLIST_LOADED);
					}
				}
			});
			loadPlaylistThread.start();		
			return null;
		}
		else{			
			return currentPlaylist.getSongAt(currentPlaylistPosition);
		}		
	}

	public Playlist getCurrentPlaylist() {		
		if(currentPlaylist != null)
			currentPlaylist.getSongAt(currentPlaylistPosition).setPlaying(true);
		return currentPlaylist;
	}
	
	private void loadInitialPlaylist(int playlistID){
		if(playlistID == -1){
			currentPlaylist.setName(Playlist.PLAYLIST_ALL);
			Thread loadedPlaylist = new Thread(new Runnable() {				
				@Override
				public void run() {
					synchronized (dbManager) {
						currentPlaylist.setSongsData(dbManager.getAllSongs());						
						handler.sendEmptyMessage(MSG_PLAYLIST_LOADED);
					}
				}
			});
			loadedPlaylist.start();		
		}
	}
	
	public void setActivityID(int activityID) {
		Log.e(TAG, "setActivityID: " + activityID);
		this.activityID = activityID;		
	}
	
	public int getActivityID() {
		return activityID;
	}

	public void releaseListeners() {
		Log.e(TAG, "releaseListeners: " + activityID);
		onPlaylistStatusChangedListenersMap.remove(activityID);
	}

	public Song getSongWithID(int id) {
		if(currentPlaylist != null){
			if(!currentPlaylist.isEmpty()){
				return currentPlaylist.getSongWithID(id);				
			}
			else
				return null;
		}
		else
			return null;
	}

}
