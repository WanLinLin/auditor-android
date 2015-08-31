package com.example.auditor.convert;

/**
 * Created by Wan Lin on 15/7/27.
 * Stores the duration compares to the quarter note duration
 */

public enum NoteDurations {
    wholeNote           (4f), // Its length is equal to four beats in 4/4 time.
    halfNote            (2f),
    quarterNote         (1f),
    eighthNote          (0.5f),
    sixteenthNote       (0.25f),
    thirtySecondNote    (0.125f),
    sixtyFourthNote     (0.0625f);

    public float time;

    NoteDurations(float time) {
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