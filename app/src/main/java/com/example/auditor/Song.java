package com.example.auditor;

import java.util.Date;

/**
 * Created by Wan Lin on 15/7/2.
 * Song
 */
public class Song {
    private long id;
    private String title;
    private Date lastModDate;

    public Song(long songID, String songTitle, Date songModDate) {
        id = songID;
        title = songTitle;
        lastModDate = songModDate;
    }

    public long getID() {return id;}
    public String getTitle() {return title;}
    public Date getLastModDate() {return lastModDate;}
}
