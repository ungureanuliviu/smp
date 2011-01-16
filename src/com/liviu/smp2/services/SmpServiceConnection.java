package com.liviu.smp2.services;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.liviu.smp2.controller.interfaces.OnPlaylistStatusChanged;
import com.liviu.smp2.controller.interfaces.OnSmpPlayerCompletetionListener;
import com.liviu.smp2.controller.interfaces.OnSmpPlayerProgressChangedListener;
import com.liviu.smp2.controller.interfaces.OnSmpServiceConnected;
import com.liviu.smp2.data.Song;

public class SmpServiceConnection implements ServiceConnection{
	
	// constants
	private final String			TAG 		= "SmpServiceConnection";
	
	// data
	private SmpService				boundSmpService;
	private Context					context;
	private ArrayList<Runnable>		serviceTasks;
	private int						activityID;
	
	// listeners
	private OnSmpServiceConnected	onSmpServiceConnected;
	
	// flags
	private boolean					isBound;
	
	public SmpServiceConnection(Context context_, int activityID_) {
		Log.e(TAG, "constructor");
		
		context 			= context_;
		isBound 			= false;
		serviceTasks		= new ArrayList<Runnable>();
		activityID			= activityID_;
		context.startService(new Intent(context, SmpService.class));
	}
	
	public void setOnSmpServiceConnected(OnSmpServiceConnected onSmpServiceConnected) {
		this.onSmpServiceConnected = onSmpServiceConnected;
	}
	
	public void startService() {
		
	}
	
	public void bindSmpService(){
		context.bindService(new Intent(context, SmpService.class), this, Context.BIND_AUTO_CREATE);
		isBound = true;
	}
	
	public void unbindSmpService(){
		if(isBound){
			context.unbindService(this);
			isBound = false;
		}
	}
	

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		boundSmpService = ((SmpService.LocalBinder)service).getService();
		
		Log.e(TAG, "service bound");
		
		// run service tasks
		for(int i = 0; i < serviceTasks.size(); i++)
			serviceTasks.get(i).run();
		
		// all tasks are done. we can release them
		serviceTasks.clear();
		
		// trigger the listener
		if(onSmpServiceConnected != null)
			onSmpServiceConnected.onSmpServiceConnected(name, service, boundSmpService);
		
		if(boundSmpService != null)
			boundSmpService.setActivityID(activityID);
		else
			Log.e(TAG, "cannot set activityID for service: " + boundSmpService);
		
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.e(TAG, "onServiceDisconnected");
		boundSmpService = null;
	}


	public boolean nextSong() {
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in run:D");
					boundSmpService.nextSong();
				}
			});
			
			return true;
		}
		else if(isBound && boundSmpService != null){
			return boundSmpService.nextSong();
		}
		else
			return false;
	}

	// setam un listener pentru a sti daca playlist-ul este in curs de incarcare sau a fost incarcat
	public void setOnPlaylistStatusChanged( OnPlaylistStatusChanged onPlaylistStatusChanged ) {
		Log.e(TAG, "setOnPlaylistStatusChanged " + onPlaylistStatusChanged + " isBound " + isBound + " service: " + boundSmpService);
		final OnPlaylistStatusChanged playlistListener = onPlaylistStatusChanged;
		
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in run2:D");
					boundSmpService.setOnPlaylistStatusChanged(playlistListener);					
				}
			});		
		}
		else
			boundSmpService.setOnPlaylistStatusChanged(playlistListener);	
	}

	public boolean isPlaying() {
		if(boundSmpService == null)
			return false;
		else 
			return boundSmpService.isPlaying();
	}

	public void sendPlayerCommand(int command_, int data_) {
		final int command  = command_;
		final int data	   = data_;
		
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in sendPlayerCommand1 " + command + " data: " + data);
					boundSmpService.sendPlayerCommand(command, data);
				}
			});
		}
		else if(isBound && boundSmpService != null){
			Log.e(TAG, "in sendPlayerCommand2 " + command + " data: " + data);
			boundSmpService.sendPlayerCommand(command, data);
		}
	}

	public void setOnSmpPlayerProgressChanged(OnSmpPlayerProgressChangedListener onSmpPlayerProgressChanged_) {
		final OnSmpPlayerProgressChangedListener onSmpPlayerProgressChanged = onSmpPlayerProgressChanged_;
		Log.e(TAG, "here: + " + onSmpPlayerProgressChanged);
		
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in run setOnSmpPlayerProgressChangedListener:D");
					boundSmpService.setOnSmpPlayerProgressChanged(onSmpPlayerProgressChanged);
				}
			});
		}
		else if(isBound && boundSmpService != null){
			boundSmpService.setOnSmpPlayerProgressChanged(onSmpPlayerProgressChanged);
		}
		else
			Log.e(TAG, "test");
	}

	public void setOnSmpPlayerCompletetionListener(int activityID_, OnSmpPlayerCompletetionListener listener) {
		final OnSmpPlayerCompletetionListener localListener = listener;
		Log.e(TAG, "setOnSmpPlayerCompletetionListener: + " + localListener + " activityID_ " + activityID_);
		activityID = activityID_;
		
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in run setOnSmpPlayerCompletetionListener");
					boundSmpService.setOnSmpPlayerCompletetionListener(activityID, localListener);
				}
			});
		}
		else if(isBound && boundSmpService != null){
			boundSmpService.setOnSmpPlayerCompletetionListener(activityID, localListener);
		}
		else
			Log.e(TAG, "test2");		
	}

	public void pauseSmpPlayerProgressChangedListener(boolean pause) {
		final boolean local = pause;
		Log.e(TAG, "here: + " + local);
		
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in run pauseSmpPlayerProgressChangedListener");
					boundSmpService.pauseSmpPlayerProgressChangedListener(local);
				}
			});
		}
		else if(isBound && boundSmpService != null){
			boundSmpService.pauseSmpPlayerProgressChangedListener(local);
		}
		else
			Log.e(TAG, "test3");				
		
	}

	public HashMap<Integer, Song> getCurrentPlaylistAsHashmap() {
		 if(isBound && boundSmpService != null){
			 return boundSmpService.getCurrentPlaylistAsHashmap();
		 }
		 else{
			 Log.e(TAG, "getCurrentPlaylistAsHashmap(): service is not bounded: " + boundSmpService + " " + isBound);
			 return null;
		 }
	}	
	
	public void setActivityID(int activity_id){
		final int tempActivityId = activity_id;
		
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in run setactivityID: " + tempActivityId);
					boundSmpService.setActivityID(tempActivityId);
				}
			});
		}
		else if(isBound && boundSmpService != null){
			boundSmpService.setActivityID(tempActivityId);
		}
		else
			Log.e(TAG, "setactivityID  " + tempActivityId);		
	}
	
	public int getActivityID(){
		return boundSmpService.getActivityID();
	}

	public void releaseListeners() {
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in run: releaseListeners");
					boundSmpService.releaseListeners();
				}
			});
		}
		else if(isBound && boundSmpService != null){
			boundSmpService.releaseListeners();
		}
	}

	public void playSongWithID(int id_) {
		final int id = id_;
		
		if(isBound && boundSmpService == null){
			serviceTasks.add(new Runnable() {				
				@Override
				public void run() {
					Log.e(TAG, "in run: playSongWithID: " + id);
					boundSmpService.playSongWithID(id);
				}
			});
		}
		else if(isBound && boundSmpService != null){
			boundSmpService.playSongWithID(id);
		}
		
	}

	public Song getCurrentSong() {	
		
		if(isBound && boundSmpService != null){
			return boundSmpService.getCurrentSong();
		}
		else
			return null;
		}

	public void setSongFavorite(boolean isFavorite, Song song) {
		boundSmpService.setSongFavorite(isFavorite, song);
	}

	public void setSongIgnore(boolean isIgnored, Song ignSong) {
		boundSmpService.setSongIgnore(isIgnored, ignSong);
		
	}

	public void setSongRate(int newRate, Song song) {
		boundSmpService.setSongRate(newRate, song);		
	}


}
