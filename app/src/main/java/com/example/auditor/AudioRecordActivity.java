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

import java.io.File;
import java.io.IOException;

import be.tarsos.dsp.io.TarsosDSPAudioFormat;


public class AudioRecordActivity extends ActionBarActivity {
    private static final String LOG_TAG = "AudioRecordActivity";
    private static String mFileName = null;
    private ExtAudioRecorder extAudioRecorder = null;
    private final Context context = this;
    private File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");
    public static final int bufferSize = 1024;
    public static TarsosDSPAudioFormat tarsosDSPAudioFormat;

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording)
                    setText("Stop recording");
                else
                    setText("Start recording");

                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
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
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.audioRecordRL);
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

//        extAudioRecorder = ExtAudioRecorder.getInstanse(ExtAudioRecorder.RECORDING_UNCOMPRESSED);
//        // use exAudioRecorder to set TarsosDSP Audio format
//        tarsosDSPAudioFormat =
//                new TarsosDSPAudioFormat(
//                        extAudioRecorder.getSampleRate(),
//                        extAudioRecorder.getBitSamples(),
//                        extAudioRecorder.getChannels(),
//                        false,  // indicates whether the data is signed or unsigned
//                        false); // indicates whether the data for a single sample
//
//        // open a file
//        try {
//            inputStream = new FileInputStream(file);
//        } catch (FileNotFoundException e) {
//            Log.e(LOG_TAG, "Failed to open a file!");
//        }
//
//        // set audio stream
//        UniversalAudioInputStream universalAudioInputStream =
//                new UniversalAudioInputStream(inputStream, tarsosDSPAudioFormat);
//
//        // set pitch detect things
//        AudioDispatcher dispatcher = new AudioDispatcher(
//                universalAudioInputStream,
//                bufferSize,
//                bufferSize / 2);
//        PitchDetectionHandler pdh = new PitchDetectionHandler() {
//            @Override
//            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
//                final float pitchInHz = result.getPitch();
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.e(LOG_TAG, "pitch: " + pitchInHz);
//                    }
//                });
//            }
//        };
//        AudioProcessor pp = new PitchProcessor(
//                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
//                extAudioRecorder.getSampleRate(),
//                bufferSize, pdh);
//        dispatcher.addAudioProcessor(pp);

        // TODO This thread is going to get a specific file pitch, so don't run this line unless you have the file.
//        new Thread(dispatcher,"Audio Dispatcher").start();
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
        Intent intent = new Intent(this, AudioFileActivity.class);
        startActivity(intent);
    }
    public void goSheetMusic(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, ShowSheetMusicActivity.class);
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

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File from = new File(mFileName);
                                File to = new File(auditorDir.getAbsolutePath() + "/" +
                                                userInput.getText() + ".wav");
                                if(from.renameTo(to))
                                    Log.e(LOG_TAG, "Set name successfully!");
                                else
                                    Log.e(LOG_TAG, "Set name failed!");
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}

// TODO turns pitch to notes
// TODO how to get the Beats(time)