package com.example.auditor.convert;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.example.auditor.AudioRecordActivity;
import com.example.auditor.song.ExtAudioRecorder;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

/**
 * Created by Wan Lin on 15/7/15.
 * Handle music stream pitch and notes conversion.
 */
public class SongConverter {
    private static final String LOG_TAG = SongConverter.class.getName();
    private static final String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";

    private static final float tooShortForHumanToSing = 0.03f; // can be ignore
    private static final int secondsPerMinute = 60; // seconds per minutes

    private int beatsPerMinute = 90; // bits per minute, speed
    private int beatsPerMeasure = 4; // 4 beats per bar
    private int beatUnit = 4; // quarter notes per bit
    private int measureDuration = secondsPerMinute / beatsPerMinute * beatsPerMeasure;

    private AudioDispatcher dispatcher;
    private String songTitle;

    // Pair<String, Float> stores the String: note name, and Float: time duration of this note
    private ArrayList<Pair<String, Float>> noteAndTimeList; // first processed pitch result
    private ArrayList<Pair<String, Float>> noteAndTimeResultList; // second processed pitch result (handle vibration and overtone)
    private ArrayList<NoteResult> noteResults;

    private Context context;

    public SongConverter(Context context) {
        noteAndTimeList = new ArrayList<>();
        noteAndTimeResultList = new ArrayList<>();
        noteResults = new ArrayList<>();

        this.context = context;
    }

    /**
     * Set up audio format for pitch detection and pitch detection event.
     * @param songTitle audio file name witch is going to convert
     * @return return false if failed to open the audio file
     */
    public boolean setUp(String songTitle) {
        this.songTitle = songTitle;

        File file = new File(auditorDir + "wav/" + songTitle + ".wav");
        InputStream inputStream;

        // open a audio file
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Failed to open a file!");
            return false;
        }

        // set TarsosDSP audio format
        ExtAudioRecorder extAudioRecorder = ExtAudioRecorder.getInstanse(ExtAudioRecorder.RECORDING_UNCOMPRESSED);
        TarsosDSPAudioFormat tarsosDSPAudioFormat =
                new TarsosDSPAudioFormat(
                        extAudioRecorder.getSampleRate(),
                        extAudioRecorder.getBitSamples(),
                        extAudioRecorder.getChannels(),
                        false,  // indicates whether the data is signed or unsigned
                        false); // indicates whether the data for a single sample

        // set audio stream
        UniversalAudioInputStream universalAudioInputStream =
                new UniversalAudioInputStream(inputStream, tarsosDSPAudioFormat);

        // set audio dispatcher
        dispatcher = new AudioDispatcher(
                universalAudioInputStream,
                AudioRecordActivity.bufferSize,
                AudioRecordActivity.bufferSize / 2);

        // set pitch detection handler, concat consecutive same note and time
        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pdr, AudioEvent ae){
                String notation = pitchToNotation(pdr.getPitch());
                Float sampleTime = ae.getConvertTime();

                Log.e(LOG_TAG, "notation: " + notation + ", time: " + sampleTime);

                // handle the first pitch
                if(noteAndTimeList.isEmpty()) {
                    Pair<String, Float> p = new Pair<>(notation, sampleTime);
                    noteAndTimeList.add(p);
                    return;
                }

                Pair last = noteAndTimeList.get(noteAndTimeList.size() - 1);

                if(notation == last.first) {
                    Pair<String, Float> p = new Pair<>(notation, sampleTime + (Float)last.second);
                    noteAndTimeList.set(noteAndTimeList.size() -1, p);
                }
                else if (notation == null) { // I don't know why get null
                    Pair<String, Float> p = new Pair<>("Pause", sampleTime);
                    noteAndTimeList.add(p);
                }
                else {
                    Pair<String, Float> p = new Pair<>(notation, sampleTime);
                    noteAndTimeList.add(p);
                }
            }
        };

        // set audio processor
        AudioProcessor ap = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                universalAudioInputStream.getFormat().getSampleRate(),
                AudioRecordActivity.bufferSize, pdh);

        // add audio processor into audio dispatcher
        dispatcher.addAudioProcessor(ap);

        return true;
    }

    public void convert() {
        String musicString;
        Pattern pattern;

        // convert byte file into float pitches, and store note names and time duration into
        dispatcher.run();

//        for(Pair<String, Float> p : noteAndTimeList)
//                Log.i(LOG_TAG, "notation: " + p.first + ", duration: " + p.second);

        // concat notes, handle vibration and overtone
        processNoteAndTimeList();

        // cut time to note duration
        for(Pair<String, Float> p: noteAndTimeResultList) {
            NoteResult noteResult = timeToNotes(p.first, p.second);
            if(noteResult != null)
                noteResults.add(noteResult);
        }

//        for(Pair<String, Float> p : noteAndTimeResultList)
//            Log.e(getClass().getName(), "notation: " + p.first + ", duration: " + p.second);

        deleteBeginAndEndRests();
        musicString = convertToMusicString();

        pattern = new Pattern(musicString);
        try { pattern.save(new File(auditorDir + "txt/" + songTitle + ".txt")); }
        catch (IOException e) {
            Log.e(LOG_TAG, "IOE");
            Toast.makeText(context, "檔案不存在", Toast.LENGTH_SHORT).show();
        }

        try { MidiFileManager.savePatternToMidi(pattern, new File(auditorDir + "midi/" + songTitle + ".mid")); }
        catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            Toast.makeText(context, "儲存 MIDI 失敗", Toast.LENGTH_SHORT).show();
        }

        Log.d(getClass().getName(), musicString);
        Log.i(getClass().getName(), "Pitch detect successfully!");
    }

    /**
     * Convert second processed noteAndTimeResultList to Jfugue music string.
     * @return Jfugue music string
     */
    private String convertToMusicString() {
        String keySignature = "KCmaj ";
        String tempo = "T" + beatsPerMinute + " ";
        String musicString = keySignature + tempo;

        for(NoteResult n: noteResults) {
            ArrayList<Pair<String, Integer>> noteTimArr = n.getNoteTimeArr();
            String jfugueNotation = n.getNoteName();

            if(n.getNoteName().length() > 2) {
                jfugueNotation = n.getNoteName().substring(0, 3);

                if(jfugueNotation.equals("Pau")) { // "Pau"use -> "R"est
                    jfugueNotation = "R";
                }
                else { // sharp or flat
                    if(jfugueNotation.endsWith("s")) {
                        jfugueNotation = jfugueNotation.substring(0, 1) + "#" + jfugueNotation.substring(1, 2);
                    }
                    else if(jfugueNotation.endsWith("f")) {
                        jfugueNotation = jfugueNotation.substring(0, 1) + "b" + jfugueNotation.substring(1, 2);
                    }
                }
            }

            for(int i = 0; i < noteTimArr.size(); i++) {
                musicString += jfugueNotation;
                Pair p = noteTimArr.get(i);

                if (i != 0 && !jfugueNotation.equals("R"))
                    musicString += "-";

                switch(p.first.toString()) {
                    case "wholeNote":
                        musicString += "w";
                        break;
                    case "halfNote":
                        musicString += "h";
                        break;
                    case "quarterNote":
                        musicString += "q";
                        break;
                    case "eighthNote":
                        musicString += "i";
                        break;
                    case "sixteenthNote":
                        musicString += "s";
                        break;
                    case "thirtySecondNote":
                        musicString += "t";
                        break;
                    case "sixtyFourthNote":
                        musicString += "x";
                        break;
                }

                if (i != noteTimArr.size() -1 && !jfugueNotation.equals("R"))
                    musicString += "-";

                musicString += " ";
            }
            musicString += "| ";
        }

        return musicString;
    }

    /**
     * Process noteAndTimeList, handle human sound vibration and overtone.
     */
    private void processNoteAndTimeList() {
        // concat notes, handle vibration and overtone
        for(int i = 0; i < noteAndTimeList.size(); i++) {
            Pair<String, Float> p = noteAndTimeList.get(i);

            // long enough
            if(p.second > tooShortForHumanToSing) {
                if(!noteAndTimeResultList.isEmpty()) { // is not empty
                    Pair<String, Float> last = noteAndTimeResultList.get(noteAndTimeResultList.size() -1);

                    if(p.first.equals(last.first)) { // same note, but split by a garbage, so concat
                        noteAndTimeResultList.set(noteAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                    }
                    else { // pause or a different notation and time, so add
                        noteAndTimeResultList.add(p);
                    }
                }
                else { // handle the first notation and time
                    noteAndTimeResultList.add(p);
                }
            }
            else { // too short for human to sing
                if(p.first.equals(NotesFrequency.Pause.name())) { // garbage pause
                    if(!noteAndTimeResultList.isEmpty()) { // is not empty
                        Pair<String, Float> last = noteAndTimeResultList.get(noteAndTimeResultList.size() -1);

                        // add the garbage pause time to the last notation and time result
                        noteAndTimeResultList.set(noteAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                    }
                }
                else { // garbage note
                    /* handle vibration, overtone */

                    // case 1:
                    // e.g: notation: F4sAndG4f, time: 0.1044898
                    //      notation: G4,        time: 0.04643991
                    if(!noteAndTimeResultList.isEmpty()) { // is not empty
                        Pair<String, Float> last = noteAndTimeResultList.get(noteAndTimeResultList.size() -1);
                        try {
                            if (last.first.contains(p.first.substring(0, 1)) || last.first.contains(p.first.substring(6, 7))) {
                                // add the similar note time to the last nation and time result
                                noteAndTimeResultList.set(noteAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                                continue;
                            }

                            if (last.first.contains("B") && p.first.contains("C")) {
                                noteAndTimeResultList.set(noteAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                                continue;
                            }
                            else if (last.first.contains("C") && p.first.contains("B")) {
                                noteAndTimeResultList.set(noteAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                                continue;
                            }
                            else if (last.first.contains("E") && p.first.contains("F")) {
                                noteAndTimeResultList.set(noteAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                                continue;
                            }
                            else if (last.first.contains("F") && p.first.contains("E")) {
                                noteAndTimeResultList.set(noteAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                                continue;
                            }
                        }
                        catch (IndexOutOfBoundsException e) {
                            // last notation string is shorter than 6
                        }
                    }

                    // case 2:
                    // e.g: notation: D4sAndE4f, time: 0.023219954
                    //      notation: D4,        time: 0.058049887
                    if(i + 1 < noteAndTimeList.size()) { // prevent out of index
                        Pair<String, Float> next = noteAndTimeList.get(i + 1);
                        try {
                            if (next.first.contains(p.first.substring(0, 1)) || next.first.contains(p.first.substring(6, 7))) {
                                // check the next
                                noteAndTimeList.set(i + 1, new Pair<>(next.second > p.second ? next.first : p.first, next.second + p.second));
                            }
                        }
                        catch (IndexOutOfBoundsException e) {
                            // last notation string is shorter than 6
                        }
                    }
                }
            }
        }

    }

    /**
     * Convert float pitch in Hertz into notation name.
     * @param pitch float pitch in Hertz
     * @return notation name
     */
    private String pitchToNotation(float pitch) {
        return NotesFrequency.getNote(pitch);
    }

    /**
     * Convert a note and duration time into a NoteResult witch contains notation name and note duration array.
     * @param noteName notation name
     * @param time how long this note last
     * @return a single NoteResult which contains notation name and note duration array
     */
    private NoteResult timeToNotes(String noteName, float time) {
        ArrayList<Pair<String, Integer>> timeResults = new ArrayList<>();
        NoteDurations[] noteDurations = NoteDurations.values();
        float quarterNoteDuration = (float)secondsPerMinute / (float)beatsPerMinute;
        int quotient;
        float remainder = time;

        for(NoteDurations n: noteDurations) {
            quotient = (int)(remainder / (quarterNoteDuration * n.getTime()));
            remainder = remainder % (quarterNoteDuration * n.getTime());

            if(quotient != 0)
                timeResults.add(new Pair<>(n.toString(), quotient));
        }

        if(!timeResults.isEmpty()) // is at least sixty-forth note
            return new NoteResult(noteName, timeResults);
        else // note duration too short to divide by the shortest note duration
            return null;
    }

    /**
     * Delete Rests at the beginning and at the end of NoteResult array.
     */
    private void deleteBeginAndEndRests() {
        // delete pause at the beginning
        while(true) {
            if (noteResults.get(0).getNoteName().equals("Pause"))
                noteResults.remove(0);
            else
                break;
        }

        // delete pause at the end
        while(true) {
            if (noteResults.get(noteResults.size() - 1).getNoteName().equals("Pause"))
                noteResults.remove(noteResults.size() - 1);
            else
                break;
        }
    }

    /**
     * Calculate the most shown note duration to be the quarter note duration.
     * @return the most shown note duration
     */
    private float getMostFrequentlyOccurringNoteDuration() {
        ArrayList<Pair<Float, Integer>> timeList = new ArrayList<>();
        float error = 0.05f;
        Pair<Float, Integer> maxDuration = null;

        for(Pair<String, Float> p : noteAndTimeResultList) {
            if(!p.first.equals("Pause")) {
                if(timeList.isEmpty())
                    timeList.add(new Pair<>(p.second, 1));
                else {
                    boolean processed = false;

                    // search list
                    for (int i = 0; i < timeList.size(); i++) {
                        Pair<Float, Integer> t = timeList.get(i);
                        if (Math.abs(p.second - t.first) < error) {
                            timeList.set(i, new Pair<>(t.first, t.second + 1));
                            processed = true;
                            break;
                        }
                    }

                    if(!processed)
                        timeList.add(new Pair<>(p.second, 1));
                }
            }
        }

        for(Pair<Float, Integer> p : timeList) {
            if (maxDuration == null)
                maxDuration = new Pair<>(p.first, p.second);
            else {
                if (p.second > maxDuration.second)
                    maxDuration = new Pair<>(p.first, p.second);
            }
        }

        return maxDuration.first;
    }
}

// TODO float add err make it lose precision, using BigDecimal to avoid this (Maybe we can ignore this issue)

// TODO fixed jfugue for android fucking save midi problem