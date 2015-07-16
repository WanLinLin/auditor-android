package com.example.auditor;

import android.util.Log;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

/**
 * Created by Wan Lin on 15/7/15.
 * This class is going to handle music file stream pitch and notes conversion.
 */
public class SongConverter{
    private static final String LOG_TAG = "SongConverter";
    // Stream is the song to be convert
    private UniversalAudioInputStream stream;

    private AudioDispatcher dispatcher;

    private PitchDetectionHandler pdh;

    private AudioProcessor ap;

    SongConverter(final UniversalAudioInputStream universalAudioInputStream){
        // set pitch detect things
        dispatcher = new AudioDispatcher(
                universalAudioInputStream,
                AudioRecordActivity.bufferSize,
                AudioRecordActivity.bufferSize / 2);

        pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pdr,AudioEvent ae){
                float pitch = pdr.getPitch();

                Log.i(LOG_TAG, "pitch: " + pitch);
            }
        };

         ap = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                universalAudioInputStream.getFormat().getSampleRate(),
                AudioRecordActivity.bufferSize, pdh);

        dispatcher.addAudioProcessor(ap);
    }

    public boolean convert(){
        Thread pitchDetectThread = new Thread(dispatcher, "Audio Dispatcher");

        long startTime = System.currentTimeMillis();
        Log.i(LOG_TAG, "start time: " + startTime);
        pitchDetectThread.start();

        try {
            pitchDetectThread.join();

            long endTime = System.currentTimeMillis();
            Log.i(LOG_TAG, "end time: " + endTime);
            long whileLoopTime =  ((endTime - startTime) / dispatcher.getWhileLoopCounter());
            Log.i(LOG_TAG, "a time in a loop: " + whileLoopTime);
        }
        catch (InterruptedException e) {
            Log.e(LOG_TAG, "Pitch detect thread join failed!");
            return false;
        }

        Log.i(LOG_TAG, "Pitch detect successfully!");
        return true;
    }
}