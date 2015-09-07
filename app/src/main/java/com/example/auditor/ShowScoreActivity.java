package com.example.auditor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.auditor.score.NumberedMusicalNotationParser;
import com.example.auditor.score.PartViewGroup;
import com.example.auditor.score.ScoreViewGroup;

import org.jfugue.Pattern;

import java.io.File;
import java.io.IOException;

public class ShowScoreActivity extends ActionBarActivity {
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private String scoreName;

    public static final int partStartId = 10001;
    public static final int measureStartId = 101;
    public static final int noteStartId = 1;
    // width:height = 2:3
    public static int defaultNoteHeight = 300;

    public static int noteHeight = defaultNoteHeight;
    public static int noteWidth = noteHeight / 3 * 2;

    // two dimension scroll view
    private float mx, my;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;

    // pinch to zoom
//    private ScaleGestureDetector mScaleDetector;
    public static float mScaleFactor = 1.f;

    private ScoreViewGroup score;
    private RelativeLayout scoreContainer;
    private Pattern pattern;
    private NumberedMusicalNotationParser numberedMusicalNotationParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        mScaleFactor = 1.f;
        setDimensions();

        Intent intent = getIntent();
        scoreName  = intent.getStringExtra("score name");
//        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        vScroll = (ScrollView) findViewById(R.id.vScroll);
        hScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
        scoreContainer = (RelativeLayout)findViewById(R.id.score_container);


        try {
            pattern = Pattern.loadPattern(new File(auditorDir + scoreName + ".txt"));

            numberedMusicalNotationParser =
                    new NumberedMusicalNotationParser(this, pattern.getMusicString());

            numberedMusicalNotationParser.parse();
            score = numberedMusicalNotationParser.getScoreViewGroup();
            scoreContainer.addView(score);
        }
        catch (IOException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_score, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_zoom_in:
                zoom(true);
                return true;
            case R.id.action_zoom_out:
                zoom(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX, curY;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                mx = event.getX();
                my = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                mx = curX;
                my = curY;
                break;
            case MotionEvent.ACTION_UP:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //This event fires when a second finger is pressed onto the screen
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //This event fires when the second finger is off the screen, but the first finger
                break;
        }

//        mScaleDetector.onTouchEvent(event);
        return true;
    }

//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//            mScaleFactor *= detector.getScaleFactor();
//
//            // Don't let the object get too small or too large.
//            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));
//
//            if(mScaleFactor % 0.5 <= 0.03) {
//                setDimensions();
//
//                for (int i = 0; i < 1; i++) {
//                    PartViewGroup part = (PartViewGroup) score.findViewById(i + partStartId);
//                    if (part == null)
//                        break;
//                    part.requestLayout();
//                }
//            }
//
//            return true;
//        }
//    }

    private void setDimensions() {
        noteHeight = (int) (defaultNoteHeight * mScaleFactor);
        noteWidth = noteHeight / 3 * 2;

        NoteChildViewDimension.NUMBER_VIEW_WIDTH = (int) (ShowScoreActivity.noteWidth * 0.5);
        NoteChildViewDimension.NUMBER_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.4);

        NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH = (int) (ShowScoreActivity.noteWidth * 0.25);
        NoteChildViewDimension.ACCIDENTAL_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.4);

        NoteChildViewDimension.BEAM_VIEW_HEIGHT = Math.round(ShowScoreActivity.noteHeight * 0.15f);

        NoteChildViewDimension.BLANK_VIEW_WIDTH = (int) (ShowScoreActivity.noteWidth * 0.25);
        NoteChildViewDimension.BLANK_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.225f);

        NoteChildViewDimension.DOTTED_VIEW_WIDTH = (int)(ShowScoreActivity.noteWidth * 0.25);
        NoteChildViewDimension.DOTTED_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.4);

        NoteChildViewDimension.OCTAVE_VIEW_WIDTH = (int)(ShowScoreActivity.noteWidth * 0.5);
        NoteChildViewDimension.OCTAVE_VIEW_HEIGHT = (int) (ShowScoreActivity.noteHeight * 0.225f);

        NoteChildViewDimension.BAR_STROKE_WIDTH = Math.round(noteHeight * 0.03f);
        NoteChildViewDimension.BAR_VIEW_HEIGHT = noteHeight;

        NoteChildViewDimension.TIE_STROKE_WIDTH = Math.round(noteHeight * 0.022f);
        NoteChildViewDimension.TIE_VIEW_HEIGHT = Math.round(noteHeight * 0.225f);
    }

    public static class NoteChildViewDimension {
        public static int NUMBER_VIEW_WIDTH = (int) (noteWidth * 0.5);
        public static int NUMBER_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        public static int ACCIDENTAL_VIEW_WIDTH = (int) (noteWidth * 0.25);
        public static int ACCIDENTAL_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        public static int BEAM_VIEW_HEIGHT = Math.round(noteHeight * 0.15f);

        public static int BLANK_VIEW_WIDTH = (int) (noteWidth * 0.25);
        public static int BLANK_VIEW_HEIGHT = (int) (noteHeight * 0.225f);

        public static int DOTTED_VIEW_WIDTH = (int) (noteWidth * 0.25);
        public static int DOTTED_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        public static int OCTAVE_VIEW_WIDTH = (int) (noteWidth * 0.5);
        public static int OCTAVE_VIEW_HEIGHT = (int) (noteHeight * 0.225f);

        public static int BAR_STROKE_WIDTH = Math.round(noteHeight * 0.03f);
        public static int BAR_VIEW_HEIGHT = noteHeight;

        public static int TIE_STROKE_WIDTH = Math.round(noteHeight * 0.022f);
        public static int TIE_VIEW_HEIGHT = Math.round(noteHeight * 0.225f);
    }

    public void zoom(boolean b) {
        ProgressDialog progress;

        if(b)
            mScaleFactor += 0.5;
        else
            mScaleFactor -= 0.5;

        if(mScaleFactor > 3) {
            mScaleFactor = 3;
            Toast.makeText(
                    ShowScoreActivity.this,
                    "最大!",
                    Toast.LENGTH_SHORT
            ).show();
        }
        else if(mScaleFactor < 0.5) {
            mScaleFactor = 0.5f;
            Toast.makeText(
                    ShowScoreActivity.this,
                    "最小!",
                    Toast.LENGTH_SHORT
                    ).show();
        }
        else {
            Runnable zoom = new Runnable() {
                @Override
                public void run() {
                    setDimensions();
                    for (int i = 0; i < 1; i++) {
                        PartViewGroup part = (PartViewGroup) score.findViewById(i + partStartId);
                        if (part == null)
                            break;
                        part.requestLayout();
                    }
                }
            };
            runOnUiThread(zoom);
        }
    }

    // TODO convert view to bitmap on android
    // http://stackoverflow.com/questions/5536066/convert-view-to-bitmap-on-android
}
