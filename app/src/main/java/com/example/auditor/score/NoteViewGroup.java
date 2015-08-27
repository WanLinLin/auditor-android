package com.example.auditor.score;

import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;

import com.example.auditor.R;

/**
 * Created by Wan Lin on 15/8/26.
 * A note view group contains number view, accidental view, dotted view, octave view, and beam view.
 */
public class NoteViewGroup extends RelativeLayout {
    private final boolean SHOW_NOTE_VIEW_GROUP_COLOR = false;
    private Context context;
    private int noteViewGroupWidth;
    private int noteViewGroupHeight;

    public NoteViewGroup(Context context) {
        super(context);
    }

    public NoteViewGroup(Context context, int noteViewGroupWidth, int noteViewGroupHeight) {
        super(context);
        this.context = context;
        this.noteViewGroupWidth = noteViewGroupWidth;
        this.noteViewGroupHeight = noteViewGroupHeight;
    }

    public void printNumberView(String note, boolean hasAccidentalView) {
        // Number note view
        NumberView n = new NumberView(context);
        n.setId(R.id.number_view);
        n.setNote(note);
        RelativeLayout.LayoutParams nlp = new RelativeLayout.LayoutParams((int)(noteViewGroupWidth * 0.5), (int) (noteViewGroupHeight * 0.4));
        nlp.topMargin = (int)(noteViewGroupHeight * 0.225);
        nlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        if(hasAccidentalView)
            nlp.addRule(RelativeLayout.RIGHT_OF, R.id.accidental_view);
        else
            nlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        n.setLayoutParams(nlp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            n.setBackgroundColor(Color.parseColor("#F5DA81"));

        this.addView(n);
    }

    public void printAccidentalView(String accidental) {
        // Accidental view
        AccidentalView a = new AccidentalView(context);
        a.setId(R.id.accidental_view);
        a.setAccidental(accidental);
        RelativeLayout.LayoutParams alp = new RelativeLayout.LayoutParams((int)(noteViewGroupWidth * 0.25), (int) (noteViewGroupHeight * 0.4));
        alp.topMargin = (int)(noteViewGroupHeight * 0.225);
        alp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        alp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        a.setLayoutParams(alp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            a.setBackgroundColor(Color.parseColor("#CEF6EC"));

        this.addView(a);
    }

    public void printDottedView(String length) {
        // Length view
        DottedView d = new DottedView(context);
        d.setId(R.id.dotted_view);
        d.setLength(length);
        RelativeLayout.LayoutParams dlp = new RelativeLayout.LayoutParams((int)(noteViewGroupWidth * 0.25), (int) (noteViewGroupHeight * 0.4));
        dlp.topMargin = (int)(noteViewGroupHeight * 0.225);
        dlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        dlp.addRule(RelativeLayout.RIGHT_OF, R.id.number_view);

        d.setLayoutParams(dlp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            d.setBackgroundColor(Color.parseColor("#F5A9A9"));

        this.addView(d);
    }

    public void printBeamView(String beams) {
        // Beam view
        BeamView b = new BeamView(context, noteViewGroupHeight * 0.15f);
        b.setId(R.id.beam_view);
        switch (beams) {
            case "i":
                b.setBeams(1);
                break;
            case "s":
                b.setBeams(2);
                break;
            case "t":
                b.setBeams(3);
                break;
            case "x":
                b.setBeams(4);
                break;
        }
        RelativeLayout.LayoutParams blp = new RelativeLayout.LayoutParams(noteViewGroupWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        blp.addRule(RelativeLayout.BELOW, R.id.number_view);
        blp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        b.setLayoutParams(blp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            b.setBackgroundColor(Color.parseColor("#81F7D8"));

        this.addView(b);
    }

    public void printOctaveView(String oct, boolean hasBeamView, boolean hasAccidentalView) {
        // Octave view
        OctaveView o = new OctaveView(context, noteViewGroupHeight);
        int octave = Integer.parseInt(oct);
        o.setOctave(octave);
        o.setId(R.id.octave_view);
        RelativeLayout.LayoutParams olp;
        olp = new RelativeLayout.LayoutParams((int)(noteViewGroupWidth * 0.5), RelativeLayout.LayoutParams.WRAP_CONTENT);

        if(octave > 4) {
            olp.topMargin = (int)(noteViewGroupHeight * 0.225f - o.geth());
            olp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        else if(octave < 4 && hasBeamView) {
            olp.addRule(RelativeLayout.BELOW, R.id.beam_view);
        }
        else if(octave < 4 && !hasBeamView){
            olp.addRule(RelativeLayout.BELOW, R.id.number_view);
        }

        if(hasAccidentalView)
            olp.leftMargin = (int)(noteViewGroupWidth * 0.25);
        olp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        o.setLayoutParams(olp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            o.setBackgroundColor(Color.parseColor("#CCCCCC"));

        this.addView(o);
    }
}