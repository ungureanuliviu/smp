package com.liviu.smp2.data;

public class SongInfo {
	private String  albumNameInfo;
	private boolean isFavoriteInfo;
	private boolean isIgnoredInfo;
	private int     playedCountInfo;
	private int     rateInfo;
	private int  	colorTagInfo;
	
	public SongInfo(String  albumName,
					int     colorTag,
					boolean isFavorite,
					boolean isIgnored,
					int     playedCount,
					int     rate) {
		albumNameInfo   = albumName;
		colorTagInfo    = colorTag;
		isFavoriteInfo  = isFavorite;
		isIgnoredInfo   = isIgnored;
		playedCountInfo = playedCount;
		rateInfo        = rate;	
	}
	
	public SongInfo() {
		albumNameInfo	 = "";
		colorTagInfo 	 = -1;
		isFavoriteInfo   = false;
		isIgnoredInfo    = false;
		playedCountInfo  = 0;
		rateInfo 		 = 0;
	}

	public void setAlbumNameInfo(String albumNameInfo) {
		this.albumNameInfo = albumNameInfo;
	}
	
	public void setColorTagInfo(int colorTagInfo) {
		this.colorTagInfo = colorTagInfo;
	}
	
	public void setFavoriteInfo(boolean isFavoriteInfo) {
		this.isFavoriteInfo = isFavoriteInfo;
	}
	
	public void setIgnoredInfo(boolean isIgnoredInfo) {
		this.isIgnoredInfo = isIgnoredInfo;
	}
	
	public void setPlayedCountInfo(int playedCountInfo) {
		this.playedCountInfo = playedCountInfo;
	}
	
	public void setRateInfo(int rateInfo) {
		this.rateInfo = rateInfo;
	}
	
	public String getAlbumNameInfo() {
		return albumNameInfo;
	}
	
	public int getColorTagInfo() {
		return colorTagInfo;
	}
	
	public int getPlayedCountInfo() {
		return playedCountInfo;
	}
	
	public int getRateInfo() {
		return rateInfo;
	}
	
	public boolean getFavoriteInfo(){
		return isFavoriteInfo;
	}
	
	public boolean getIgnoredInfo(){
		return isIgnoredInfo;
	}
	
	@Override
	public String toString() {
		String strTemp = "\nAlbum name: " + albumNameInfo + "\n" + 
						 "isFavoriteInfo: " + isFavoriteInfo + "\n" + 
						 "isIgnoredInfo: " + isIgnoredInfo + "\n" +
						 "playedCountInfo: " + playedCountInfo + "\n" + 
						 "colorTagInfo: " + colorTagInfo + "\n";
		
		return strTemp;
	}
	


}
