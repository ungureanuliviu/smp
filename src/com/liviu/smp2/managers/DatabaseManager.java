package com.liviu.smp2.managers;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.liviu.smp2.data.Playlist;
import com.liviu.smp2.data.Song;
import com.liviu.smp2.data.SongInfo;
import com.liviu.smp2.util.Constants;

public class DatabaseManager {

	// constants 
	private final String 		TAG						= "DatabaseManager";
	private final String[] 		PROJECTION_ALL_SONGS  	=  {
															  MediaStore.Audio.Media.ALBUM,         // 0
														  	  MediaStore.Audio.Media.ALBUM_ID, 		// 1
															  MediaStore.Audio.Media.ARTIST, 		// 2
															  MediaStore.Audio.Media.COMPOSER,		// 3
															  MediaStore.Audio.Media.DATA,			// 4
															  MediaStore.Audio.Media.DISPLAY_NAME,	// 5
															  MediaStore.Audio.Media.DURATION,		// 6
															  MediaStore.Audio.Media.TITLE,			// 7
															  MediaStore.Audio.Media.YEAR,			// 8
															  MediaStore.Audio.Media._ID			// 9
															};
	private final String[]	PROJECTION_ALL_PLAYLISTS	= {
													 		  MediaStore.Audio.Playlists._ID,
															  MediaStore.Audio.Playlists.NAME,
															  MediaStore.Audio.Playlists.DATA
														  };
	// URIs
	private final Uri 			URI_SONGS					= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	private final Uri			URI_PLAYLISTS				= MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
	
	// data
	private SQLiteDatabase		db;
	private Context				context;
	private ContentResolver		contentResolver;
	
	public DatabaseManager(Context context_) {
		context 			= context_;
		contentResolver		= context.getContentResolver();
	}
	
	public boolean openDatabase(){
		try{
			db = context.openOrCreateDatabase(Constants.DB_NAME, Context.MODE_PRIVATE, null);
			db.execSQL(Constants.CREATE_TABLE_ALBUM_INFO_TABLE);
			db.execSQL(Constants.CREATE_TABLE_SONGS_INFO);
			
			return true;
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		}
		catch (IllegalStateException e) {
			e.printStackTrace();
			closeDatabase();
			return false;
		}
	}
	
	public void closeDatabase(){		
		if(db.isOpen()) db.close();			
	}
		
	public void crPrintAllSongs(){
		Cursor 		c    = contentResolver.query(URI_SONGS, PROJECTION_ALL_SONGS, null, null, null);
		boolean 	flag = true;
		
		if(c == null){
			Log.e(TAG, "printAllSongs() c is null");
			return;
		}
		
		c.moveToFirst();
		while(flag){
			Log.e(TAG, "Song name: " + c.getString(5) + " " + c.getString(4));
			flag = c.moveToNext();			
		}
		
		c.close();
	}
	
	public HashMap<Integer, Song> getAllSongs() {
		
		Log.e(TAG, "getAllSongs()");
		HashMap<Integer, Song>   songsMap 	= new HashMap<Integer, Song>();
		HashMap<Integer, String> songsImgs  = getAlbumArtFromMediaStore();
		SongInfo				 songInfo	= new SongInfo();
		Cursor 					c    		= contentResolver.query(URI_SONGS, PROJECTION_ALL_SONGS, null, null, null);
		boolean 				flag 		= true;
		Song					tempSong	= new Song();
		int 					index		= 0;
		
		if(c == null){			
			Log.e(TAG, "printAllSongs() c is null");
			return songsMap;
		}
		
		if(c.getCount() == 0){
			c.close();
			return songsMap;
		}
			
		
		c.moveToFirst();
		while(flag){
			tempSong = new Song();
			tempSong.setId(c.getInt(9));
			songInfo = getSongInfo(tempSong.getId());
			if(songInfo != null)
				tempSong.setSongInfo(songInfo);
			tempSong.setAlbumName(c.getString(0));
			tempSong.setAlbumId(c.getLong(1));
			tempSong.setArtist(c.getString(2));
			tempSong.setComposer(c.getString(3));
			tempSong.setPath(c.getString(4));
			tempSong.setDisplayName(c.getString(5));
			tempSong.setDuration(c.getInt(6));
			tempSong.setTitle(c.getString(7));
			tempSong.setYear(c.getInt(8));			
			tempSong.setImagePath(songsImgs.get(c.getInt(1)));
		
			Log.e(TAG, "song: " + tempSong.toString());
			songsMap.put(index, tempSong);
			index++;
			
			flag = c.moveToNext();			
		}
		Log.e(TAG, "songs loaded: " + songsMap.size());				
		c.close();
		return songsMap;
	}	
	
	
	
	
	
	
	
	
	
	//============================== PLAYLISTS ====================================
	public  ArrayList<Playlist> crGetAllPlaylists() {
		ArrayList<Playlist> playlistsArray 			 = new ArrayList<Playlist>();		
		Playlist			emptyPlaylist			 = new Playlist();
		boolean				flag					 = true;
		
		Cursor c = contentResolver.query(URI_PLAYLISTS,
										 PROJECTION_ALL_PLAYLISTS,
										 null, 
										 null,
										 MediaStore.Audio.Playlists._ID + " DESC");
		if(c ==  null){
			Log.e(TAG, "cursor is null in getAllPlaylists");
			return playlistsArray;
		}
		
		c.moveToFirst();	
		while(flag){
			emptyPlaylist = new Playlist();
			Log.e(TAG, "playlist id: " + c.getLong(0) +  " playlist name: " + c.getString(1) + " data: " + c.getString(2));
			emptyPlaylist.setName(c.getString(1));
			emptyPlaylist.setID(c.getInt(0));
			emptyPlaylist.setData(c.getString(2));
			emptyPlaylist.setSize(getPlaylistSize(emptyPlaylist.getID()));
			
			playlistsArray.add(emptyPlaylist);
			flag = c.moveToNext();
		}
		
		c.close();
		
		return playlistsArray;
	}	
	
	public int getPlaylistSize(long playlistID) {
		Log.e(TAG, "getPlaylistSize: " + playlistID);
		
		if(playlistID == -1)
			return 0;
		
		int count 						  = 0;					
		String[] playlistSongsProjections = new String[]{ MediaStore.Audio.Playlists.Members.AUDIO_ID };
		Cursor c 						  = contentResolver.query(
																	MediaStore.Audio.Playlists.Members.getContentUri("external", playlistID),
																	playlistSongsProjections,
																	null,
																	null,
																	null
																 );
		if(c == null){
			Log.e(TAG, "songCursor is null");
			return 0;
		}
		
		count = c.getCount();		
		c.close();				
		
		Log.e(TAG, "song count for playlist: " + playlistID + " is : " + count);
		return count;
	}
	
	private HashMap<Integer, String> getAlbumArtFromMediaStore(){
		
		HashMap<Integer, String> imgPaths = new HashMap<Integer, String>();
		Cursor cAlbumArt = contentResolver.query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
												 new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART}, null, null, null);

		if(cAlbumArt == null)
			return imgPaths;
		
		int count = cAlbumArt.getCount();
		int index = 0;
		cAlbumArt.moveToFirst();
		
		while(index < count){
			
			Log.e(TAG, "path: " + cAlbumArt.getString(1));
			imgPaths.put(cAlbumArt.getInt(0), cAlbumArt.getString(1));	
			cAlbumArt.moveToNext();
			index++;
		}
	
		cAlbumArt.close();
		
		return imgPaths;
	}
	
	
	/** update played count for a given songID. 
	return: true  : update was successful
    		  false : otherwise
	params: new value of the count
      	  an ID for a song
	*/
	public boolean updatePlayedCount(int newCount, Song song){
		int songID 			 = song.getId();
		ContentValues values = new ContentValues();
		
		values.put(Constants.SONG_PLAYED_COUNT_FIELD, newCount);

		openDatabase();
		int n = db.update(Constants.TABLE_SONGS_INFO, 
						  values,
						  Constants.SONG_ID_FIELD + "=" + songID,
						  null);
		closeDatabase();
		
		Log.e(TAG, "updatePlayedCount(" + newCount + ", " + songID + ") affected rowNumber: " + n);
		if(n == 0){
			Log.e(TAG, "I can't find the song with id : " + songID + ". I will add it to table and I'll try again to update the playing count.");
			if(insertSongInfo(song))
				return updatePlayedCount(newCount, song);
			else
				return false;
		}
		else
			return true;
	}
	
	/**update the rate of a song 
	 * params: new rate value
	 * 		   an  ID for a song
	 * return: true : update was successful
	 * 		   false: otherwise
	 */
	public boolean updateRate(int newRate, Song song){
		int			  songID = song.getId();
		ContentValues values = new ContentValues();
		
		values.put(Constants.SONG_RATE_FIELD, newRate);
	
		openDatabase();
		int n = db.update(Constants.TABLE_SONGS_INFO, 
				  values,
				  Constants.SONG_ID_FIELD + "=" + songID,
				  null);	
		Log.e(TAG, "updateRate(" + newRate + ", " + songID + ") affected rowNumber: " + n);
		
		closeDatabase();
		
		if(n == 0){
			Log.e(TAG, "I can't find the song with id: " + songID + " to update the rating.");
			if(insertSongInfo(song))			
				return updateRate(newRate, song);
			else
				return false;
		}
		else
			return true;
	}
	
	
	
	/**update Album name 
	 * params: new album name value;
	 * 		   a song ID 
	 * return: true: if update was successful
	 * 		   false: otherwise
	 */	
	public boolean updateAlbumName(String newName, Song song){
		int			  songID = song.getId();
		ContentValues values = new ContentValues();
		
		values.put(Constants.SONG_ALBUM_NAME_FIELD, newName);
	
		openDatabase();
		int n = db.update(Constants.TABLE_SONGS_INFO, 
				  values,
				  Constants.SONG_ID_FIELD + "=" + songID,
				  null);
		closeDatabase();
		Log.e(TAG, "updateAlbumName(" + newName + ", " + songID + ") affected rowNumber: " + n);
		
		if(n == 0){
			Log.e(TAG, "I can't find the song with ID: " + songID + " to update the album name");
			if(insertSongInfo(song))
				return updateAlbumName(newName, song);
			else
				return false;
		}
		else
			return true;				
	}
	
	/**update Color tag of a song 
	 * params: new album name value;
	 * 		   a song ID 
	 * return: true: if update was successful
	 * 		   false: otherwise
	 */	
	public boolean updateColorTag(int newColor, Song song){
		int			  songID = song.getId();
		ContentValues values = new ContentValues();
		
		values.put(Constants.SONG_COLOR_TAG_FIELD, newColor);
	
		openDatabase();
		int n = db.update(Constants.TABLE_SONGS_INFO, 
				  values,
				  Constants.SONG_ID_FIELD + "=" + songID,
				  null);	
		Log.e(TAG, "updateColorTag(" + newColor + ", " + songID + ") affected rowNumber: " + n);
		closeDatabase();
		
		if(n == 0){
			Log.e(TAG, "i can't find the song with ID: " + songID + " to update the color tag");
			if(insertSongInfo(song))
				return updateColorTag(newColor, song);
			else
				return false;
		}
		else
			return true;
	}
	
	/**set a song as favorite or not
	 * params: new fav values
	 * 		   a song ID 
	 * return: true: if update was successful
	 * 		   false: otherwise
	 */	
	public boolean updateFavorite(boolean newFav, Song song){
		int			  songID = song.getId();
		ContentValues values = new ContentValues();
		
		values.put(Constants.SONG_IS_FAVORITE_FIELD, (newFav == true) ? 1 : 0);
	
		openDatabase();
		int n = db.update(Constants.TABLE_SONGS_INFO, 
				  values,
				  Constants.SONG_ID_FIELD + "=" + songID,
				  null);	
		Log.e(TAG, "updateFavorite(" + newFav + ", " + songID + ") affected rowNumber: " + n);
		closeDatabase();
		if(n == 0){
			Log.e(TAG, "I can't find the song with ID: " + songID + " to update favorite value.");
			if(insertSongInfo(song))
				return updateFavorite(newFav, song);
			else
				return false;
		}
		else
			return true;
	}
	
	/**ignore a song or not
	 * params: new ignore status value;
	 * 		   a song ID 
	 * return: true: if update was successful
	 * 		   false: otherwise
	 */	
	public boolean updateIgnore(boolean newIgnore, Song song){
		int			  songID = song.getId();
		ContentValues values = new ContentValues();
		
		values.put(Constants.SONG_IS_IGNORED_FIELD, (newIgnore == true ? 1 : 0));
		
		openDatabase();
		int n = db.update(Constants.TABLE_SONGS_INFO, 
				  values,
				  Constants.SONG_ID_FIELD + "=" + songID,
				  null);	
		closeDatabase();
		Log.e(TAG, "updategnore(" + newIgnore + ", " + songID + ") affected rowNumber: " + n);
		
		if(n == 0){
			Log.e(TAG, "I can't find the song with ID: " + songID + " to update the ignore value");
			if(insertSongInfo(song))
				return updateIgnore(newIgnore, song);
			else
				return false;
		}
		else
			return true;
	}
	
	public boolean insertSongInfo(Song tempSong){
		
		ContentValues values = new ContentValues();		
		
		values.put(Constants.SONG_ID_FIELD, 		  	  tempSong.getId());
		values.put(Constants.SONG_ALBUM_NAME_FIELD, 	  tempSong.getAlbumName());
		values.put(Constants.SONG_PLAYED_COUNT_FIELD, 	  tempSong.getPlayCount());
		values.put(Constants.SONG_RATE_FIELD, 	  		  tempSong.getRate());
		values.put(Constants.SONG_COLOR_TAG_FIELD, 		  tempSong.getColorTag());

		if(tempSong.isFavorite())
			values.put(Constants.SONG_IS_FAVORITE_FIELD, 1);
		else
			values.put(Constants.SONG_IS_FAVORITE_FIELD, 0);
		
		if(tempSong.isIgnored())
			values.put(Constants.SONG_IS_IGNORED_FIELD, 1);
		else
			values.put(Constants.SONG_IS_IGNORED_FIELD, 0);
		
		try{
			openDatabase();
			db.insertOrThrow(Constants.TABLE_SONGS_INFO, null, values);
			closeDatabase();		
		}
		catch (SQLException e){
			e.printStackTrace();
			closeDatabase();
			Log.e(TAG,"ERROR at insert!the value is possible to exist in database " + e.toString());
			return false;
		}
		
		return true;
	}	
	
	public SongInfo getSongInfo(int songID){
		openDatabase();
		Cursor c = db.query(Constants.TABLE_SONGS_INFO, new String[]{Constants.SONG_ID_FIELD,           //0																	 
																	 Constants.SONG_IS_FAVORITE_FIELD,  //1
																	 Constants.SONG_IS_IGNORED_FIELD,   //2
																	 Constants.SONG_PLAYED_COUNT_FIELD, //3
																	 Constants.SONG_RATE_FIELD},        //4
																	 Constants.SONG_ID_FIELD + "=" + songID,
																	 null, null, null, null);
		closeDatabase();
		try{
			c.moveToFirst();			
			SongInfo songInfo = new SongInfo("no album",
											-1,
											(c.getInt(1) == 1) ? true : false,
											(c.getInt(2) == 1) ? true : false,
											 c.getInt(3),
											 c.getInt(4));
			
			//Log.e(TAG, "The song with ID " + song.getId() + " is in database");
			c.close();
			return songInfo;
			
		}
		catch(RuntimeException e){
			Log.e(TAG, "getSongInfoss: The song with ID " + songID + " is NOT in database: I will insert it now!");	
			if(!c.isClosed())
				c.close();
			
			return null;			
		}
	}	
	
	
}