package com.example.auditor.score;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.jfugue.Pattern;

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
    private int curX; // relative to measure view, refresh every part
    private NoteContext noteContext;

    public NumberedMusicalNotationParser(Context context, int noteViewGroupHeight, String musicString) {
        this.context = context;
        this.musicString = musicString;
        this.noteViewGroupHeight = noteViewGroupHeight;
        this.noteViewGroupWidth = noteViewGroupHeight / 3 * 2;
        noteContext = new NoteContext();
        curX = 0;
        score = new Score(context);
    }

    public void parse() {
        int partIndex = 0;
        int measureIndex = 0;
        int noteViewGroupIndex = 0;

        Pattern pattern = new Pattern(musicString);
        String[] tokens = pattern.getTokens();

        // add first part
        Part part = new Part(context, noteViewGroupHeight);
        score.printPart(part, partIndex);

        // add first measure
        Measure measure = new Measure(context, noteViewGroupWidth, noteViewGroupHeight);
        part.printMeasure(measure, measureIndex);
        curX += Part.barStrokeWidth * 3;
        measureIndex++;

        for(String t : tokens) {
            switch (t) {
                case " ":
                    break;

                case "|":
                    // add a new measure
                    measure = new Measure(context, noteViewGroupWidth, noteViewGroupHeight);
                    part.printMeasure(measure, measureIndex);
                    curX += Part.barStrokeWidth * 3;
                    measureIndex++;
                    noteViewGroupIndex = 0;
                    break;

                default:
                    // add a new note view group
                    parseNoteContext(t);
                    noteContext.printNoteContext();
                    addNote(measure.getId(), part.getId(), noteViewGroupIndex);
                    noteViewGroupIndex++;
                    noteContext = null;
                    break;
            }
        }

        part.addTieView();
    }

    private void parseNoteContext(String t) {
        int searchPos = 0;
        noteContext = new NoteContext();

        while(searchPos < t.length()) {
            char c = t.charAt(searchPos);

            switch (c) {
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'R':
                    noteContext.note = Character.toString(c);
                    break;

                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                    noteContext.octave = Character.toString(c);
                    break;

                case '#':
                case 'b':
                    noteContext.accidental = Character.toString(c);
                    break;

                case '-':
                    if(noteContext.tieEnd == null)
                        noteContext.tieEnd = true;
                    else
                        noteContext.tieStart = true;
                    break;

                // won't have beam view
                case 'w':
                case 'h':
                case 'q':
                // will have beam view
                case 'i':
                case 's':
                case 't':
                case 'x':
                    if(noteContext.tieEnd == null)
                        noteContext.tieEnd = false;
                    noteContext.duration = Character.toString(c);
                    break;

                case '.':
                    noteContext.dot = Character.toString(c);
                    break;
            }
            searchPos++;
        }
    }

    private void addNote(int measureId, int partId, int noteViewGroupIndex) {
        Part part = (Part)score.findViewById(partId);
        Measure measure = (Measure)score.findViewById(measureId);
        measure.printNote(noteContext.note, noteContext.accidental, noteContext.dot, noteContext.octave, noteContext.duration, noteViewGroupIndex);

        /* calculate current x position */
        curX += measure.getCurNoteViewGroupWidth() / 2;
        if(noteContext.tieEnd)
            part.addTieInfo(new Pair<>(curX, "end"));
        if(noteContext.tieStart)
            part.addTieInfo(new Pair<>(curX, "start"));
        curX += measure.getCurNoteViewGroupWidth() / 2;
    }

    public Score getScore() {
        return score;
    }

    class NoteContext {
        String note = "";
        String octave = "";
        String accidental = "";
        Boolean tieEnd = null;
        String duration = ""; // i, s, ,t ,x
        Boolean tieStart = false;
        String dot = "";

        void printNoteContext() {
            Log.e(getClass().getName(), "note: " + note);
            Log.e(getClass().getName(), "octave: " + octave);
            Log.e(getClass().getName(), "accidental: " + accidental);
            Log.e(getClass().getName(), "tieEnd: " + tieEnd);
            Log.e(getClass().getName(), "duration: " + duration);
            Log.e(getClass().getName(), "tieStart: " + tieStart);
            Log.e(getClass().getName(), "dot: " + dot);
        }
    }
}
// TODO smart phone loop station