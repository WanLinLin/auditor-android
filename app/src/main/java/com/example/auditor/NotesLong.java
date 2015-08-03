package com.example.auditor;

/**
 * Created by Wan Lin on 15/7/27.
 * Stores the types of notes
 */

public enum  NotesLong {
    wholeNote           (1f),
    halfNote            (0.5f),
    quarterNote         (0.25f),
    eighthNote          (0.125f),
    sixteenthNote       (0.0625f),
    thirtySecondNote    (0.03125f),
    sixtyFourthNote     (0.015625f);

    public float time;

    NotesLong(float time) {
        this.time = time;
        this.time = time;
    }

    /* getter and setter */
    public float getTime() {
        return this.time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}