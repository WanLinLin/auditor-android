package com.example.auditor;

import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/7/27.
 * Store single note result.
 */
public class NoteResult {
    private String noteName;

    // This ArrayList is going to store the duration of the note
    private ArrayList<Pair<String, Integer>> noteTimeArr = new ArrayList<>();

    public NoteResult(String noteName, ArrayList<Pair<String, Integer>> noteTimeArr){
        this.noteName = noteName;
        this.noteTimeArr = noteTimeArr;
    }

    public String getNoteName() {
        return noteName;
    }

    public void setNoteName(String noteName) {
        this.noteName = noteName;
    }

    public ArrayList<Pair<String, Integer>> getNoteTimeArr() {
        return noteTimeArr;
    }

    public void setNoteTimeArr(ArrayList<Pair<String, Integer>> noteTimeArr) {
        this.noteTimeArr = noteTimeArr;
    }
}
