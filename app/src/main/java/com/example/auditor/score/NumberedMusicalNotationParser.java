package com.example.auditor.score;

import android.graphics.Color;
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

        // ViewTreeObserver notify when whole score is ready
        ViewTreeObserver vto = scoreViewGroup.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scoreViewGroup.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // if the score hasn't shown
                if (!ShowScoreActivity.scoreContainer.isShown()) {
                    ShowScoreActivity.scoreContainer.setVisibility(View.VISIBLE);

                    // play the loading fade out loading animation
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
        MeasureViewGroup curParseMeasure = null;

        String[] tokens = musicString.split(" ");

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
                case "K": // it's a key signature token
                    break;

                case "T": // it's a tempo token
                    break;

                case "|":
                    // a measure end, print BarView
                    curParsePart.printBarView(curParseMeasure.getId());
                    noteViewGroupIndex = 0;
                    curX += ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;
                    break;

                default:
                    // is the fist note of a measure, add a measure
                    if(noteViewGroupIndex == 0) {
                        curParsePart.printMeasure(measureIndex);
                        curParseMeasure = (MeasureViewGroup)curParsePart.findViewById(measureIndex + ShowScoreActivity.measureStartId);
                        measureIndex++;
                    }

                    // add a new note
                    parseNoteContext(token);

                    // calculate whether the new note would exceed the screen width
                    int width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
                    if(!noteContext.accidental.isEmpty())
                        width += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
                    if(!noteContext.dot.isEmpty())
                        width += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;

                    // if exceed screen width
                    if(curX + width > ShowScoreActivity.screenWidth) {
                        // end a part
                        noteViewGroupIndex = 0;
                        curX = 0;
                        curParsePart.printTieView();
                        curParsePart.clearTieInfo();

                        // print a new part
                        scoreViewGroup.printPart(partIndex);
                        curParsePart = (PartViewGroup)scoreViewGroup.findViewById(partIndex + ShowScoreActivity.partStartId);
                        partIndex++;
                        curX += ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;
                        measureIndex = 0;

                        curParsePart.printMeasure(measureIndex);
                        curParseMeasure = (MeasureViewGroup)curParsePart.findViewById(measureIndex + ShowScoreActivity.measureStartId);
                        measureIndex++;
                    }
                    addNoteAndWord(curParseMeasure.getId(), curParsePart.getId(), noteViewGroupIndex);
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
                    addNoteAndWord(curParseMeasure.getId(), curParsePart.getId(), noteViewGroupIndex);
                    noteViewGroupIndex++;
                }
            }

            if(token.contains("h")) {
                noteContext = new NoteContext();
                noteContext.note = durationNote;
                noteContext.duration = "-"; // indicate beam view that this note is a duration note
                noteContext.tieEnd = false;
                addNoteAndWord(curParseMeasure.getId(), curParsePart.getId(), noteViewGroupIndex);
                noteViewGroupIndex++;
            }
        }
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

    private void addNoteAndWord(int measureId, int partId, int noteViewGroupIndex) {
        PartViewGroup partViewGroup = (PartViewGroup) scoreViewGroup.findViewById(partId);
        MeasureViewGroup measureViewGroup = (MeasureViewGroup) partViewGroup.findViewById(measureId);
        if(noteViewGroupIndex == 0) measureViewGroup.printBarWidthView();

        measureViewGroup.printNote(noteContext, noteViewGroupIndex);
        measureViewGroup.printWord(noteContext.word, noteViewGroupIndex, !noteContext.accidental.isEmpty(), !noteContext.dot.isEmpty());

        // the current adding note
        NoteViewGroup note = (NoteViewGroup)measureViewGroup.findViewById(noteViewGroupIndex + ShowScoreActivity.noteStartId);

        // store tie information
        if(noteContext.tieEnd) {
            partViewGroup.addTieInfo(new Pair<>(curX + note.getViewWidth()/2, "end"));
        }
        if(noteContext.tieStart) {
            partViewGroup.addTieInfo(new Pair<>(curX + note.getViewWidth()/2, "start"));
        }

        curX += note.getViewWidth();
    }

    public ScoreViewGroup getScoreViewGroup() {
        return scoreViewGroup;
    }
}