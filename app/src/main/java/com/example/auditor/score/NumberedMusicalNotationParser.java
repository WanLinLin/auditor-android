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
    private int curX; // relative to measure view, refresh every part
    private ScoreViewGroup scoreViewGroup;
    private NoteContext noteContext;

    public NumberedMusicalNotationParser(Context context, int noteViewGroupHeight, String musicString) {
        this.context = context;
        this.musicString = musicString;
        this.noteViewGroupHeight = noteViewGroupHeight;
        this.noteViewGroupWidth = noteViewGroupHeight / 3 * 2;
        noteContext = new NoteContext();
        curX = 0;
        scoreViewGroup = new ScoreViewGroup(context);
    }

    public void parse() {
        int partIndex = 0;
        int measureIndex = 0;
        int noteViewGroupIndex = 0;

        Pattern pattern = new Pattern(musicString);
        String[] tokens = pattern.getTokens();

        // add first part
        PartViewGroup partViewGroup = new PartViewGroup(context, noteViewGroupHeight);
        scoreViewGroup.printPart(partViewGroup, partIndex);

        // add first measure
        MeasureViewGroup measureViewGroup = new MeasureViewGroup(context, noteViewGroupWidth, noteViewGroupHeight);
        partViewGroup.printMeasure(measureViewGroup, measureIndex);
        curX += PartViewGroup.barStrokeWidth * 3;

        measureIndex++;

        for(String token : tokens) {
            String s = token.substring(0,1);
            switch (s) {
                case "K":
                    Log.e(getClass().getName(), "key signature: " + token);
                    break;

                case "T":
                    Log.e(getClass().getName(), "tempo: " + token);
                    break;

                case "|":
                    // add a new measure
                    measureViewGroup = new MeasureViewGroup(context, noteViewGroupWidth, noteViewGroupHeight);
                    partViewGroup.printMeasure(measureViewGroup, measureIndex);
                    curX += PartViewGroup.barStrokeWidth * 3;
                    measureIndex++;
                    noteViewGroupIndex = 0;
                    break;

                default:
                    // add a new note view group
                    parseNoteContext(token);
                    addNote(measureViewGroup.getId(), partViewGroup.getId(), noteViewGroupIndex);
                    noteViewGroupIndex++;
                    break;
            }

            // handle w, h note duration
            String durationNote;
            if(noteContext.note.equals("R"))
                durationNote = "R";
            else
                durationNote = "-";

            if(token.contains("w")) {
                noteContext = new NoteContext();
                noteContext.note = durationNote;
                noteContext.tieEnd = false;
                for(int i = 0; i < 3; i++) {
                    addNote(measureViewGroup.getId(), partViewGroup.getId(), noteViewGroupIndex);
                    noteViewGroupIndex++;
                }
            }

            if(token.contains("h")) {
                noteContext = new NoteContext();
                noteContext.note = durationNote;
                noteContext.tieEnd = false;
                addNote(measureViewGroup.getId(), partViewGroup.getId(), noteViewGroupIndex);
                noteViewGroupIndex++;
            }
        }

        partViewGroup.addTieView();
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
                    if(noteContext.tieEnd == null)
                        noteContext.tieEnd = false;
                    break;
                // have beam view
                case 'i':
                case 's':
                case 't':
                case 'x':
                    noteContext.duration = Character.toString(c);
                    if(noteContext.tieEnd == null)
                        noteContext.tieEnd = false;
                    break;

                case '.':
                    noteContext.dot = Character.toString(c);
                    break;
            }
            searchPos++;
        }
    }

    private void addNote(int measureId, int partId, int noteViewGroupIndex) {
        PartViewGroup partViewGroup = (PartViewGroup) scoreViewGroup.findViewById(partId);
        MeasureViewGroup measureViewGroup = (MeasureViewGroup) scoreViewGroup.findViewById(measureId);
        measureViewGroup.printNote(noteContext.note, noteContext.accidental, noteContext.dot, noteContext.octave, noteContext.duration, noteViewGroupIndex);

        /* calculate current x position */
        curX += measureViewGroup.getCurNoteViewGroupWidth() / 2;
        if(noteContext.tieEnd)
            partViewGroup.addTieInfo(new Pair<>(curX, "end"));
        if(noteContext.tieStart)
            partViewGroup.addTieInfo(new Pair<>(curX, "start"));
        curX += measureViewGroup.getCurNoteViewGroupWidth() / 2;
    }

    public ScoreViewGroup getScoreViewGroup() {
        return scoreViewGroup;
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