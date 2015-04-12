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
import java.io.IOException;


public class AudioRecordActivity extends ActionBarActivity {

    private static final String LOG_TAG = "AudioRecordActivity";

    private static String mFileName = null;

    final Context context = this;

    private ExtAudioRecorder extAudioRecorder = null;
    private MediaPlayer mPlayer = null;

    private void onRecord(boolean start){
        if(start)
            startRecording();
        else
            stopRecording();
    }

    private void onPlay(boolean start){
        if(start)
            startPlaying();
        else
            stopPlaying();
    }

    private void startPlaying(){
        mPlayer = new MediaPlayer();
        try{
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        }
        catch(IOException e){
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying(){
        mPlayer.release();
        mPlayer = null;
    }

    //not using mediaRecorder but ExAudioRecorder
    private void startRecording(){
        extAudioRecorder = ExtAudioRecorder.getInstanse(true);
        extAudioRecorder.setOutputFile(mFileName);

        try{
            extAudioRecorder.prepare();
        }
        catch (IOException e){
            Log.e(LOG_TAG, "prepare() failed");
        }
        extAudioRecorder.start();
    }

    private void stopRecording(){
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
                            public void onClick(DialogInterface dialog,int id) {
                                File from = new File(mFileName);
                                File to = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/" + userInput.getText() + ".3gp");
                                from.renameTo(to);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    class RecordButton extends Button {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRecord(mStartRecording);
                if(mStartRecording)
                    setText("Stop recording");
                else
                    setText("Start recording");

                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx){
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    class PlayButton extends Button{
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if(mStartPlaying)
                    setText("Stop playing");
                else
                    setText("Start playing");

                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx){
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    public AudioRecordActivity(){
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");
        dir.mkdirs();

        mFileName = dir.getAbsolutePath() + "tmp.3gp";
    }

    public void goToAudioFile(View view){
        // Do something in response to button
        Intent intent = new Intent(this, AudioFileActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.audioRecordRL);
        RecordButton recordButton = new RecordButton(this);

        RelativeLayout.LayoutParams btLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        btLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        btLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);


        rl.addView(recordButton, btLayoutParams);



//        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//
//        ll.addView(mRecordButton,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));

//        setContentView(ll, llParams);
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
}
