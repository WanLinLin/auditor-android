package com.example.auditor;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.example.auditor.score.NumberedMusicalNotationParser;

import java.io.File;

public class ShowSheetMusicActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ShowSheetMusicActivity";
    private File musicDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");
    private final boolean SHOW_PARENT_VIEW_GROUP_COLOR = false;

    // width:height = 2:3
    public int noteHeight = 200;
//    public int noteWidth = noteHeight / 3 * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sheet_music);

        // root view
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_show_sheet_music);

        // TODO add custom vertical and horizontal scroll view
        // TODO http://stackoverflow.com/questions/2044775/scrollview-vertical-and-horizontal-in-android

        NumberedMusicalNotationParser numberedMusicalNotationParser =
                new NumberedMusicalNotationParser(this, noteHeight, "B4t");

        numberedMusicalNotationParser.parse();
        rl.addView(numberedMusicalNotationParser.getScore());
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