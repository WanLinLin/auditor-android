package com.example.auditor.score;

import android.content.Context;
import android.graphics.Color;
import android.widget.RelativeLayout;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/26.
 * A note view group contains number view, accidental view, dotted view, octave view, and beam view.
 */
public class NoteViewGroup extends RelativeLayout {
    private static final String LOG_TAG = NoteViewGroup.class.getName();
    private final boolean SHOW_NOTE_VIEW_GROUP_COLOR = false;
    private Context context;
    private boolean hasAccidentalView;
    private boolean hasDottedView;
    private boolean hasBeamView;
    private boolean tieStart;
    private boolean tieEnd;

    private int width;

    public NoteViewGroup(Context context) {
        super(context);
    }

    public NoteViewGroup(Context context, boolean hasAccidentalView, boolean hasBeamView, boolean hasDottedView, boolean tieStart, boolean tieEnd) {
        super(context);
        this.context = context;
        this.hasAccidentalView = hasAccidentalView;
        this.hasBeamView = hasBeamView;
        this.hasDottedView = hasDottedView;
        this.tieStart = tieStart;
        this.tieEnd = tieEnd;

        width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
        if(hasAccidentalView)
            width += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
        if(hasDottedView)
            width += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
        if(hasAccidentalView)
            width += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
        if(hasDottedView)
            width += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;

        setMeasuredDimension(width, ShowScoreActivity.noteHeight);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        for(int i = 0; i < getChildCount(); i++) {
            getChildAt(i).requestLayout();
        }
    }

    public void printBlankView() {
        BlankView blankView = new BlankView(context);
        blankView.setId(R.id.blank_view);
        RelativeLayout.LayoutParams blkvlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        blkvlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        blkvlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        blankView.setLayoutParams(blkvlp);
        this.addView(blankView);
    }

    public void printNumberView(String note) {
        // Number note view
        NumberView n = new NumberView(context);
        n.setId(R.id.number_view);
        n.setNote(note);
        RelativeLayout.LayoutParams nlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        nlp.addRule(RelativeLayout.BELOW, R.id.blank_view);

        if(hasAccidentalView)
            nlp.addRule(RelativeLayout.RIGHT_OF, R.id.blank_view);
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
        RelativeLayout.LayoutParams alp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        alp.addRule(RelativeLayout.BELOW, R.id.blank_view);
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
        RelativeLayout.LayoutParams dlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );

        dlp.addRule(RelativeLayout.BELOW, R.id.blank_view);
        dlp.addRule(RelativeLayout.RIGHT_OF, R.id.number_view);
        d.setLayoutParams(dlp);

        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            d.setBackgroundColor(Color.parseColor("#F5A9A9"));

        this.addView(d);
    }

    public void printBeamView(String beams) {
        // Beam view
        BeamView b = new BeamView(context);
        b.setId(R.id.beam_view);
        b.setHasAccidentalView(hasAccidentalView);
        b.setHasDottedView(hasDottedView);
        switch (beams) {
            case "w":
            case "h":
            case "q":

            case "i":
            case "s":
            case "t":
            case "x":
                b.setDuration(beams);
                break;

        }
        RelativeLayout.LayoutParams blp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );

        blp.addRule(RelativeLayout.BELOW, R.id.number_view);
        blp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        b.setLayoutParams(blp);

        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            b.setBackgroundColor(Color.parseColor("#81F7D8"));

        this.addView(b);
    }

    public void printOctaveView(String oct) {
        // Octave view
        OctaveView o = new OctaveView(context, hasBeamView, hasAccidentalView);
        int octave = Integer.parseInt(oct);
        o.setOctave(octave);
        o.setId(R.id.octave_view);
        RelativeLayout.LayoutParams olp;
        olp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        if(octave > 4) {
            olp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        else if(octave < 4 && hasBeamView) {
            olp.addRule(RelativeLayout.BELOW, R.id.beam_view);
        }
        else if(octave < 4 && !hasBeamView){
            olp.addRule(RelativeLayout.BELOW, R.id.number_view);
        }

        if(hasAccidentalView)
            olp.addRule(RelativeLayout.RIGHT_OF, R.id.blank_view);
        else
            olp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        o.setLayoutParams(olp);

        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            o.setBackgroundColor(Color.parseColor("#CCCCCC"));

        this.addView(o);
    }

    public int getViewWidth() {
        return width;
    }

    public boolean isTieEnd() {
        return tieEnd;
    }

    public boolean isTieStart() {
        return tieStart;
    }

    public boolean hasAccidentalView() {
        return hasAccidentalView;
    }

    public boolean hasDottedView() {
        return hasDottedView;
    }
}