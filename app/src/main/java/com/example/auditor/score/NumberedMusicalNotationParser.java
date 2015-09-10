package com.example.auditor.score;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.auditor.ShowScoreActivity;

import org.jfugue.Pattern;

/**
 * Created by Wan Lin on 15/8/26.
 * Parse music string and render it to a score object;
 */
public class NumberedMusicalNotationParser {
    private static final String LOG_TAG = NumberedMusicalNotationParser.class.getName();
    private Context context;
    private String musicString;
    private int curX; // relative to measure view, refresh every part
    private ScoreViewGroup scoreViewGroup;
    private NoteContext noteContext;

    public NumberedMusicalNotationParser(Context context, String musicString) {
        this.context = context;
        this.musicString = musicString;
        noteContext = new NoteContext();
        scoreViewGroup = new ScoreViewGroup(context);
    }

    public void parse() {
        int partIndex = 0;
        int measureIndex = 0;
        int noteViewGroupIndex = 0;
        curX = 0;
        PartViewGroup curParsePart;
        MeasureViewGroup curParseMeasure = new MeasureViewGroup(context);

        Pattern pattern = new Pattern(musicString);
        String[] tokens = pattern.getTokens();

        // add first part
        scoreViewGroup.printPart(partIndex);
        curParsePart = (PartViewGroup)scoreViewGroup.findViewById(partIndex + ShowScoreActivity.partStartId);
        partIndex++;

        // print first bar
        curParsePart.printBarView(-1); // print first bar
        curX += ShowScoreActivity.NoteChildViewDimension.BAR_STROKE_WIDTH * 3;

        for(String token : tokens) {
            String s = token.substring(0,1);
            switch (s) {
                case "K":
//                    Log.e(getClass().getName(), "key signature: " + token);
                    break;

                case "T":
//                    Log.e(getClass().getName(), "tempo: " + token);
                    break;

                case "|":
                    // print bar view
                    curParsePart.printBarView(curParseMeasure.getId());
                    noteViewGroupIndex = 0;
                    curX += ShowScoreActivity.NoteChildViewDimension.BAR_STROKE_WIDTH * 3;
                    break;

                default:
                    if(noteViewGroupIndex == 0) {
                        curParsePart.printMeasure(measureIndex);
                        curParseMeasure = (MeasureViewGroup)scoreViewGroup.findViewById(measureIndex + ShowScoreActivity.measureStartId);
                        measureIndex++;
                    }

                    // add a new note view group
                    parseNoteContext(token);
                    addNote(curParseMeasure.getId(), curParsePart.getId(), noteViewGroupIndex);
                    noteViewGroupIndex++;
                    break;
            }

            // handle w, h note duration (add "-")
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
                    addNote(curParseMeasure.getId(), curParsePart.getId(), noteViewGroupIndex);
                    noteViewGroupIndex++;
                }
            }

            if(token.contains("h")) {
                noteContext = new NoteContext();
                noteContext.note = durationNote;
                noteContext.tieEnd = false;
                addNote(curParseMeasure.getId(), curParsePart.getId(), noteViewGroupIndex);
                noteViewGroupIndex++;
            }
        }

        curParsePart.printTieView();
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
        if(noteViewGroupIndex == 0)
            measureViewGroup.printBarWidthView();
        measureViewGroup.printNote(noteContext.note, noteContext.accidental, noteContext.dot, noteContext.octave, noteContext.duration, noteContext.tieStart, noteContext.tieEnd, noteViewGroupIndex);

        // TODO parse word and print it
        measureViewGroup.printWord("叡", noteViewGroupIndex);

        if(noteContext.tieEnd) {
            partViewGroup.addTieInfo(new Pair<>((int)(curX + measureViewGroup.getCurNoteViewGroupWidth()/2), "end"));
        }
        if(noteContext.tieStart) {
            partViewGroup.addTieInfo(new Pair<>((int)(curX + measureViewGroup.getCurNoteViewGroupWidth()/2), "start"));
        }

        curX += measureViewGroup.getCurNoteViewGroupWidth();
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