package com.example.auditor.score;

import android.util.Log;

/**
 * Created by Wan Lin on 2015/10/18.
 * NoteContext
 */
public class NoteContext {
    private static final String LOG_TAG = "NoteContext";
    public String note = ""; // must be filled
    public String octave = ""; // must be filled
    public String duration = ""; // must be filled
    public String accidental = "";
    public Boolean tieEnd = null;
    public Boolean tieStart = false;
    public String dot = "";
    public String word = "";

    void printNoteContext() {
        Log.e(LOG_TAG, "note: " + note);
        Log.e(LOG_TAG, "octave: " + octave);
        Log.e(LOG_TAG, "accidental: " + accidental);
        Log.e(LOG_TAG, "tieEnd: " + tieEnd);
        Log.e(LOG_TAG, "duration: " + duration);
        Log.e(LOG_TAG, "tieStart: " + tieStart);
        Log.e(LOG_TAG, "dot: " + dot);
        Log.e(LOG_TAG, "word: " + word);
    }
}
