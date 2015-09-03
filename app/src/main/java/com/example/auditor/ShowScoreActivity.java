package com.example.auditor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.example.auditor.score.NumberedMusicalNotationParser;
import com.example.auditor.score.ScoreViewGroup;

import org.jfugue.Pattern;

import java.io.File;
import java.io.IOException;

public class ShowScoreActivity extends ActionBarActivity {
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private String scoreName;

    // width:height = 2:3
    private static int defaultNoteHeight = 300;

    public static int noteHeight = defaultNoteHeight;
    public static int noteWidth = noteHeight / 3 * 2;

    // two dimension scroll view
    private float mx, my;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;

    // pinch to zoom
    private ScaleGestureDetector mScaleDetector;
    public static float mScaleFactor = 1.f;

    private ScoreViewGroup score;
    private RelativeLayout scoreContainer;
    private Pattern pattern;
    private NumberedMusicalNotationParser numberedMusicalNotationParser;
//    private ZoomView zoomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        noteHeight = defaultNoteHeight;
        noteWidth = noteHeight / 3 * 2;

        Intent intent = getIntent();
        scoreName  = intent.getStringExtra("score name");
        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

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
        getMenuInflater().inflate(R.menu.menu_musc_score, menu);
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

        mScaleDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            noteHeight = (int) (defaultNoteHeight * mScaleFactor);
            noteWidth = noteHeight / 3 * 2;


            numberedMusicalNotationParser.getScoreViewGroup().removeAllViews();
            numberedMusicalNotationParser.parse();
            scoreContainer.invalidate();

            return true;
        }
    }

    // TODO pinch zoom
    // http://stackoverflow.com/questions/5216658/pinch-zoom-for-custom-view

    // TODO redraw all child view
    // http://stackoverflow.com/questions/5991968/how-to-force-an-entire-layout-view-refresh

    // TODO convert view to bitmap on android
    // http://stackoverflow.com/questions/5536066/convert-view-to-bitmap-on-android
}
