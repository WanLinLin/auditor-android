package com.example.auditor.score;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;
import com.example.auditor.button.OctaveButton;

/**
 * Created by Wan Lin on 15/8/26.
 * A note view group contains number view, accidental view, dotted view, octave view, and beam view.
 */
public class NoteViewGroup extends RelativeLayout {
    private static final String LOG_TAG = NoteViewGroup.class.getName();
    private Context context;
    private boolean hasAccidentalView;
    private boolean hasDottedView;
    private boolean hasBeamView;
    private boolean tieStart;
    private boolean tieEnd;

    public static NoteViewGroup curEditNote;

    private int width;

    public NoteViewGroup(Context context) {
        super(context);
        this.context = context;
        this.hasAccidentalView = false;
        this.hasDottedView = false;
        this.hasBeamView = false;
        this.tieStart = false;
        this.tieEnd = false;

        init();
    }

    public NoteViewGroup(Context context, NoteContext noteContext) {
        super(context);
        this.context = context;
        this.hasAccidentalView = !noteContext.accidental.isEmpty();
        this.hasBeamView = !noteContext.duration.isEmpty();
        this.hasDottedView = !noteContext.dot.isEmpty();
        this.tieStart = noteContext.tieStart;
        this.tieEnd = noteContext.tieEnd;

        width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
        if(hasAccidentalView)
            width += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
        if(hasDottedView)
            width += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;

        init();
    }

    private void init() {
        setTag("NoteViewGroup");

        this.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(ShowScoreActivity.scoreEditMode) {
                    MeasureViewGroup m = (MeasureViewGroup) getParent();
                    PartViewGroup p = (PartViewGroup) m.getParent();

                    Intent data = new Intent();
                    data.putExtra("note id", getId());
                    data.putExtra("measure id", m.getId());
                    data.putExtra("part id", p.getId());
                    // Create a new ClipData.Item from the NewNoteButton's tag
                    ClipData.Item item = new ClipData.Item(data);
                    // Create a new ClipData using the tag as a label
                    ClipData dragData =
                            new ClipData(
                                    v.getTag().toString(),
                                    new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                                    item
                            );
                    // Instantiates the drag shadow builder.
                    View.DragShadowBuilder myShadow = new ShowScoreActivity.MyDragShadowBuilder(NoteViewGroup.this);

                    // Starts the drag
                    v.startDrag(dragData,   // the data to be dragged
                            myShadow,       // the drag shadow builder
                            null,           // no need to use local data
                            0               // flags (not currently used, set to 0)
                    );
                }
                return false;
            }
        });
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                collectNoteContextToEditButton();
                break;
        }
        ShowScoreActivity.mx = event.getRawX(); // x position relative to screen
        ShowScoreActivity.my = event.getRawY(); // y position relative to screen
        return super.onTouchEvent(event);
    }

    public void collectNoteContextToEditButton() {
        if(ShowScoreActivity.scoreEditMode) {
            MeasureViewGroup.curEditMeasure = (MeasureViewGroup)getParent();
            curEditNote = NoteViewGroup.this;

            NumberView numberView = (NumberView)findViewById(R.id.number_view);
            AccidentalView accidentalView = (AccidentalView)findViewById(R.id.accidental_view);
            DottedView dottedView = (DottedView)findViewById(R.id.dotted_view);
            BeamView beamView = (BeamView)findViewById(R.id.beam_view);
            OctaveView octaveView = (OctaveView)findViewById(R.id.octave_view);

            String note;
            if(numberView != null) {
                note = numberView.getNote();
                ShowScoreActivity.numberButton.setNote(note);
            }
            else {
                ShowScoreActivity.numberButton.setNote("R");
                note = "R";
            }

            if(accidentalView != null) ShowScoreActivity.accidentalButton.setAccidental(accidentalView.getAccidental());
            else ShowScoreActivity.accidentalButton.setAccidental("");

            if(dottedView != null) ShowScoreActivity.dottedButton.setDot(dottedView.getDot());
            else ShowScoreActivity.dottedButton.setDot("");

            if(beamView != null) ShowScoreActivity.beamButton.setDuration(beamView.getDuration());
            else ShowScoreActivity.beamButton.setDuration("");

            if(octaveView != null) {
                if(note.equals("R")) {
                    ShowScoreActivity.topOctaveButton.setEnabled(false);
                    ShowScoreActivity.bottomOctaveButton.setEnabled(false);
//                            OctaveButton.setOctave(octaveView.getOctave());
                }
                else {
                    ShowScoreActivity.topOctaveButton.setEnabled(true);
                    ShowScoreActivity.bottomOctaveButton.setEnabled(true);
                    OctaveButton.setOctave(octaveView.getOctave().equals("") ? "4" : octaveView.getOctave());
                }
            }
            else {
                OctaveButton.setOctave("4");
            }
            ShowScoreActivity.topOctaveButton.invalidate();
            ShowScoreActivity.bottomOctaveButton.invalidate();

            if(!ShowScoreActivity.keyboard.isShown()) {
                ShowScoreActivity.keyboard.setVisibility(VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.keyboard_swipe_in);
                ShowScoreActivity.keyboard.setAnimation(animation);
                ShowScoreActivity.keyboard.animate();
            }
            else {
                ShowScoreActivity.numberButton.invalidate();
                ShowScoreActivity.accidentalButton.invalidate();
                ShowScoreActivity.dottedButton.invalidate();
                ShowScoreActivity.beamButton.invalidate();
            }
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

        nlp.addRule(RelativeLayout.RIGHT_OF, R.id.accidental_view);
        n.setLayoutParams(nlp);

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

        this.addView(d);
    }

    public void printBeamView(String beams) {
        // Beam view
        BeamView b = new BeamView(context);
        b.setId(R.id.beam_view);
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

        this.addView(b);
    }

    public void printOctaveView(String oct) {
        // Octave view
        OctaveView o = new OctaveView(context);
        int octave;
        o.setOctave(oct);
        o.setId(R.id.octave_view);
        RelativeLayout.LayoutParams olp;
        olp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        if(!oct.equals("")) {
            octave = Integer.parseInt(oct);

            if(octave > 4) olp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            else olp.addRule(RelativeLayout.BELOW, R.id.beam_view);
        }
        else {
            olp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }

        olp.addRule(RelativeLayout.RIGHT_OF, R.id.accidental_view);
        o.setLayoutParams(olp);

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

    public void setHasAccidentalView(boolean hasAccidentalView) {
        this.hasAccidentalView = hasAccidentalView;
    }

    public void setHasDottedView(boolean hasDottedView) {
        this.hasDottedView = hasDottedView;
    }
}