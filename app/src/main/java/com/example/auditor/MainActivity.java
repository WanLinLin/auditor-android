package com.example.auditor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;


public class MainActivity extends Activity {
    private static final String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private static final String LOG_TAG = "MainActivity";

    // TODO upload lyric using phone id
    // android phone id
    public static String androidId;

    private boolean isPlaying = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.e(LOG_TAG, "android id: " + androidId);

        File auditor = new File(auditorDir);
        auditor.mkdir();

        File score = new File(auditorDir + "score/");
        score.mkdir();

        File wav = new File(auditorDir + "wav/");
        wav.mkdir();

        File midi = new File(auditorDir + "midi/");
        midi.mkdir();

        File txt = new File(auditorDir + "txt/");
        txt.mkdir();
    }

    public void goToAudioRecord(View view){
        Intent intent = new Intent(this, AudioRecordActivity.class);
        startActivity(intent);
    }

    public void goToSlidingTabActivity(View view){
        Intent intent = new Intent(this, SlidingTabActivity.class);
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
