package com.example.auditor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.auditor.score.MeasureViewGroup;
import com.example.auditor.score.NoteViewGroup;
import com.example.auditor.score.NumberedMusicalNotationParser;
import com.example.auditor.score.PartViewGroup;
import com.example.auditor.score.ScoreViewGroup;

import org.jfugue.Pattern;

import java.io.File;
import java.io.IOException;

public class ShowScoreActivity extends ActionBarActivity {
    private static final String LOG_TAG = ShowScoreActivity.class.getName();
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private String scoreName;

    // two dimension scroll view
    private ScoreViewGroup score;
    private RelativeLayout scoreContainer;
    private Pattern pattern;
    private NumberedMusicalNotationParser numberedMusicalNotationParser;

    private float mx, my;
    private ScrollView vScroll;

    private HorizontalScrollView hScroll;
    public static final int partStartId = 10001;

    public static final int measureStartId = 101;
    public static final int noteStartId = 1;
    public static int defaultNoteHeight = 300;

    public static int noteHeight = defaultNoteHeight;

    public static int noteWidth = noteHeight / 3 * 2;

    // pinch to zoom
//    private ScaleGestureDetector mScaleDetector;
    public static float mScaleFactor = 1.f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        // reset parameters
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
                if(mScaleFactor < 3) {
                    mScaleFactor += 0.5;
                    zoom();

                    if(mScaleFactor == 3) {
                        Toast.makeText(
                                ShowScoreActivity.this,
                                "最大!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
                return true;
            case R.id.action_zoom_out:
                if (mScaleFactor > 0.5) {
                    mScaleFactor -= 0.5;
                    zoom();

                    if(mScaleFactor == 0.5) {
                        Toast.makeText(
                                ShowScoreActivity.this,
                                "最小!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
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

        NoteChildViewDimension.NUMBER_VIEW_WIDTH = (int) (noteWidth * 0.5);
        NoteChildViewDimension.NUMBER_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH = (int) (noteWidth * 0.25);
        NoteChildViewDimension.ACCIDENTAL_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        NoteChildViewDimension.BEAM_VIEW_HEIGHT = Math.round(noteHeight * 0.15f);

        NoteChildViewDimension.BLANK_VIEW_WIDTH = (int) (noteWidth * 0.25);
        NoteChildViewDimension.BLANK_VIEW_HEIGHT = (int) (noteHeight * 0.225f);

        NoteChildViewDimension.DOTTED_VIEW_WIDTH = (int) (noteWidth * 0.25);
        NoteChildViewDimension.DOTTED_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        NoteChildViewDimension.OCTAVE_VIEW_WIDTH = (int) (noteWidth * 0.5);
        NoteChildViewDimension.OCTAVE_VIEW_HEIGHT = (int) (noteHeight * 0.225f);

        NoteChildViewDimension.BAR_STROKE_WIDTH = Math.round(noteHeight * 0.03f);
        NoteChildViewDimension.BAR_VIEW_HEIGHT = noteHeight;

        NoteChildViewDimension.TIE_STROKE_WIDTH = Math.round(noteHeight * 0.022f);
        NoteChildViewDimension.TIE_VIEW_HEIGHT = Math.round(noteHeight * 0.225f);

        NoteChildViewDimension.WORD_VIEW_HEIGHT = (int) (noteHeight * 0.3f);
    }

    public static class NoteChildViewDimension {
        public static int NUMBER_VIEW_WIDTH;
        public static int NUMBER_VIEW_HEIGHT;

        public static int ACCIDENTAL_VIEW_WIDTH;
        public static int ACCIDENTAL_VIEW_HEIGHT;

        public static int BEAM_VIEW_HEIGHT;

        public static int BLANK_VIEW_WIDTH;
        public static int BLANK_VIEW_HEIGHT;

        public static int DOTTED_VIEW_WIDTH;
        public static int DOTTED_VIEW_HEIGHT;

        public static int OCTAVE_VIEW_WIDTH;
        public static int OCTAVE_VIEW_HEIGHT;

        public static int BAR_STROKE_WIDTH;
        public static int BAR_VIEW_HEIGHT;

        public static int TIE_STROKE_WIDTH;
        public static int TIE_VIEW_HEIGHT;

        public static int WORD_VIEW_HEIGHT;
    }

    public void zoom() {
        setDimensions();

        for (int i = 0; i < 1; i++) {
            PartViewGroup part = (PartViewGroup)score.findViewById(i + partStartId);
            if (part == null) break;

            int curX = 0;
            part.clearTieInfo();
            part.getTieViewGroup().removeAllViews();

            for(int j = 0; j < partStartId - measureStartId; j++) {
                MeasureViewGroup measure = (MeasureViewGroup)part.findViewById(j + measureStartId);
                if (measure == null) break;

                curX += ShowScoreActivity.NoteChildViewDimension.BAR_STROKE_WIDTH * 3;

                for(int k = 0; k < measureStartId - noteStartId; k++) {
                    NoteViewGroup note = (NoteViewGroup)measure.findViewById(k + noteStartId);
                    if (note == null) break;

                    int noteViewWidth = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
                    if(note.hasAccidentalView())
                        noteViewWidth += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
                    if(note.hasDottedView())
                        noteViewWidth += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;

                    if(note.isTieEnd()) {
                        part.addTieInfo(new Pair<>((curX + noteViewWidth/2), "end"));
                    }
                    if(note.isTieStart()) {
                        part.addTieInfo(new Pair<>((curX + noteViewWidth/2), "start"));
                    }
                    curX += noteViewWidth;
                }
            }
            part.printTieView();
            part.requestLayout();
        }
    }

    // TODO add lyric view, each word will align to a single note

    // TODO convert view to bitmap on android
    // http://stackoverflow.com/questions/5536066/convert-view-to-bitmap-on-android
}
