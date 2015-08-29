package com.example.auditor;

import android.content.Intent;
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

public class ShowScoreActivity extends ActionBarActivity {
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private String scoreName;

    // width:height = 2:3
    public int noteHeight = 130;
//    public int noteWidth = noteHeight / 3 * 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        // TODO add custom vertical and horizontal scroll view
        // TODO http://stackoverflow.com/questions/2044775/scrollview-vertical-and-horizontal-in-android

        // root view
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_show_score);
        Pattern pattern;

        Intent intent = getIntent();
        scoreName  = intent.getStringExtra("score name");

        try {
            pattern = Pattern.loadPattern(new File(auditorDir + scoreName + ".txt"));

            NumberedMusicalNotationParser numberedMusicalNotationParser =
                    new NumberedMusicalNotationParser(this, noteHeight, pattern.getMusicString());

            numberedMusicalNotationParser.parse();
            rl.addView(numberedMusicalNotationParser.getScoreViewGroup());
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
}
