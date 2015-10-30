package com.example.auditor.convert;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.example.auditor.AudioRecordPage;
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
    private static final String LOG_TAG = "SongConverter";
    private static final String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";

    public static float tooShortToSing = 0.1f; // can be ignore
    private static final float tooShortPauseTime = 0.04f;
    private static final int secondsPerMinute = 60; // seconds per minutes
    private static final float minFreqDifBetweenSamples = 0.97f;

    private int beatsPerMinute = 80; // bits per minute, speed
    private int beatsPerMeasure = 4; // 4 beats per bar
    private int beatUnit = 4; // quarter notes per bit
    private int measureDuration = secondsPerMinute / beatsPerMinute * beatsPerMeasure;

    private AudioDispatcher dispatcher;
    private String songTitle;
    private int curNoteAndTimeListPtr = 0;

    // after concatenate
    private ArrayList<Pair<String, Float>> noteAndTimeList;

    // after calculate, second processed pitch result (handle vibration and overtone)
    private ArrayList<Pair<String, Float>> noteAndTimeResultList;

    // result
    private ArrayList<NoteResult> noteResults;

    private Context context;

    public SongConverter(Context context) {
        noteAndTimeList = new ArrayList<>();
        noteAndTimeResultList = new ArrayList<>();
        noteResults = new ArrayList<>();
        float shortestNoteDuration = (float)secondsPerMinute / (float)beatsPerMinute * NoteDurations.sixtyFourthNote.time;

        if(shortestNoteDuration > tooShortToSing)
            tooShortToSing = shortestNoteDuration;
        Log.e(LOG_TAG, "too short to sing: " + tooShortToSing);

        this.context = context;
    }

    /**
     * Set up audio format for pitch detection and pitch detection event.
     * @param songTitle audio file name witch is going to convert
     * @return return false if failed to open the audio file
     */
    public boolean setUp(String songTitle) {
        this.songTitle = songTitle;

        File file = new File(auditorDir + "wav/" + songTitle);
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
                AudioRecordPage.bufferSize,
                AudioRecordPage.bufferSize / 2); // overlap, usually half of buffer size or 2/3

        // set pitch detection handler, concat consecutive same note and time
        final PitchDetectionHandler pdh = new mPitchDetectionHandler();

        // set audio processor
        AudioProcessor ap = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                universalAudioInputStream.getFormat().getSampleRate(),
                AudioRecordPage.bufferSize, pdh);

        // add audio processor into audio dispatcher
        dispatcher.addAudioProcessor(ap);

        return true;
    }

    /**
     * all conversion starts here
     */
    public void convert()  {
        String musicString;
        Pattern pattern;

        // convert byte file into float pitches, and store note names and time duration into
        // noteAndTimeList, delete short pauses
        Log.w(LOG_TAG, "========== GET PITCH, DELETE SHORT PAUSE, CONCAT LONG PAUSE  ==========");
        dispatcher.run();

        Log.d(LOG_TAG, "========== CONCAT NOTE TIME ==========");
        for(Pair<String, Float> p : noteAndTimeList) {
            if(p.first.equals(NotesFrequency.Pause.getNote()))
                Log.e(LOG_TAG, String.format("time: %9f, note: %s", p.second, p.first));
            else
                Log.d(LOG_TAG, String.format("time: %9f, note: %s", p.second, p.first));
        }

        // concat notes, handle vibration and overtone
//        processNoteAndTimeList();
        Log.e(LOG_TAG, "========== CALCULATE EACH DOMINATE NOTE ==========");
        process();

        Log.i(LOG_TAG, "========== FINAL NOTE AND TIME RESULT ==========");
        for(Pair<String, Float> p : noteAndTimeResultList) {
            Log.i(LOG_TAG, String.format("time: %9f, note: %s", p.second, p.first));
        }

        // cut time to note duration
        for(Pair<String, Float> p: noteAndTimeResultList) {
            NoteResult noteResult = new NoteResult(p.first, timeDurationToNoteDuration(p.second));
            if(noteResult.getNoteTimeArr() != null)
                noteResults.add(noteResult);
        }

        deleteBeginAndEndRests();
        musicString = convertToMusicString();

        pattern = new Pattern(musicString);
        try { pattern.save(new File(auditorDir + "txt/" + songTitle.substring(0, songTitle.length() - 4) + ".txt")); }
        catch (IOException e) {
            Log.e(LOG_TAG, "IOE");
            Toast.makeText(context, "檔案不存在", Toast.LENGTH_SHORT).show();
        }

        try { MidiFileManager.savePatternToMidi(pattern, new File(auditorDir + "midi/" + songTitle.substring(0, songTitle.length() - 4) + ".mid")); }
        catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
            Toast.makeText(context, "儲存 MIDI 失敗", Toast.LENGTH_SHORT).show();
        }

        Log.d(getClass().getName(), musicString);
        Log.i(getClass().getName(), "Pitch detect successfully!");
    }

    private void process() {
        ArrayList<Pair<String, Float>> noteAndTimeBuffer = new ArrayList<>();

        process:
        for(int i = 0; i < noteAndTimeList.size(); i++) {
            Pair<String, Float> curNoteAndTime = noteAndTimeList.get(i);
            curNoteAndTimeListPtr = i;

            // handle the first notation and time
            if(noteAndTimeResultList.isEmpty()) {
                // first note is pause
                if(curNoteAndTime.first.equals(NotesFrequency.Pause.getNote())) {
                    noteAndTimeResultList.add(curNoteAndTime);
                    continue;
                }
            }

            // is note
            if(!curNoteAndTime.first.equals(NotesFrequency.Pause.getNote())) {

                // long enough to sing
                if(curNoteAndTime.second > tooShortToSing) {
                    if(!noteAndTimeBuffer.isEmpty())  // if the buffer has note
                        addTheDominateNote(noteAndTimeBuffer);

                    // get last note
                    int lastIndex = noteAndTimeResultList.size() > 0 ? noteAndTimeResultList.size() - 1 : 0;
                    Pair<String, Float> last = noteAndTimeResultList.get(lastIndex);

                    // the same with the last note, concat directly
                    if(curNoteAndTime.first.equals(last.first)) {
                        concatToPrevNote(curNoteAndTime.second);
                    }
                    else {
                        noteAndTimeResultList.add(curNoteAndTime); // TODO should concat
                    }
                }
                else {
                    // search noteAndTimeBuffer
                    for (int n = 0; n < noteAndTimeBuffer.size(); n++) {
                        Pair<String, Float> curNATBPair = noteAndTimeBuffer.get(n);

                        if (curNATBPair.first.equals(curNoteAndTime.first)) {
                            noteAndTimeBuffer.set(n, new Pair<>(curNATBPair.first, curNATBPair.second + curNoteAndTime.second));
                            continue process;
                        }
                    }
                    noteAndTimeBuffer.add(curNoteAndTime);
                }
            }
            // is pause
            else {
                addTheDominateNote(noteAndTimeBuffer);
                noteAndTimeResultList.add(curNoteAndTime); // add the pause
            }
        }

        // if the last note in noteAndTimeList is a note
        if (!noteAndTimeList.get(noteAndTimeList.size() -1).first.equals(NotesFrequency.Pause.getNote())) {
            addTheDominateNote(noteAndTimeBuffer);
        }
        curNoteAndTimeListPtr = 0;
    }

    private void addTheDominateNote(ArrayList<Pair<String, Float>> noteAndTimeBuffer) {
        float timeDurationSum = 0f;
        Pair<String, Float> dominatePair = null;

        for(int j = 0; j < noteAndTimeBuffer.size(); j++) {
            Pair<String, Float> cur = noteAndTimeBuffer.get(j);
            timeDurationSum += cur.second;

            Log.e(LOG_TAG, String.format("time: %9f, note: %s", cur.second, cur.first));
            if(dominatePair == null) { // process first noteAndTime
                dominatePair = cur;
                continue;
            }

            if(cur.second > dominatePair.second) dominatePair = cur;
        }

        if(dominatePair != null) { // has buffer
            int lastIndex = noteAndTimeResultList.size() > 0 ? noteAndTimeResultList.size() - 1 : 0;
            Pair<String, Float> last = noteAndTimeResultList.get(lastIndex);

            // the same note, concat directly
            if(dominatePair.first.equals(last.first)) {
                concatToPrevNote(timeDurationSum);
            }

            // different note, long enough to sing
            if(timeDurationSum > tooShortToSing) {
                noteAndTimeResultList.add(new Pair<>(dominatePair.first, timeDurationSum));
                Log.e(LOG_TAG, String.format("add ---------------> total time: %9f, note: %s", timeDurationSum, dominatePair.first));
            }
            // different note, but too short to sing
            else {
                if(last.first.equals(NotesFrequency.Pause.getNote()))
                    concatToNextNote(timeDurationSum);
                else
                    concatToPrevNote(timeDurationSum);
            }
            noteAndTimeBuffer.clear();
        }
    }

    private void concatToPrevNote(float noteTimeDuration) {
        int lastIndex = noteAndTimeResultList.size() > 0 ? noteAndTimeResultList.size() - 1 : 0;
        Pair<String, Float> last = noteAndTimeResultList.get(lastIndex);

        noteAndTimeResultList.set(lastIndex, new Pair<>(last.first, last.second + noteTimeDuration));
        Log.e(LOG_TAG, String.format("reset last --------> total time: %9f, note: %s", last.second + noteTimeDuration, last.first));
    }

    private void concatToNextNote(float noteTimeDuration) {
        int nextIndex = curNoteAndTimeListPtr;
        if(nextIndex >= noteAndTimeList.size()) return;
        Pair<String, Float> next = noteAndTimeList.get(nextIndex);

        noteAndTimeList.set(nextIndex, new Pair<>(next.first, next.second + noteTimeDuration));
        Log.e(LOG_TAG, String.format("reset next --------> total time: %9f, note: %s", next.second + noteTimeDuration, next.first));
    }

    /**
     * Process noteAndTimeList, handle human sound vibration and overtone.
     */
    private void processNoteAndTimeList() {
        // concat notes, handle vibration
        for(int i = 0; i < noteAndTimeList.size(); i++) {
            Pair<String, Float> curNoteAndTime = noteAndTimeList.get(i);
            int lastIndex = noteAndTimeResultList.size() -1;

            // handle the first notation and time
            if(noteAndTimeResultList.isEmpty()) {
                noteAndTimeResultList.add(curNoteAndTime);
                continue;
            }

            Pair<String, Float> last = noteAndTimeResultList.get(lastIndex);
            // noteAndTimeList process the first concatenating, but the noteAndTimeList might be
            // update below
            if(curNoteAndTime.first.equals(last.first)) {
                noteAndTimeResultList.set(lastIndex, new Pair<>(last.first, last.second + curNoteAndTime.second));
                continue;
            }

            // long enough for human to sing
            if(curNoteAndTime.second > tooShortToSing) {
                // pause or a different notation and time, so add
                noteAndTimeResultList.add(curNoteAndTime);
            }
            else { // too short for human to sing
                /*
                if(curNoteAndTime.first.equals(NotesFrequency.Pause.name())) { // garbage pause
                    Pair<String, Float> last = noteAndTimeResultList.get(lastIndex);

                    // add the garbage pause time to the last notation and time result
                    noteAndTimeResultList.set(lastIndex, new Pair<>(last.first, last.second + curNoteAndTime.second));
                }
                */

                /*else*/
                { // garbage note
                    /* handle vibration, overtone */

                    Pair<String, String> neighborNotesPair = NotesFrequency.getNeighborNotes(curNoteAndTime.first);

                    // case 1:
                    // e.g: notation: F4sAndG4f, time: 0.1044898   --> garbage
                    //      notation: G4,        time: 0.04643991  --> curPos, garbage
                    // concat to previous note
                    if(last.first.equals(neighborNotesPair.first) || last.first.equals(neighborNotesPair.second))
                        noteAndTimeResultList.set(lastIndex, new Pair<>(last.first, last.second + curNoteAndTime.second));

                    // case 2:
                    // e.g: notation: D4sAndE4f, time: 0.023219954  --> curPos, garbage
                    //      notation: D4,        time: 0.058049887  --> garbage
                    // if the note can't concat to prev, then concat to next note,
                    // updating the noteAndTimeList
                    else {
                        if (i + 1 < noteAndTimeList.size()) { // prevent out of index
                            Pair<String, Float> next = noteAndTimeList.get(i + 1);

                            // check the next
                            if (next.first.equals(neighborNotesPair.first) || next.first.equals(neighborNotesPair.second)) {
                                noteAndTimeList.set(i + 1, new Pair<>(next.second > curNoteAndTime.second ? next.first : curNoteAndTime.first, next.second + curNoteAndTime.second));
                            }
                        }
                    }
                }
            }
        }

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

            // convert notation info to jfugue notation
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

            // process the same note array
            for(int i = 0; i < noteTimArr.size(); i++) {
                musicString += jfugueNotation;
                Pair p = noteTimArr.get(i);

                // if the note is Rest
                if (i != 0 && !jfugueNotation.equals("R"))
                    musicString += "-";

                // process duration
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

            // measure bar
//            musicString += "| ";
            musicString += " ";
        }

        // final measure bar
        musicString += "|";

        return musicString;
    }

    /**
     * Convert a note and duration time into a NoteResult witch contains notation name and note duration array.
     * @param time how long this note last
     * @return a single NoteResult which contains notation name and note duration array
     */
    private ArrayList<Pair<String, Integer>> timeDurationToNoteDuration(float time) {
        ArrayList<Pair<String, Integer>> timeResults = new ArrayList<>();
        NoteDurations[] noteDurations = NoteDurations.values();
        float quarterNoteDuration = (float)secondsPerMinute / (float)beatsPerMinute;
        int quotient; // how many the specific note duration has, e.g 2 half notes
        float remainder = time;

        for(NoteDurations n: noteDurations) {
            quotient = (int)(remainder / (quarterNoteDuration * n.getTime()));
            remainder = remainder % (quarterNoteDuration * n.getTime());

            if(quotient != 0)
                timeResults.add(new Pair<>(n.toString(), quotient));

//            if(remainder / (quarterNoteDuration * NoteDurations.sixtyFourthNote.getTime()) < 0)
//                break;
        }

        if(!timeResults.isEmpty()) // is at least sixty-forth note
            return timeResults;
        else // note duration too short to divide by the shortest note duration
            return null;
    }

    /**
     * Delete Rests at the beginning and at the end of NoteResult array.
     */
    private void deleteBeginAndEndRests() {
        if(noteResults.isEmpty()) return;

        // delete pause at the beginning
        while(true) {
            if(noteResults.isEmpty()) break; // delete to no any notes

            if (noteResults.get(0).getNoteName().equals("Pause"))
                noteResults.remove(0);
            else
                break;
        }

        // delete pause at the end
        while(true) {
            if(noteResults.isEmpty()) break; // delete to no any notes

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

    private class mPitchDetectionHandler implements PitchDetectionHandler {
        float prePitch = -5f; // -5 indicate start sample, -1 is used
        String preNotation = null;
        float pauseTime = 0f;

        @Override
        public void handlePitch(PitchDetectionResult pdr, AudioEvent ae) {
            float pitch = pdr.getPitch();
            String notation = NotesFrequency.getNote(pitch);
            float timeDuration = ae.getTimeDuration(); // pause note buffer

            // process the first set
            if (prePitch == -5f && preNotation == null) {
                prePitch = pitch;
                preNotation = notation;

                // if the first sample note is not pause, than add to list
                if (!notation.equals(NotesFrequency.Pause.getNote()))
                    noteAndTimeList.add(new Pair<>(notation, timeDuration));
                else
                    pauseTime += timeDuration;

                return;
            }

            if (notation.equals(NotesFrequency.Pause.getNote())) { // buf the pause sample
                pauseTime += timeDuration;
                return; // not to add the pause
            }
            else { // it's a normal note

                // handle the pause note buffer
                if (pauseTime > tooShortPauseTime) { // if the pause is long enough
                    noteAndTimeList.add(new Pair<>(NotesFrequency.Pause.getNote(), pauseTime)); // add to list
                        Log.e(LOG_TAG, String.format("time: %9f, note: %s", pauseTime, NotesFrequency.Pause.getNote()));
                }
                pauseTime = 0; // reset the pause time

                // handle the pitch be near the bound of two notes
                if (!notation.equals(preNotation) && Math.abs(pitch - prePitch) < minFreqDifBetweenSamples) {
                    Log.w(LOG_TAG, String.format("time: %9f, note: %s", timeDuration, preNotation));
                    notation = preNotation;
                }
                else {
                    Log.w(LOG_TAG, String.format("time: %9f, note: %s", timeDuration, notation));
                }
            }

            int lastIndex = noteAndTimeList.size() - 1;
            Pair<String, Float> last = noteAndTimeList.get(lastIndex);

            if (notation.equals(last.first)) { // the same as the last note
                Pair<String, Float> p = new Pair<>(notation, timeDuration + last.second);
                noteAndTimeList.set(lastIndex, p);
            }
            else { // not the same as the last note, add this new note
                Pair<String, Float> p = new Pair<>(notation, timeDuration);
                noteAndTimeList.add(p);
            }

            prePitch = pitch;
            preNotation = notation;
        }
    }
}

// TODO float add err make it lose precision, using BigDecimal to avoid this (Maybe we can ignore this issue)