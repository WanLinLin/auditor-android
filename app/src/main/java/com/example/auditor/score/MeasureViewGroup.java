package com.example.auditor.score;

import android.content.Context;
import android.widget.RelativeLayout;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/26.
 * A measure contains numbers of note view groups.
 */
public class MeasureViewGroup extends RelativeLayout {
    private Context context;
    private int curNoteViewGroupWidth;

    public static final int noteStartId = 1;

    public MeasureViewGroup(Context context) {
        super(context);
        this.context = context;
    }

    public void printNote(String note, String accidental, String dot, String octave, String duration, int i) {
        int noteViewGroupId = i + noteStartId;
        curNoteViewGroupWidth = ShowScoreActivity.noteWidth;
        RelativeLayout.LayoutParams rlp;
        NoteViewGroup noteViewGroup;

        /* adjust note view group width */
        if (accidental.isEmpty())
            curNoteViewGroupWidth -= ShowScoreActivity.noteWidth * 0.25f;
        if (dot.isEmpty())
            curNoteViewGroupWidth -= ShowScoreActivity.noteWidth * 0.25f;

        /* mysterious bug: if curNoteViewGroupWidth is odd number, tie view would crash */
        if(curNoteViewGroupWidth % 2 != 0)
            curNoteViewGroupWidth += 1;

        rlp = new RelativeLayout.LayoutParams(curNoteViewGroupWidth, ShowScoreActivity.noteHeight);

        if (noteViewGroupId == noteStartId) { // is the first note, make it align left
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        else if (noteViewGroupId > noteStartId) { // not the first note, make it right of the left one
            rlp.addRule(RelativeLayout.RIGHT_OF,
                    this.findViewById(noteViewGroupId - 1).getId());
        }

        noteViewGroup = new NoteViewGroup(context);
        noteViewGroup.setLayoutParams(rlp);
        noteViewGroup.setId(noteViewGroupId);

        // add number view
        noteViewGroup.printNumberView(note, !accidental.isEmpty());

        // has accidental
        if(!accidental.isEmpty())
            noteViewGroup.printAccidentalView(accidental);

        // has dot
        if(!dot.isEmpty())
            noteViewGroup.printDottedView(dot);

        // has duration shorter than quarter note
        if("istx".contains(duration))
            noteViewGroup.printBeamView(duration);

        // octave is 0 ~ 3, 5 ~ 8
        if(!octave.isEmpty())
            noteViewGroup.printOctaveView(octave, !duration.isEmpty(), !accidental.isEmpty());

        this.addView(noteViewGroup);
    }

    public float getCurNoteViewGroupWidth() {
        return curNoteViewGroupWidth;
    }
}