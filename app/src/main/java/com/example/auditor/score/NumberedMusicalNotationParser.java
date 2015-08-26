package com.example.auditor.score;

import android.content.Context;
import android.util.Pair;

/**
 * Created by Wan Lin on 15/8/26.
 * Parse music string and render it to a score object;
 */
public class NumberedMusicalNotationParser {
    private Context context;
    private String musicString;
    private int noteViewGroupHeight;
    private int noteViewGroupWidth;
    private Score score;

    public NumberedMusicalNotationParser(Context context, int noteViewGroupHeight, String musicString) {
        this.context = context;
        this.musicString = musicString;
        this.noteViewGroupHeight = noteViewGroupHeight;
        this.noteViewGroupWidth = noteViewGroupHeight / 3 * 2;
    }

    public void parse() {
        Score score = new Score(context);

        String note = "B";
        String octave = "4";
        String accidental = "";
        Boolean tieEnd = false;
        String duration = "t"; // i, s, ,t ,x
        Boolean tieStart = false;
        String dot = "";

        /* add i part views into this score */
        for(int i = 0; i < 4; i++) {
            // relative to measure view, refresh every part
            int curX = 0;

            Part part = new Part(context, noteViewGroupHeight);
            score.printPart(part, i);

            /* add j measure views into this part */
            for (int j = 0; j < 2; j++) {
                Measure measure = new Measure(context, noteViewGroupWidth, noteViewGroupHeight);
                part.printMeasure(measure, j);
                curX += Part.barStrokeWidth * 3;

                /* add k note view groups into this measure */
                for (int k = 0; k < 4; k++) {
                    measure.printNote(note, accidental, dot, octave, duration, k);

                    if(j == 0 && k == 3) {
                        tieStart = true;
                    }
                    if(j == 1 && k == 0) {
                        tieEnd = true;
                    }

                    /* calculate current x position */
                    curX += measure.getCurNoteViewGroupWidth() / 2;
                    if(tieStart) {
                        part.addTieInfo(new Pair<>(curX, "start"));
                        tieStart = false;
                    }

                    if(tieEnd) {
                        part.addTieInfo(new Pair<>(curX, "end"));
                        tieEnd = false;
                    }
                    curX += measure.getCurNoteViewGroupWidth() / 2;
                }
            }
            part.addTieView();
        }

        this.score = score;
    }

    public Score getScore() {
        return score;
    }
}
