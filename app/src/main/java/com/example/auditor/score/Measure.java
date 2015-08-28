package com.example.auditor.score;

import android.content.Context;
import android.widget.RelativeLayout;

/**
 * Created by Wan Lin on 15/8/26.
 * A measure contains numbers of note view groups.
 */
public class Measure extends RelativeLayout {
    private Context context;
    private int noteViewGroupWidth;
    private int noteViewGroupHeight;
    private int curNoteViewGroupWidth;

    public static final int noteStartId = 1;

    public Measure(Context context) {
        super(context);
    }

    public Measure(Context context, int noteViewGroupWidth, int noteViewGroupHeight) {
        super(context);
        this.context = context;
        this.noteViewGroupWidth = Math.round(noteViewGroupWidth);
        this.noteViewGroupHeight = noteViewGroupHeight;
    }

    public void printNote(String note, String accidental, String dot, String octave, String duration, int i) {
        int noteViewGroupId = i + noteStartId;
        curNoteViewGroupWidth = noteViewGroupWidth;
        RelativeLayout.LayoutParams rlp;
        NoteViewGroup noteViewGroup;

        /* adjust note view group width */
        if (accidental.isEmpty())
            curNoteViewGroupWidth -= noteViewGroupWidth * 0.25f;
        if (dot.isEmpty())
            curNoteViewGroupWidth -= noteViewGroupWidth * 0.25f;

        /* mysterious bug: if curNoteViewGroupWidth is odd number, tie view would crash */
        if(curNoteViewGroupWidth % 2 != 0)
            curNoteViewGroupWidth += 1;

        rlp = new RelativeLayout.LayoutParams(curNoteViewGroupWidth, noteViewGroupHeight);

        if (noteViewGroupId == noteStartId) { // is the first note, make it align left
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        else if (noteViewGroupId > noteStartId) { // not the first note, make it right of the left one
            rlp.addRule(RelativeLayout.RIGHT_OF,
                    this.findViewById(noteViewGroupId - 1).getId());
        }

        noteViewGroup = new NoteViewGroup(context, noteViewGroupWidth, noteViewGroupHeight);
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