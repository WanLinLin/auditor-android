package com.example.auditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.auditor.convert.SongConverter;
import com.example.auditor.song.ExtAudioRecorder;
import com.example.auditor.view.RecordButton;
import com.example.auditor.view.WaveView;

import java.io.File;
import java.io.IOException;

/**
 * Created by Wan Lin on 2015/10/8.
 * AudioRecordPage
 */
public class AudioRecordPage extends Fragment{
    private static final String LOG_TAG = "AudioRecordActivity";
    private static final String wavDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/wav/";
    private static String tmpFileName = null;
    private ExtAudioRecorder extAudioRecorder = null;
    private SlidingTabActivity slidingTabActivity;

    public static final int bufferSize = 1024;
    public static final double minLevel = -90;
    public static final double maxLevel = -40;

    private EditText userInput;
    private RelativeLayout rootView;
    boolean mStartRecording = true;
    private static WaveView waveView;
    private Chronometer chronometer;

    long escapeTime = 0;

    public AudioRecordPage() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tmpFileName = wavDir + "tmp.wav";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (RelativeLayout)inflater.inflate(R.layout.audio_record_page, container, false);
        waveView = (WaveView)rootView.findViewById(R.id.wave_view);
        chronometer = (Chronometer) rootView.findViewById(R.id.chronometer);

        RecordButton recordButton = (RecordButton)rootView.findViewById(R.id.record_button);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                mStartRecording = !mStartRecording;
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        slidingTabActivity = (SlidingTabActivity)activity;
    }

    /* record */
    private void onRecord(boolean start) {
        if (start)
            startRecording();
        else
            stopRecording();
    }

    private void startRecording() {
        extAudioRecorder = ExtAudioRecorder.getInstanse(ExtAudioRecorder.RECORDING_UNCOMPRESSED);

        extAudioRecorder.setOutputFile(tmpFileName);
        try {
            extAudioRecorder.prepare();
        }
        catch (IOException e) {
            Log.e(LOG_TAG, ".prepare() failed");
        }
        extAudioRecorder.start();

        chronometer.setBase(SystemClock.elapsedRealtime() + escapeTime);
        chronometer.start();
    }

    private void stopRecording() {
        extAudioRecorder.stop();
        extAudioRecorder.release();
        extAudioRecorder = null;
        waveView.setPressure(minLevel);
        waveView.invalidate();

        escapeTime = chronometer.getBase() - SystemClock.elapsedRealtime();
        chronometer.stop();

        setFileName();
    }

    private void setFileName(){
        // get audio_record_popup_window.xml view
        LayoutInflater li = LayoutInflater.from(slidingTabActivity);
        View promptsView = li.inflate(R.layout.audio_record_popup_rename, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(slidingTabActivity);

        // set audio_record_popup_window.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.yes, // click yes
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String newFileName = userInput.getText() + ".wav";
                                File from = new File(tmpFileName);
                                File to = new File(wavDir + newFileName);

                                if(from.renameTo(to)) { // if save successfully
                                    SlidingTabAdapter adapter = slidingTabActivity.getAdapter();
                                    AudioFileListPage audioFileListPage = (AudioFileListPage)adapter.getPage(SlidingTabAdapter.AUDIO_FILE_LIST);
                                    audioFileListPage.refreshList();

                                    new ConvertSongTask().execute(newFileName);
                                }
                                else { // if save failed
                                    Toast.makeText(slidingTabActivity,
                                            getResources().getString(R.string.save_failed),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel, // click no
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                File tmpFile = new File(tmpFileName);
                                tmpFile.delete();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // reset the chronometer
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                escapeTime = 0;

                // reset wave view
                waveView.setPressure(minLevel);
                waveView.invalidate();
            }
        });
    }

    public static void updatePressure(double level) {
        waveView.setPressure(level);
        waveView.invalidate();
    }

    private class ConvertSongTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(slidingTabActivity, getString(R.string.converting),
                    getString(R.string.please_wait), true);
        }

        @Override
        protected Boolean doInBackground(String... songs) {
            SongConverter songConverter = new SongConverter(slidingTabActivity);

            Log.e(LOG_TAG, songs[0]);
            if (!songConverter.setUp(songs[0]))
                return false;

            songConverter.convert();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                SlidingTabAdapter adapter = slidingTabActivity.getAdapter();
                ScoreFileListPage scoreFileListPage = (ScoreFileListPage)adapter.getPage(SlidingTabAdapter.SCORE_FILE_LIST);
                scoreFileListPage.refreshList();
                slidingTabActivity.getViewPager().setCurrentItem(SlidingTabAdapter.SCORE_FILE_LIST, true);

                Toast.makeText(slidingTabActivity, slidingTabActivity.getString(R.string.success), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(slidingTabActivity, slidingTabActivity.getString(R.string.failed), Toast.LENGTH_SHORT).show();
            }

            progress.dismiss();
        }
    }
}
