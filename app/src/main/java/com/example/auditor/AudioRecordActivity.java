package com.example.auditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.auditor.song.ExtAudioRecorder;

import java.io.File;
import java.io.IOException;


public class AudioRecordActivity extends ActionBarActivity {
    private static final String LOG_TAG = "AudioRecordActivity";
    private static String mFileName = null;
    private ExtAudioRecorder extAudioRecorder = null;
    private final Context context = this;
    private File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");
    public static final int bufferSize = 1024;
    private EditText userInput;

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording)
                    setText(R.string.stop);
                else
                    setText(R.string.record);

                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText(R.string.record);
            setOnClickListener(clicker);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        auditorDir.mkdirs();
        mFileName = auditorDir.getAbsolutePath() + "/tmp.wav";

        /* ----LAYOUT SETTING---- */
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_audio_record);
        RecordButton recordButton = new RecordButton(this);
        // setting basic recordButton parameters
        RelativeLayout.LayoutParams btLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        // set recordButton to be the center of the screen
        btLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        btLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        // add recordButton into the audioRecordRL
        rl.addView(recordButton, btLayoutParams);
        /* ----LAYOUT SETTING---- */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio_record, menu);
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

    /* change activity */
    public void goToAudioFile(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, AudioFileListActivity.class);
        startActivity(intent);
    }
    public void goToScoreList(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, ScoreFileListActivity.class);
        startActivity(intent);
    }

    /* record */
    private void onRecord(boolean start) {
        if (start)
            startRecording();
        else
            stopRecording();
    }
    private void startRecording() {
        // false means not compressed
        extAudioRecorder = ExtAudioRecorder.getInstanse(ExtAudioRecorder.RECORDING_UNCOMPRESSED);
        extAudioRecorder.setOutputFile(mFileName);

        try {
            extAudioRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, ".prepare() failed");
        }
        extAudioRecorder.start();
    }
    private void stopRecording() {
        extAudioRecorder.stop();
        extAudioRecorder.release();
        extAudioRecorder = null;

        setFileName();
    }
    private void setFileName(){
        // get audio_record_popup_window.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.audio_record_popup_rename, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set audio_record_popup_window.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            File from = new File(mFileName);
                            File to = new File(auditorDir.getAbsolutePath() + "/" +
                                            userInput.getText() + ".wav");

                            if(from.renameTo(to)) {
                                Log.e(LOG_TAG, "Set name successfully!");
                            }
                            else {
                                Log.e(LOG_TAG, "Set name failed!");
                            }
                        }
                    })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
// TODO swipe tabs!!!
// TODO https://www.youtube.com/watch?v=Xu2yoIqTsj4