package com.example.auditor;

import java.util.Date;

/**
 * Created by Wan Lin on 15/8/29.
 * Score
 */
public class Score {
    private long id;
    private String title;
    private Date lastModDate;

    public Score(long scoreID, String scoreTitle, Date scoreModDate) {
        id = scoreID;
        title = scoreTitle;
        lastModDate = scoreModDate;
    }

    public long getID() {return id;}
    public String getTitle() {return title;}
    public Date getLastModDate() {return lastModDate;}
}
