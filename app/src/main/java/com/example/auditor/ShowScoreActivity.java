package com.example.auditor;

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

import com.example.auditor.score.NumberedMusicalNotationParser;
import com.example.auditor.score.ScoreViewGroup;

import org.jfugue.Pattern;

import java.io.File;
import java.io.IOException;

public class ShowScoreActivity extends ActionBarActivity {
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private String scoreName;

    // width:height = 2:3
    public int noteHeight = 300;
//    public int noteWidth = noteHeight / 3 * 2;

    private float mx, my;
    private ScrollView vScroll;
    private HorizontalScrollView hScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        vScroll = (ScrollView) findViewById(R.id.vScroll);
        hScroll = (HorizontalScrollView) findViewById(R.id.hScroll);
        RelativeLayout scoreContainer = (RelativeLayout)findViewById(R.id.score_container);

        Pattern pattern;

        Intent intent = getIntent();
        scoreName  = intent.getStringExtra("score name");

        try {
            pattern = Pattern.loadPattern(new File(auditorDir + scoreName + ".txt"));

            final NumberedMusicalNotationParser numberedMusicalNotationParser =
                    new NumberedMusicalNotationParser(this, noteHeight, pattern.getMusicString());

            numberedMusicalNotationParser.parse();
            ScoreViewGroup scoreViewGroup = numberedMusicalNotationParser.getScoreViewGroup();
            scoreContainer.addView(scoreViewGroup);
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

        switch (event.getAction()) {

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
        }

        return true;
    }

    // TODO convert view to bitmap on android
    // http://stackoverflow.com/questions/5536066/convert-view-to-bitmap-on-android
}
