package com.example.auditor.score;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/26.
 * A measure contains numbers of note view groups.
 */
public class MeasureViewGroup extends RelativeLayout {
    private static final String LOG_TAG = MeasureViewGroup.class.getName();
    private Context context;
    private int curNoteViewGroupWidth;
    private int width = 0;

    public static MeasureViewGroup curEditMeasure;

    public MeasureViewGroup(Context context) {
        super(context);
        this.context = context;
//        ViewTreeObserver vto = getViewTreeObserver();
//        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                Log.e(LOG_TAG, "measure on global layout!");
//            }
//        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ShowScoreActivity.scoreEditMode) {
                    curEditMeasure = MeasureViewGroup.this;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        for(int i = 0; i < getChildCount(); i++) {
            getChildAt(i).requestLayout();
        }
    }

    public void printBarWidthView() {
        BarWidthView barWidthView = new BarWidthView(context);
        barWidthView.setId(R.id.bar_width_view);
        RelativeLayout.LayoutParams bwv =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        bwv.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        bwv.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        barWidthView.setLayoutParams(bwv);
        this.addView(barWidthView);
        this.width+=ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;
    }

    public void printNote(String note, String accidental, String dot, String octave, String duration, boolean tieStart, boolean tieEnd, int i) {
        int noteViewGroupId = i + ShowScoreActivity.noteStartId;
        NoteViewGroup noteViewGroup = new NoteViewGroup(context, !accidental.isEmpty(), !duration.isEmpty(), !dot.isEmpty(), tieStart, tieEnd);
        RelativeLayout.LayoutParams rlp =
                new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        if (noteViewGroupId == ShowScoreActivity.noteStartId) { // is the first note, make it align left
            rlp.addRule(RelativeLayout.RIGHT_OF, R.id.bar_width_view);
        }
        else if (noteViewGroupId > ShowScoreActivity.noteStartId) { // not the first note, make it right of the left one
            rlp.addRule(RelativeLayout.RIGHT_OF,
                    this.findViewById(noteViewGroupId - 1).getId());
        }

        noteViewGroup.setLayoutParams(rlp);
        noteViewGroup.setId(noteViewGroupId);

        // add blank view
        noteViewGroup.printBlankView();

        // add number view
        noteViewGroup.printNumberView(note);

        // has accidental
        noteViewGroup.printAccidentalView(accidental);

        // has dot
        noteViewGroup.printDottedView(dot);

        // duration to beam
        if("whqistx".contains(duration))
            noteViewGroup.printBeamView(duration);

        // octave is 0 ~ 3, 5 ~ 8
        noteViewGroup.printOctaveView(octave);

        this.addView(noteViewGroup);
        this.width+=noteViewGroup.getViewWidth();
        curNoteViewGroupWidth = noteViewGroup.getViewWidth();
    }

    public void printWord(String word, int i, boolean hasAccidentalView, boolean hasDottedView) {
        int noteViewGroupId = i + ShowScoreActivity.noteStartId;

        WordView wordView = new WordView(context, hasAccidentalView, hasDottedView);
        wordView.setId(i + ShowScoreActivity.wordStartId);
        wordView.setWord(word);

        RelativeLayout.LayoutParams rlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        rlp.addRule(RelativeLayout.BELOW, R.id.bar_width_view);
        if (noteViewGroupId == ShowScoreActivity.noteStartId) { // is the first word, make it align left
            rlp.addRule(RelativeLayout.RIGHT_OF, R.id.bar_width_view);
        }
        else if (noteViewGroupId > ShowScoreActivity.noteStartId) { // not the first word, make it right of the left one
            rlp.addRule(RelativeLayout.RIGHT_OF,
                    noteViewGroupId - 1);
        }

        wordView.setLayoutParams(rlp);
        this.addView(wordView);
    }

    public float getCurNoteViewGroupWidth() {
        return curNoteViewGroupWidth;
    }
}