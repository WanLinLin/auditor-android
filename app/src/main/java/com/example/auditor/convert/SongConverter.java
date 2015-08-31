package com.example.auditor.convert;

import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import com.example.auditor.AudioRecordActivity;
import com.example.auditor.song.ExtAudioRecorder;

import org.jfugue.Pattern;

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
public class SongConverter{
    private static final String LOG_TAG = "SongConverter";
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private String musicDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/";

    private static final float tooShortForHumanToSing = 0.02f; // 0.03 second can be ignore
    private int secondsPerMinute = 60; // seconds per minutes
    private int beatsPerMinute = 120; // bits per minute, speed
    private int beatsPerMeasure = 4; // 4 beats per bar
    private int beatUnit = 4; // quarter notes per bit
    private int measureDuration = secondsPerMinute / beatsPerMinute * beatsPerMeasure;
    private String songTitle;

    private AudioDispatcher dispatcher;

    // Pair<String, Float> stores the String: note name, and Float: time duration of this note
    private ArrayList<Pair<String, Float>> noteAndTimeList; // store pitch result
    private ArrayList<Pair<String, Float>> noteAndTimeResultList;
    private ArrayList<NoteResult> noteResults;

    public SongConverter(String songTitle) {
        noteAndTimeList = new ArrayList<>();
        noteAndTimeResultList = new ArrayList<>();
        noteResults = new ArrayList<>();
        this.songTitle = songTitle;
    }

    public boolean setUp() {
        File file = new File(auditorDir + songTitle + ".wav");
        InputStream inputStream;

        // open a file
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "Failed to open a file!");
            return false;
        }

        ExtAudioRecorder extAudioRecorder = ExtAudioRecorder.getInstanse(ExtAudioRecorder.RECORDING_UNCOMPRESSED);
        // use exAudioRecorder to set TarsosDSP Audio format
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

        // set pitch detect things
        dispatcher = new AudioDispatcher(
                universalAudioInputStream,
                AudioRecordActivity.bufferSize,
                AudioRecordActivity.bufferSize / 2);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pdr, AudioEvent ae){
                String notation = pitchToNotation(pdr.getPitch());
                Float sampleTime = ae.getConvertTime();

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

        AudioProcessor ap = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                universalAudioInputStream.getFormat().getSampleRate(),
                AudioRecordActivity.bufferSize, pdh);

        dispatcher.addAudioProcessor(ap);

        return true;
    }

    public void convert() {
        String musicString;
        Pattern pattern;

        // convert byte file into float pitches, and store note names and time duration into
        dispatcher.run();

        // concat notes, handle vibration and overtone
        processNoteAndTimeList();

        // cut time to note duration
        for(Pair p: noteAndTimeResultList) {
            NoteResult noteResult = timeToNotes(p.first.toString(), (float) p.second);
            if(noteResult != null)
                noteResults.add(noteResult);
        }

        // delete pause at the beginning
        for(int i = 0; i < noteResults.size(); i++) {
            if (noteResults.get(i).getNoteName().equals("Pause"))
                noteResults.remove(i);
            else
                break;
        }

        // convert result to music string
        musicString = processIntoMusicString();

        pattern = new Pattern(musicString);
        try {
            pattern.savePattern(new File(auditorDir + songTitle + ".txt"));
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "IOE");
        }

//        try {
//            file = new FileOutputStream(new File(auditorDir + "/music.xml"));
//            AuditorMusicXmlRenderer renderer = new AuditorMusicXmlRenderer();
//            MusicStringParser parser = new MusicStringParser();
//            parser.addParserListener(renderer);
//
//            Pattern pattern = new Pattern(musicString);
//            parser.parse(pattern);
//
//            Serializer serializer = new Serializer(file, "UTF-8");
//            serializer.setIndent(4);
//            serializer.write(renderer.getMusicXMLDoc());
//
//            file.flush();
//            file.close();
//        }
//        catch (FileNotFoundException e) {
//            Log.e(LOG_TAG, "file not found");
//        }
//        catch (UnsupportedEncodingException e) {
//            Log.e(LOG_TAG, "unsupported encoding exception");
//        }
//        catch (IOException e) {
//            Log.e(LOG_TAG, "io exception");
//        }

        Log.d(LOG_TAG, musicString);
        Log.i(LOG_TAG, "Pitch detect successfully!");
    }

    private String processIntoMusicString() {
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
                    else { // maybe pause or a different notation and time, so add
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
                else {
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

    private String pitchToNotation(float pitch) {
        return NotesFrequency.getNote(pitch);
    }

    private NoteResult timeToNotes(String noteName, float time) {
        ArrayList<Pair<String, Integer>> timeResults = new ArrayList<>();
        NotesLong[] notesLongs = NotesLong.values();
        float remainder = time / (secondsPerMinute * beatsPerMeasure / beatsPerMinute);

        for(NotesLong n: notesLongs) {
            int quotient = (int)(remainder / n.getTime());
            remainder = remainder % n.getTime();

            if(quotient != 0)
                timeResults.add(new Pair<>(n.toString(), quotient));
        }

        if(!timeResults.isEmpty()) // is at least sixty-forth note
            return new NoteResult(noteName, timeResults);
        else // note duration too short to divide by the shortest note duration
            return null;
    }
}

// TODO float add err make it lose precision, using BigDecimal to avoid this (Maybe we can ignore this issue)

// TODO fixed jfugue for android fucking save midi problem