package com.example.auditor;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.example.auditor.score.NumberedMusicalNotationParser;

import org.jfugue.Pattern;

import java.io.File;
import java.io.IOException;

public class ShowSheetMusicActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ShowSheetMusicActivity";
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private final boolean SHOW_PARENT_VIEW_GROUP_COLOR = false;

    // width:height = 2:3
    public int noteHeight = 75;
//    public int noteWidth = noteHeight / 3 * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sheet_music);

        // root view
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_show_sheet_music);

        // TODO add custom vertical and horizontal scroll view
        // TODO http://stackoverflow.com/questions/2044775/scrollview-vertical-and-horizontal-in-android

        Pattern pattern;

        try {
            pattern = Pattern.loadPattern(new File(auditorDir + "pattern.txt"));

            NumberedMusicalNotationParser numberedMusicalNotationParser =
                    new NumberedMusicalNotationParser(this, noteHeight, pattern.getMusicString());

            numberedMusicalNotationParser.parse();
            rl.addView(numberedMusicalNotationParser.getScore());
        }
        catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_sheet_music, menu);
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
    protected void onDestroy(){
        super.onDestroy();
    }
}