package com.example.auditor;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {
    private static final String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private static final String LOG_TAG = MainActivity.class.getName();

    private boolean isPlaying = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        final MediaPlayer mediaPlayer = new MediaPlayer();
//        File f = new File(auditorDir + "midi/bach_bourree.mid");
        File f = new File(auditorDir + "midi/test.mid");

        try {
            FileInputStream fis = new FileInputStream(f);
            FileDescriptor fd = fis.getFD();
            mediaPlayer.setDataSource(fd);
            mediaPlayer.prepare();
        }
        catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "file not found");
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "ioe");
        }

        Button midiButton = (Button)findViewById(R.id.midi);
        midiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    isPlaying = false;
                }
                else {
                    mediaPlayer.start();
                    isPlaying = true;
                }
            }
        });
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
