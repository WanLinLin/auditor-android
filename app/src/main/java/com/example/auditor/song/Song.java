package com.example.auditor.song;

/**
 * Created by wanlin on 15/7/2.
 */
public class Song {
    private long id;
    private String title;
    private String lastModDate;

    public Song(long songID, String songTitle, String songModDate) {
        id = songID;
        title = songTitle;
        lastModDate = songModDate;
    }

    public long getID() {return id;}
    public String getTitle() {return title;}
    public String getLastModDate() {return lastModDate;}
}
