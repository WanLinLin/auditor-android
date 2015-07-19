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

    private int count = 0;

    SongConverter(final UniversalAudioInputStream universalAudioInputStream){
        // set pitch detect things
        dispatcher = new AudioDispatcher(
                universalAudioInputStream,
                AudioRecordActivity.bufferSize,
                AudioRecordActivity.bufferSize / 2);

        pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pdr, AudioEvent ae){
                float pitch = pdr.getPitch();

                Log.i(LOG_TAG, "pitch - " + (count++) + ": " + pitch);

                // TODO log out from pitch frequency to notes
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

        long convertStartTime = System.currentTimeMillis();
        long convertEndTime;
        Log.i(LOG_TAG, "start time: " + convertStartTime);
        pitchDetectThread.start();

        try {
            pitchDetectThread.join();

            convertEndTime = System.currentTimeMillis();
            Log.i(LOG_TAG, "end time: " + convertEndTime);
        }
        catch (InterruptedException e) {
            Log.e(LOG_TAG, "Pitch detect thread join failed!");
            return false;
        }

        Log.i(LOG_TAG, "Pitch detect successfully!");

        Log.e(LOG_TAG, "average convert period: " + (convertEndTime - convertStartTime) / count );

        count = 0;

        return true;
    }

/*    private String pitchToNotes(float pitch){
        Enum
    }*/
}

// TODO 偵測音準會出現基頻的泛音, 想辦法將泛音改成基頻，限制時間內出現泛音取鄰近基頻取代，否則就有可能是唱出的高低音
// TODO design the map or array list to store the data of notes