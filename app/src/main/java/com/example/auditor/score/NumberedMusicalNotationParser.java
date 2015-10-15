package com.example.auditor.score;

import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/26.
 * Parse music string and render it to a score object;
 */
public class NumberedMusicalNotationParser {
    private static final String LOG_TAG = NumberedMusicalNotationParser.class.getName();
    private ShowScoreActivity showScoreActivity;
    private String musicString;
    private int curX; // to calculate tie position, relative to part view, refresh every part
    private ScoreViewGroup scoreViewGroup;
    private NoteContext noteContext;

    public static final Character sentenceEndTag = '~';

    public NumberedMusicalNotationParser(ShowScoreActivity showScoreActivity, String musicString) {
        this.showScoreActivity = showScoreActivity;
        this.musicString = musicString;
        noteContext = new NoteContext();
        scoreViewGroup = new ScoreViewGroup(showScoreActivity);
        scoreViewGroup.setId(R.id.score_view_group);

        ViewTreeObserver vto = scoreViewGroup.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scoreViewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (!ShowScoreActivity.scoreContainer.isShown()) {
                    ShowScoreActivity.scoreContainer.setVisibility(View.VISIBLE);

                    ShowScoreActivity.scoreContainer.post(new Runnable() {
                        @Override
                        public void run() {
                            RelativeLayout loadingViewLayout = (RelativeLayout) ShowScoreActivity.rootView.findViewById(R.id.loading_image_layout);
                            if (loadingViewLayout != null) {
                                Animation animation = AnimationUtils.loadAnimation(NumberedMusicalNotationParser.this.showScoreActivity, R.anim.loading_image_fade_out);
                                loadingViewLayout.setAnimation(animation);
                                loadingViewLayout.animate();
                                ShowScoreActivity.rootView.removeView(loadingViewLayout);
                            }
                            ShowScoreActivity.rootView.setBackgroundColor(Color.WHITE);
                        }
                    });
                }
            }
        });
    }

    public void parse() {
        int partIndex = 0;
        int measureIndex = 0;
        int noteViewGroupIndex = 0;
        curX = 0;
        PartViewGroup curParsePart;
        MeasureViewGroup curParseMeasure = new MeasureViewGroup(showScoreActivity);

        String[] tokens = musicString.split(" ");
//        for(String t : tokens) Log.d(LOG_TAG, "token: " + t);

        // add first part
        scoreViewGroup.printPart(partIndex);
        curParsePart = (PartViewGroup)scoreViewGroup.findViewById(partIndex + ShowScoreActivity.partStartId);
        partIndex++;

        // print first bar
        curParsePart.printBarView(-1); // print first bar
        curX += ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;

        for(String token : tokens) {
            if(token.equals("") || token.equals(" ")) continue;
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
                    curX += ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;
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
                    curParseMeasure.printWord(noteContext.word, noteViewGroupIndex, !noteContext.accidental.equals(""), !noteContext.dot.equals(""));
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
                noteContext.duration = "-"; // indicate beam view that this note is a duration note
                noteContext.tieEnd = false;
                for(int i = 0; i < 3; i++) {
                    addNote(curParseMeasure.getId(), curParsePart.getId(), noteViewGroupIndex);
                    curParseMeasure.printWord(noteContext.word, noteViewGroupIndex, !noteContext.accidental.equals(""), !noteContext.dot.equals(""));
                    noteViewGroupIndex++;
                }
            }

            if(token.contains("h")) {
                noteContext = new NoteContext();
                noteContext.note = durationNote;
                noteContext.duration = "-"; // indicate beam view that this note is a duration note
                noteContext.tieEnd = false;
                addNote(curParseMeasure.getId(), curParsePart.getId(), noteViewGroupIndex);
                curParseMeasure.printWord(noteContext.word, noteViewGroupIndex, !noteContext.accidental.equals(""), !noteContext.dot.equals(""));
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

                default:
                    if(Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                        if(t.charAt(t.length()-1) == sentenceEndTag) noteContext.word = Character.toString(c) + sentenceEndTag;
                        else noteContext.word = Character.toString(c);
                    }
                    break;
            }
            searchPos++;
        }
    }

    private void addNote(int measureId, int partId, int noteViewGroupIndex) {
        PartViewGroup partViewGroup = (PartViewGroup) scoreViewGroup.findViewById(partId);
        MeasureViewGroup measureViewGroup = (MeasureViewGroup) scoreViewGroup.findViewById(measureId);
        if(noteViewGroupIndex == 0) measureViewGroup.printBarWidthView();

//        Log.e(LOG_TAG, "note: " + noteContext.note + ", accidental: " + noteContext.accidental + ", octave: " + noteContext.octave + ", duration: " + noteContext.duration + ", tieStart: " + noteContext.tieStart + ", tieEnd: " + noteContext.tieEnd);
        measureViewGroup.printNote(noteContext.note, noteContext.accidental, noteContext.dot, noteContext.octave, noteContext.duration, noteContext.tieStart, noteContext.tieEnd, noteViewGroupIndex);

        // store tie information
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
        String word = "";

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