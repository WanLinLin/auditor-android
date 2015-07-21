package com.example.auditor;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
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

    private AudioDispatcher dispatcher;

    private PitchDetectionHandler pdh;

    private AudioProcessor ap;

    private int count = 0;

    private ArrayList<Pair<Float, Float>> pitchBuffer = new ArrayList<>();

    SongConverter(UniversalAudioInputStream universalAudioInputStream){
        // set pitch detect things
        dispatcher = new AudioDispatcher(
                universalAudioInputStream,
                AudioRecordActivity.bufferSize,
                AudioRecordActivity.bufferSize / 2);

        pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pdr, AudioEvent ae){
                float pitch = pdr.getPitch();

                Pair<Float, Float> pitchAndTime = new Pair<>(pitch, ae.getConvertTime());

//                Log.i(LOG_TAG, "pitch - " + count + ": " + pitch);
                Log.i(LOG_TAG, "note  - " + count + ": " + pitchToNotes(pitch));
                count++;

                pitchBuffer.add(pitchAndTime);
            }
        };

         ap = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                universalAudioInputStream.getFormat().getSampleRate(),
                AudioRecordActivity.bufferSize, pdh);

        dispatcher.addAudioProcessor(ap);
    }

    public boolean convert(){
        float songTotalTime = 0;
        Thread pitchDetectThread = new Thread(dispatcher, "Audio Dispatcher");

        pitchDetectThread.start();

        try {
            pitchDetectThread.join();
        }
        catch (InterruptedException e) {
            Log.e(LOG_TAG, "Pitch detect thread join failed!");
            return false;
        }

        for (Pair p: pitchBuffer) {
            songTotalTime += (Float)p.second;
        }

        Log.i(LOG_TAG, "song total time: " + songTotalTime);
        Log.i(LOG_TAG, "Pitch detect successfully!");
        pitchBuffer.clear();
        count = 0;
        return true;
    }

    private String pitchToNotes(float pitch){
        return NotesFrequency.getNote(pitch);
    }
}

// TODO 偵測音準會出現基頻的泛音, 想辦法將泛音改成基頻：限制時間內出現泛音取鄰近基頻取代，否則就有可能是唱出的高低音
// TODO design the map or array list to store the data of notes
// TODO 依時間軸把沒被分開的convert sample連接成一個element，並過濾掉泛音轉換成音符，且記錄時間長度