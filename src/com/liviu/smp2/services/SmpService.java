package com.liviu.smp2.services;

import java.util.HashMap;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.liviu.smp2.controller.Controller;
import com.liviu.smp2.controller.interfaces.OnPlaylistStatusChanged;
import com.liviu.smp2.controller.interfaces.OnSmpPlayerCompletetionListener;
import com.liviu.smp2.controller.interfaces.OnSmpPlayerProgressChangedListener;
import com.liviu.smp2.data.Playlist;
import com.liviu.smp2.data.Song;
import com.liviu.smp2.managers.PlaylistManager;

public class SmpService extends Service implements OnErrorListener{
	
	// constants
	private final String 			TAG		=	"SmpService";
	
	// data
	private LocalBinder  			binder  = new LocalBinder();
	private SmpPlayer				player;
	private PlaylistManager			playlistManager;
	private int						activityID;
	
	// listeners
	private OnSmpPlayerProgressChangedListener	onSmpPlayerProgressChangedListener;
	private OnSmpPlayerCompletetionListener		onSmpPlayerCompletetionListener;
	private boolean								pauseSmpPlayerProgressChangedListener;

	@Override
	public void onCreate() {	
		super.onCreate();
				
		Log.e(TAG, "onCreate()");
		
		// init
		player								  = new SmpPlayer(this);
		playlistManager						  = new PlaylistManager(this);
		pauseSmpPlayerProgressChangedListener = false;
		activityID							  = Controller.MAIN_ACTIVITY_ID;
		
	}
	
	public void test(String text){
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// ====================== OVERRIDED ===========================
	@Override
	public IBinder onBind(Intent intent) {		
		Log.e(TAG, "onBind " + intent);
		return binder;
	}
	
	@Override
	public void onDestroy() {	
		super.onDestroy();
		Log.e(TAG,  "ondestroy");		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "onStartCommand Intent = " + intent + " flags = " + flags + " startId = " + startId);
		return super.onStartCommand(intent, flags, startId);
	}
	
	// inner classes
	public class LocalBinder extends Binder{
		public SmpService getService(){ return SmpService.this; }
	}

	public boolean nextSong() {
		player.play(playlistManager.getNextSong());		
		return true;		
	}

	public void setOnPlaylistStatusChanged(
			OnPlaylistStatusChanged onPlaylistStatusChanged) {
		Log.e(TAG, "setOnPlaylistStatusChanged " + onPlaylistStatusChanged);
		playlistManager.setOnPlaylistStatusChanged(onPlaylistStatusChanged);
		
	}
	
	public boolean isPlaying() {
		return player.isPlaying();
	}

	public void sendPlayerCommand(int command, int data) {
		switch (command) {
		case SmpPlayer.COMMAND_PLAY:
									Log.e(TAG, "sendPlayerCommand: PLAY " + command + " " + player.getCurrentPosition());
									if(!player.isDataSourceLoaded()){
										
										player 			= new SmpPlayer(this);																				
										Song tempSong1 	= playlistManager.getCurrentSong();
										
										if(tempSong1 != null){
											setPlayerListeners();
											player.play(tempSong1);											
										}
										else
											Log.e(TAG, "tempSong1 is null");
									}
									else
										player.play();	
									
									break;
		case SmpPlayer.COMMAND_PLAY_BY_ID:
									Log.e(TAG, "sendPlayerCommand: PLAY " + command + " " + player.getCurrentPosition() + " id: " + data);
									if(player.isPlaying()) player.stop();
									
									if(player.isDataSourceLoaded()){										
										player.reset();
										player.release();																												
									}
									
									player	       = new SmpPlayer(this);									
									Song tempSong2 = playlistManager.getSongWithID(data);
									
									if(tempSong2 != null){
										setPlayerListeners();
										player.play(tempSong2);
									}
									else
										Log.e(TAG, "tempSong2 is null");
									
									break;									
		case SmpPlayer.COMMAND_PAUSE:
									Log.e(TAG, "sendPlayerCommand: PAUSE " + command);
									try{
										if(player.isPlaying())
											player.pause();
									}
									catch (IllegalStateException e) {
										e.printStackTrace();										
									}
									break;
		case SmpPlayer.COMMAND_NEXT:
									Log.e(TAG, "sendPlayerCommand: NEXT " + command );
									if(player.isPlaying()) player.stop();
									
									if(player.isDataSourceLoaded()){										
										player.reset();
										player.release();																												
									}
									
									player 			= new SmpPlayer(this);									
									Song tempSong3 	= playlistManager.getNextSong();
									
									if(tempSong3 != null){
										setPlayerListeners();
										player.play(tempSong3);
									}
									else
										Log.e(TAG, "tempSong3 is null");
									
									break;
		case SmpPlayer.COMMAND_PREV:
									Log.e(TAG, "sendPlayerCommand: PREV " + command );
									if(player.isPlaying()) 
										player.stop();
									
									if(player.isDataSourceLoaded()){										
										player.reset();
										player.release();																												
									}
									
									player 			= new SmpPlayer(this);									
									Song tempSong4 	= playlistManager.getPrevSong();
									
									if(tempSong4 != null){
										setPlayerListeners();									
										player.play(tempSong4);
									}
									else
										Log.e(TAG, "tempSong4 is null");
									
									break;	
		case SmpPlayer.COMMAND_SEEK_TO:
									Log.e(TAG, "sendPlayerCommand: SEEK_TO " + command + " " + data * 1000);
									player.seekTo(data * 1000);
									break;
		default:
			break;
		}
	}

	public void setPlayerListeners(){
		
		// set listeners
		player.setOnErrorListener(this);
		player.pauseSmpPlayerProgressChangedListener(pauseSmpPlayerProgressChangedListener);
		player.setOnSmpPlayerProgressChanged(onSmpPlayerProgressChangedListener);
		player.setOnSmpPlayerCompletetionListener(activityID, onSmpPlayerCompletetionListener);		
		
	}
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(TAG, "onError: what = " + what + " extra = " + extra);
		return false;
	}

	public void setOnSmpPlayerProgressChanged(OnSmpPlayerProgressChangedListener onSmpPlayerProgressChanged_) {
		Log.e(TAG, "here: + " + onSmpPlayerProgressChanged_);
		onSmpPlayerProgressChangedListener = onSmpPlayerProgressChanged_;
		player.setOnSmpPlayerProgressChanged(onSmpPlayerProgressChangedListener);
	}

	public void setOnSmpPlayerCompletetionListener(int activityID_, OnSmpPlayerCompletetionListener listener) {
		final OnSmpPlayerCompletetionListener userListener = listener;
		
		setActivityID(activityID_);
		onSmpPlayerCompletetionListener = new OnSmpPlayerCompletetionListener() {
			
			@Override
			public void onPlayerComplete(MediaPlayer mp, Song song) {
				if(song == null){
					Log.e(TAG, "aici song is null");
					return;
				}
				else
					Log.e(TAG, "Aici song is: " + song);
				// this is the song what is finished
				playlistManager.updatePlayingCount(song);
				// this is the first song after the current one is finished
				sendPlayerCommand(SmpPlayer.COMMAND_NEXT, -1);
				userListener.onPlayerComplete(mp, song);				
			}
		};
		Log.e(TAG, "activityID in SmpService is: " + activityID + " " + onSmpPlayerCompletetionListener + " " + listener);
		player.setOnSmpPlayerCompletetionListener(activityID_, onSmpPlayerCompletetionListener);
		
	}

	public void pauseSmpPlayerProgressChangedListener(boolean pause) {
		
		player.pauseSmpPlayerProgressChangedListener(pause);
	}

	public HashMap<Integer, Song> getCurrentPlaylistAsHashmap() {
		Playlist			   tempPls  = playlistManager.getCurrentPlaylist();
		
		if(tempPls != null){
			return tempPls.getSongs();
		}
		else
			return null;
	}
	
	public void setActivityID(int activity_id){		
		playlistManager.setActivityID(activity_id);
		player.setActivityID(activity_id);
		activityID = activity_id;
	}
	
	public int getActivityID(){
		return activityID;
	}

	public void releaseListeners() {
		playlistManager.releaseListeners();
	}

	public void playSongWithID(int id) {
		player.play(playlistManager.getSongWithID(id));
	}

	public Song getCurrentSong() {
		return playlistManager.getCurrentSong();
		
	}

	public void setSongFavorite(boolean isFavorite, Song song) {
		playlistManager.setSongFavorite(isFavorite, song);
	}

	public void setSongIgnore(boolean isIgnored, Song ignSong) {
		playlistManager.setSongIgnore(isIgnored, ignSong);
		
	}

	public void setSongRate(int newRate, Song song) {
		playlistManager.setSongRate(newRate, song);
	}


}
