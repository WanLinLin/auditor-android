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

    private final String notesToIgnore = "Garbage";

    private AudioDispatcher dispatcher;

    private PitchDetectionHandler pdh;

    private AudioProcessor ap;

    private int curSearchPos = 0;

    // number of float samples to ignore
    private static final int ignore = 5;

    private int count = 0;

    private ArrayList<Pair<String, Float>> noteAndTime = new ArrayList<>();

    private ArrayList<Pair<String, Float>> noteList = new ArrayList<>();

    SongConverter(UniversalAudioInputStream universalAudioInputStream) {
        // set pitch detect things
        dispatcher = new AudioDispatcher(
                universalAudioInputStream,
                AudioRecordActivity.bufferSize,
                AudioRecordActivity.bufferSize / 2);

        pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pdr, AudioEvent ae){
                float pitch = pdr.getPitch();
                float sampleTime = ae.getConvertTime();
                String note = pitchToNotes(pitch);

                Pair<String, Float> pitchAndTime = new Pair<>(note, sampleTime);

//                Log.i(LOG_TAG, "pitch - " + count + ": " + pitch);
                Log.i(LOG_TAG, "note  - " + count + ": " + note);
                count++;

                noteAndTime.add(pitchAndTime);
            }
        };

         ap = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                universalAudioInputStream.getFormat().getSampleRate(),
                AudioRecordActivity.bufferSize, pdh);

        dispatcher.addAudioProcessor(ap);
    }

    public boolean convert() {
        Thread pitchDetectThread = new Thread(dispatcher, "Audio Dispatcher");
        boolean havingGarbage = false;
        float garbageTime = 0;

        pitchDetectThread.start();
        try {
            pitchDetectThread.join();
        }
        catch (InterruptedException e) {
            Log.e(LOG_TAG, "Pitch detect thread join failed!");
            return false;
        }

        Log.e(LOG_TAG, "count: " + count);
        Log.e(LOG_TAG, "nodeAndTime: " + noteAndTime.size());

        while(curSearchPos < noteAndTime.size()) {
            Pair<String, Float> firstNoteResult = getConsecutiveNotes(curSearchPos);
            Log.e(LOG_TAG, "curSearchPos: " + curSearchPos);

            if(!firstNoteResult.first.equals(notesToIgnore) && curSearchPos < noteAndTime.size()) {
                if(havingGarbage) {
                    Pair<String, Float> secondNoteResult = getConsecutiveNotes(curSearchPos);
                    Log.e(LOG_TAG, "curSearchPos: " + curSearchPos);

                    if(secondNoteResult.first.equals(firstNoteResult.first)){ // concat
                        Pair<String, Float> noteAddingToNoteList = new Pair<>(firstNoteResult.first, firstNoteResult.second + garbageTime + secondNoteResult.second);
                        noteList.set(noteList.size() - 1, noteAddingToNoteList);
                    }
                    else {
                        Pair<String, Float> noteAddingToNoteList = new Pair<>(firstNoteResult.first, firstNoteResult.second + garbageTime);
                        noteList.set(noteList.size() - 1, noteAddingToNoteList);
                        noteList.add(secondNoteResult);
                    }

                    garbageTime = 0;
                    havingGarbage = false;
                }
                else {
                    noteList.add(firstNoteResult);
                }
            }
            else { // is garbage
                havingGarbage = true;
                garbageTime = firstNoteResult.second;
            }
        }

        for(Pair p: noteList) {
            Log.e(LOG_TAG, "Note: " + p.first + ", Time: " + p.second);
        }

        Log.i(LOG_TAG, "Pitch detect successfully!");
        curSearchPos = 0;
        noteAndTime.clear();
        return true;
    }

    private String pitchToNotes(float pitch) {
        return NotesFrequency.getNote(pitch);
    }

    private Pair<String, Float> getConsecutiveNotes(int startPos) {
        int readCount = 0;
        float noteDuration = 0;
        Pair startNoteAndTime = noteAndTime.get(startPos);
        noteDuration += (Float)startNoteAndTime.second;

        for(int i = startPos + 1; i < noteAndTime.size(); i++) {
            Pair next = noteAndTime.get(i);
            readCount++;
            if (next.first == startNoteAndTime.first) {
                noteDuration += (Float)next.second;
            }
            else {
                break;
            }
        }

        curSearchPos += readCount + 1;

        if(readCount > ignore) { // long enough to be a note piece
            return new Pair<>(startNoteAndTime.first.toString(), noteDuration);
        }
        else {
            return new Pair<>(notesToIgnore, noteDuration);
        }
    }
}

// TODO 偵測音準會出現基頻的泛音, 想辦法將泛音改成基頻：限制時間內出現泛音取鄰近基頻取代，否則就有可能是唱出的高低音
// TODO design the map or array list to store the data of notes
// TODO 依時間軸把沒被分開的convert sample連接成一個element，並過濾掉泛音轉換成音符，且記錄時間長度

// TODO modify the calculation