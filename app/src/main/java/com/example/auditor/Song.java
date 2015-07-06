package com.example.auditor;

/**
 * Created by wanlin on 15/7/2.
 */
public class Song {
    private long id;
    private String title;

    public Song(long songID, String songTitle) {
        id = songID;
        title = songTitle;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
}
