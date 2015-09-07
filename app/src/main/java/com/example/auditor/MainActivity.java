package com.example.auditor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.auditor.score.AccidentalView;
import com.example.auditor.score.BeamView;
import com.example.auditor.score.BlankView;
import com.example.auditor.score.DottedView;
import com.example.auditor.score.NoteViewGroup;
import com.example.auditor.score.NumberView;
import com.example.auditor.score.OctaveView;


public class MainActivity extends ActionBarActivity {

    // pinch to zoom
    private ScaleGestureDetector mScaleDetector;
    public static float mScaleFactor = 1.f;

    private NoteViewGroup nvg;
    private NumberView nv;
    private AccidentalView av;
    private BlankView blkv;
    private OctaveView ov;
    private DottedView dv;
    private BeamView bv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        RelativeLayout root = (RelativeLayout)findViewById(R.id.activity_main);

        nvg = new NoteViewGroup(this, true, true, true);
        nvg.setHasAccidentalView(true);
        nvg.setHasDottedView(true);
        nvg.setBackgroundColor(Color.LTGRAY);
        RelativeLayout.LayoutParams nvglp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        nvglp.topMargin = 200;
        nvglp.leftMargin = 200;
        nvg.setLayoutParams(nvglp);

        blkv = new BlankView(this);
        blkv.setId(R.id.blank_view);

        // top octave
        ov = new OctaveView(this);
        ov.setOctave(2);
        ov.setId(R.id.octave_view);
        RelativeLayout.LayoutParams ovlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
//        ovlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ovlp.addRule(RelativeLayout.RIGHT_OF, R.id.blank_view);
        ovlp.addRule(RelativeLayout.BELOW, R.id.beam_view);
        ov.setLayoutParams(ovlp);

        av = new AccidentalView(this);
        av.setAccidental("b");
        av.setId(R.id.accidental_view);
        RelativeLayout.LayoutParams avlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        avlp.addRule(RelativeLayout.BELOW, R.id.blank_view);
        av.setLayoutParams(avlp);

        nv = new NumberView(this);
        nv.setNote("C");
        nv.setId(R.id.number_view);
        RelativeLayout.LayoutParams nvlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        nvlp.addRule(RelativeLayout.RIGHT_OF, R.id.blank_view);
        nvlp.addRule(RelativeLayout.BELOW, R.id.blank_view);
        nv.setLayoutParams(nvlp);

        dv = new DottedView(this);
        dv.setLength(".");
        dv.setId(R.id.dotted_view);
        RelativeLayout.LayoutParams dvlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        dvlp.addRule(RelativeLayout.RIGHT_OF, R.id.number_view);
        dvlp.addRule(RelativeLayout.BELOW, R.id.blank_view);
        dv.setLayoutParams(dvlp);

        bv = new BeamView(this);
        bv.setBeams(3);
        bv.setId(R.id.beam_view);
        RelativeLayout.LayoutParams bvlp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
        bv.setHasAccidentalView(true);
        bv.setHasDottedView(true);
        bvlp.addRule(RelativeLayout.BELOW, R.id.number_view);
        bv.setLayoutParams(bvlp);

        nvg.addView(blkv);
        nvg.addView(av);
        nvg.addView(nv);
        nvg.addView(ov);
        nvg.addView(dv);
        nvg.addView(bv);
        root.addView(nvg);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));

            ShowScoreActivity.noteHeight = (int) (ShowScoreActivity.defaultNoteHeight * mScaleFactor);
            ShowScoreActivity.noteWidth = ShowScoreActivity.noteHeight / 3 * 2;

            ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH = (int) (ShowScoreActivity.noteWidth * 0.5);
            ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.4);

            ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH = (int) (ShowScoreActivity.noteWidth * 0.25);
            ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.4);

            ShowScoreActivity.NoteChildViewDimension.BEAM_VIEW_HEIGHT = Math.round(ShowScoreActivity.noteHeight * 0.15f);

            ShowScoreActivity.NoteChildViewDimension.BLANK_VIEW_WIDTH = (int) (ShowScoreActivity.noteWidth * 0.25);
            ShowScoreActivity.NoteChildViewDimension.BLANK_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.225f);

            ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH = (int)(ShowScoreActivity.noteWidth * 0.25);
            ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.4);

            ShowScoreActivity.NoteChildViewDimension.OCTAVE_VIEW_WIDTH = (int)(ShowScoreActivity.noteWidth * 0.5);
            ShowScoreActivity.NoteChildViewDimension.OCTAVE_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.225f);

            nvg.requestLayout();
            return true;
        }
    }

    public void goToAudioRecord(View view){
        Intent intent = new Intent(this, AudioRecordActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
