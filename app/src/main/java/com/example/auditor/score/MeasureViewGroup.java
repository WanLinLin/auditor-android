package com.example.auditor.score;

import android.content.ClipDescription;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
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

    public static MeasureViewGroup curEditMeasure;

    public MeasureViewGroup(Context context) {
        super(context);
        this.context = context;
    }

    public MeasureViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MeasureViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
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
    public boolean onDragEvent(DragEvent event) {
        int x = (int) event.getX();

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                // Determines if this View can accept the dragged data
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    return true;
                }
                return false;

            case DragEvent.ACTION_DRAG_ENTERED: // touch point enter the view bound
                return true;

            case DragEvent.ACTION_DRAG_LOCATION: // touch point is in the view bound
                int blankNoteWidth = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
                int prevNoteCenterX = 0;

                // reset all note margin and padding to 0
                for(int i = 0; i < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; i++) {
                    NoteViewGroup n = (NoteViewGroup)findViewById(ShowScoreActivity.noteStartId + i);
                    if(n == null) break; // search beyond the last note, break

                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)n.getLayoutParams();
                    lp.leftMargin = 0;
                    n.setLayoutParams(lp);
                }
                setPadding(0, 0, 0, 0);

                if(x > getWidth() - blankNoteWidth) {
                    setPadding(0, 0, blankNoteWidth, 0);
                    break;
                }
                else {
                    // find the position to set note left margin
                    for (int i = 0; i < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; i++) {
                        NoteViewGroup n = (NoteViewGroup) findViewById(ShowScoreActivity.noteStartId + i);
                        if (n == null) break; // search beyond the last note, break

                        // the note is right of the touch position
                        if (x < n.getLeft() + n.getWidth() / 2 && x > prevNoteCenterX) {
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) n.getLayoutParams();
                            lp.leftMargin = blankNoteWidth;
                            n.setLayoutParams(lp);
                            break;
                        }
                        prevNoteCenterX = n.getLeft() + n.getWidth() / 2;
                    }
                }
                requestLayout();

                return true;

            case DragEvent.ACTION_DRAG_EXITED: // touch point leave the view bound
                // reset all note margin and padding to 0
                for(int i = 0; i < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; i++) {
                    NoteViewGroup n = (NoteViewGroup)findViewById(ShowScoreActivity.noteStartId + i);
                    if(n == null) break; // search beyond the last note, break

                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)n.getLayoutParams();
                    lp.leftMargin = 0;
                    n.setLayoutParams(lp);
                }
                setPadding(0, 0, 0, 0);
                return true;

            case DragEvent.ACTION_DROP:
                int newNoteId = 0;

                // reset all note margin to 0
                for(int i = 0; i < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; i++) {
                    NoteViewGroup n = (NoteViewGroup)findViewById(ShowScoreActivity.noteStartId + i);
                    if(n == null) break; // search beyond the last note, break

                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)n.getLayoutParams();
                    lp.leftMargin = 0;
                    n.setLayoutParams(lp);
                }
                setPadding(0, 0, 0, 0);
                requestLayout();

                // search which note id should be use
                for (int i = 0; i < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; i++) {
                    NoteViewGroup n = (NoteViewGroup) findViewById(ShowScoreActivity.noteStartId + i);
                    if (n == null) break; // search beyond the last note, break

                    newNoteId = n.getId();
                    if (x <= n.getRight()) break; // the note is right of the drop position
                }

                // if drop on the last note
                if(x > getWidth() - ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH) {
                    // create a new note insert into the measure
                    NoteContext noteContext = new NoteContext();
                    noteContext.note = "C";
                    noteContext.octave = "4";
                    noteContext.duration = "q";
                    noteContext.tieEnd = false;
                    printNoteAndWord(noteContext, newNoteId - ShowScoreActivity.noteStartId + 1);

                    NoteViewGroup n = (NoteViewGroup)findViewById(newNoteId + 1);
                    n.collectNoteContextToEditButton();
                }
                else {
                    // realign all notes and words behind the new note
                    for (int i = (getChildCount() - 1) / 2; i > newNoteId - 1; i--) {
                        NoteViewGroup n = (NoteViewGroup) findViewById(i);
                        if (n == null) break; // search beyond the last note, break

                        n.setId(i + 1);
                        RelativeLayout.LayoutParams nlp = new RelativeLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        nlp.addRule(ALIGN_PARENT_TOP);
                        nlp.addRule(RIGHT_OF, i);
                        n.setLayoutParams(nlp);
                    }

                    // create a new note insert into the measure
                    NoteContext noteContext = new NoteContext();
                    noteContext.note = "C";
                    noteContext.octave = "4";
                    noteContext.duration = "q";
                    noteContext.tieEnd = false;
                    printNoteAndWord(noteContext, newNoteId - ShowScoreActivity.noteStartId);

                    NoteViewGroup n = (NoteViewGroup)findViewById(newNoteId);
                    n.collectNoteContextToEditButton();
                }

                requestLayout();
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                return true;

            default: // An unknown action type was received.
                Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                return false;
        }
        return false;
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
    }

    public void printNoteAndWord(NoteContext noteContext, int i) {
        printNote(noteContext, i);
        printWord(noteContext.word, i, !noteContext.accidental.isEmpty(), !noteContext.dot.isEmpty());
    }

    public void printNote(NoteContext noteContext, int i) {
        int noteViewGroupId = i + ShowScoreActivity.noteStartId;
        NoteViewGroup noteViewGroup = new NoteViewGroup(context, noteContext);
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
        noteViewGroup.printNumberView(noteContext.note);

        // has accidental
        noteViewGroup.printAccidentalView(noteContext.accidental);

        // has dot
        noteViewGroup.printDottedView(noteContext.dot);

        // duration to beam
        if("whqistx".contains(noteContext.duration))
            noteViewGroup.printBeamView(noteContext.duration);

        // octave is 0 ~ 3, 5 ~ 8
        noteViewGroup.printOctaveView(noteContext.octave);

        this.addView(noteViewGroup);
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