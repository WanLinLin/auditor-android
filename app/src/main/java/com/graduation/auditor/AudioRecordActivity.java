package com.example.project.gp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


public class AudioRecordActivity extends ActionBarActivity {
    private static final String LOG_TAG = "AudioRecordActivity";
    private File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/niceshot.3gp");
    private InputStream inputStream;
    private static String mFileName = null;
    private ExtAudioRecorder extAudioRecorder = null;
    private MediaPlayer mPlayer = null;
    private final Context context = this;
    private static final int bufferSize = 1024;

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

    class PlayButton extends Button {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying)
                    setText("Stop playing");
                else
                    setText("Start playing");

                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    private void onRecord(boolean start) {
        if (start)
            startRecording();
        else
            stopRecording();
    }

    private void onPlay(boolean start) {
        if (start)
            startPlaying();
        else
            stopPlaying();
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        // false means not compressed
        extAudioRecorder = ExtAudioRecorder.getInstanse(ExtAudioRecorder.RECORDING_UNCOMPRESSED);
        extAudioRecorder.setOutputFile(mFileName);

        try {
            extAudioRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        extAudioRecorder.start();
    }

    private void stopRecording() {
        extAudioRecorder.stop();
        extAudioRecorder.release();
        extAudioRecorder = null;

        // get audio_record_popup_window.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.audio_record_popup_window, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

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
                                File to = new File(
                                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/" + userInput.getText() + ".3gp");
                                from.renameTo(to);
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

    public AudioRecordActivity() {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");
        dir.mkdirs();

        mFileName = dir.getAbsolutePath() + "tmp.3gp";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

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


        extAudioRecorder = ExtAudioRecorder.getInstanse(ExtAudioRecorder.RECORDING_UNCOMPRESSED);

        // use exAudioRecorder to set TarsosDSP Audio format
        TarsosDSPAudioFormat tarsosDSPAudioFormat =
                new TarsosDSPAudioFormat(
                        extAudioRecorder.getSampleRate(),
                        extAudioRecorder.getBitSamples(),
                        extAudioRecorder.getChannels(),
                        false,  // indicates whether the data is signed or unsigned
                        false); // indicates whether the data for a single sample


        // open a file
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "failed to open a file");
        }

        // set audio stream
        UniversalAudioInputStream universalAudioInputStream =
                new UniversalAudioInputStream(inputStream, tarsosDSPAudioFormat);

        AudioDispatcher dispatcher = new AudioDispatcher(
                universalAudioInputStream,
                bufferSize,
                bufferSize / 2);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult result, AudioEvent e) {
                final float pitchInHz = result.getPitch();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView text = (TextView) findViewById(R.id.textView);
//                        text.setText("pitch: " + pitchInHz);
                        Log.e(LOG_TAG, "pitch: " + pitchInHz);
                    }
                });
            }
        };

        AudioProcessor pp = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                extAudioRecorder.getSampleRate(),
                bufferSize, pdh);
        dispatcher.addAudioProcessor(pp);
        new Thread(dispatcher,"Audio Dispatcher").start();

        // TODO now can read file and detect the file pitch
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


    // change activity
    public void goToAudioFile(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, AudioFileActivity.class);
        startActivity(intent);
    }

    public void goSheetMusic(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, ShowSheetMusic.class);
        startActivity(intent);
    }

    // TODO complete the PitchToNotes class
    class PitchToNotes{
        float pitch;

        PitchToNotes(final float pitch){

        }
    }
}

// TODO turns pitch to notes
// TODO how to get the Beats(time)