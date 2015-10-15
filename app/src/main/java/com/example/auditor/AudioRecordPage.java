package com.example.auditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.auditor.song.ExtAudioRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Wan Lin on 2015/10/8.
 * AudioRecordPage
 */
public class AudioRecordPage extends Fragment{
    private static final String LOG_TAG = "AudioRecordActivity";
    private static final String wavDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/wav/";
    private static String mFileName = null;
    private ExtAudioRecorder extAudioRecorder = null;
    private SlidingTabActivity slidingTabActivity;
    public static final int bufferSize = 1024;
    private EditText userInput;
    private boolean setFileName;

    public AudioRecordPage() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFileName = wavDir + "tmp.wav";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout)inflater.inflate(R.layout.audio_record_page, container, false);
        Bundle args = getArguments();

        RecordButton recordButton = new RecordButton(slidingTabActivity);
        // setting basic recordButton parameters
        RelativeLayout.LayoutParams btLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        // set recordButton to be the center of the screen
        btLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        btLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        // add recordButton into the audioRecordRL
        rootView.addView(recordButton, btLayoutParams);
        /* ----LAYOUT SETTING---- */

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        slidingTabActivity = (SlidingTabActivity)activity;
    }

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

        try { extAudioRecorder.prepare(); }
        catch (IOException e) { Log.e(LOG_TAG, ".prepare() failed"); }
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
        LayoutInflater li = LayoutInflater.from(slidingTabActivity);
        View promptsView = li.inflate(R.layout.audio_record_popup_rename, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(slidingTabActivity);

        // set audio_record_popup_window.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File from = new File(mFileName);
                                File to = new File(wavDir +
                                        userInput.getText() + ".wav");

                                if(from.renameTo(to)) {
                                    Toast.makeText(slidingTabActivity,
                                            getString(R.string.save_successfully),
                                            Toast.LENGTH_SHORT).show();

                                    SlidingTabAdapter adapter = slidingTabActivity.getAdapter();
                                    AudioFileListPage page = (AudioFileListPage)adapter.getPage(SlidingTabAdapter.AUDIO_FILE_LIST);
                                    page.refreshList();
                                    slidingTabActivity.getViewPager().setCurrentItem(SlidingTabAdapter.AUDIO_FILE_LIST, true);
                                }
                                else {
                                    Toast.makeText(slidingTabActivity,
                                            getResources().getString(R.string.save_failed),
                                            Toast.LENGTH_SHORT).show();
                                }
                                setFileName = true;
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                File tmpFile = new File(mFileName);
                                tmpFile.delete();
                                setFileName = false;
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
