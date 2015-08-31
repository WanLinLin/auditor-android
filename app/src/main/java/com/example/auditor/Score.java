package com.example.auditor;

/**
 * Created by wanlin on 15/8/29.
 */
public class Score {
    private long id;
    private String title;
    private String lastModDate;

    public Score(long scoreID, String scoreTitle, String scoreModDate) {
        id = scoreID;
        title = scoreTitle;
        lastModDate = scoreModDate;
    }

    public long getID() {return id;}
    public String getTitle() {return title;}
    public String getLastModDate() {return lastModDate;}
}
