package com.example.auditor;

import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import org.jfugue.MusicStringParser;
import org.jfugue.Pattern;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import nu.xom.Serializer;

/**
 * Created by Wan Lin on 15/7/15.
 * Handle music stream pitch and notes conversion.
 */
public class SongConverter{
    private static final String LOG_TAG = "SongConverter";

    private static final float tooShortForHumanToSing = 0.02f; // 0.03 second can be ignore
    private static final int secondsPerMinute = 60; // seconds per minutes
    private static final int beatsPerMinute = 120; // bits per minute, speed
    private static final int beatsPerBar = 4; // 4 beats per bar
    private static final int beatUnit = 4; // quarter notes per bit

    private AudioDispatcher dispatcher;

    // Pair<String, Float> stores the String: note name, and Float: time duration of this note
    private ArrayList<Pair<String, Float>> notationAndTimeList; // store pitch result
    private ArrayList<Pair<String, Float>> notationAndTimeResultList;
    private ArrayList<NoteResult> noteResults = new ArrayList<>();

    public SongConverter(UniversalAudioInputStream universalAudioInputStream) {
        notationAndTimeList = new ArrayList<>();
        notationAndTimeResultList  = new ArrayList<>();

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
                Log.i(LOG_TAG, "notation: " + notation + ", time: " + sampleTime);

                // handle the first pitch
                if(notationAndTimeList.isEmpty()) {
                    Pair<String, Float> p = new Pair<>(notation, sampleTime);
                    notationAndTimeList.add(p);
                    return;
                }

                Pair last = notationAndTimeList.get(notationAndTimeList.size() - 1);

                if(notation == last.first) {
                    Pair<String, Float> p = new Pair<>(notation, sampleTime + (Float)last.second);
                    notationAndTimeList.set(notationAndTimeList.size() -1, p);
                }
                else if (notation == null) { // I don't know why get null
                    Pair<String, Float> p = new Pair<>("Pause", sampleTime);
                    notationAndTimeList.add(p);
                }
                else {
                    Pair<String, Float> p = new Pair<>(notation, sampleTime);
                    notationAndTimeList.add(p);
                }
            }
        };

        AudioProcessor ap = new PitchProcessor(
                PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                universalAudioInputStream.getFormat().getSampleRate(),
                AudioRecordActivity.bufferSize, pdh);

        dispatcher.addAudioProcessor(ap);
    }

    public void convert() {
        // convert byte file into float pitches, and store note names and time duration into
        dispatcher.run();

        for(int i = 0; i < notationAndTimeList.size(); i++) {
            Pair<String, Float> p = notationAndTimeList.get(i);

            // long enough
            if(p.second > tooShortForHumanToSing) {
                if(!notationAndTimeResultList.isEmpty()) { // is not empty
                    Pair<String, Float> last = notationAndTimeResultList.get(notationAndTimeResultList.size() -1);

                    if(p.first.equals(last.first)) { // same note, but split by a garbage, so concat
                        notationAndTimeResultList.set(notationAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                    }
                    else { // maybe pause or a different notation and time, so add
                        notationAndTimeResultList.add(p);
                    }
                }
                else { // handle the first notation and time
                    notationAndTimeResultList.add(p);
                }
            }
            else { // too short for human to sing
                if(p.first.equals(NotesFrequency.Pause.name())) { // garbage pause
                    if(!notationAndTimeResultList.isEmpty()) { // is not empty
                        Pair<String, Float> last = notationAndTimeResultList.get(notationAndTimeResultList.size() -1);

                        // add the garbage pause time to the last notation and time result
                        notationAndTimeResultList.set(notationAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                    }
                }
                else {
                    /* handle vibration, overtone */

                    // case 1:
                    // e.g: notation: F4sAndG4f, time: 0.1044898
                    //      notation: G4,        time: 0.04643991
                    if(!notationAndTimeResultList.isEmpty()) { // is not empty
                        Pair<String, Float> last = notationAndTimeResultList.get(notationAndTimeResultList.size() -1);
                        try {
                            if (last.first.contains(p.first.substring(0, 1)) || last.first.contains(p.first.substring(6, 7))) {
                                // add the similar note time to the last nation and time result
                                notationAndTimeResultList.set(notationAndTimeResultList.size() - 1, new Pair<>(last.first, last.second + p.second));
                                continue;
                            }
                        }
                        catch (IndexOutOfBoundsException e) {
                            // last notation string is shorter than 6
                            // I decide to do nothing
                        }
                    }

                    // case 2:
                    // e.g: notation: D4sAndE4f, time: 0.023219954
                    //      notation: D4,        time: 0.058049887
                    if(i + 1 < notationAndTimeList.size()) { // prevent out of index
                        Pair<String, Float> next = notationAndTimeList.get(i + 1);
                        try {
                            if (next.first.contains(p.first.substring(0, 1)) || next.first.contains(p.first.substring(6, 7))) {
                                // check the next
                                notationAndTimeList.set(i + 1, new Pair<>(next.second > p.second ? next.first : p.first, next.second + p.second));
                            }
                        }
                        catch (IndexOutOfBoundsException e) {
                            // last notation string is shorter than 6
                            // I decide to do nothing
                        }
                    }
                }
            }
        }

        // process time to note duration
        for(Pair p: notationAndTimeResultList) {
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

        String musicString = "";

        for(NoteResult n: noteResults) {
            ArrayList<Pair<String, Integer>> noteTimArr = n.getNoteTimeArr();
            String jfugueNotation = n.getNoteName().substring(0, 2);

            for(int i = 0; i < noteTimArr.size(); i++) {
                if(jfugueNotation.equals("Pa")) { // "Pa"use -> "R"est
                    jfugueNotation = "R";
                }
                else if(jfugueNotation.length() == 3) { // sharp or flat
                    if(jfugueNotation.endsWith("s"))
                        jfugueNotation = jfugueNotation.substring(0, 1) + "#" + jfugueNotation.substring(1, 2);
                    else if(jfugueNotation.endsWith("f"))
                        jfugueNotation = jfugueNotation.substring(0, 1) + "b" + jfugueNotation.substring(1, 2);
                }

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

        File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");
        FileOutputStream file;
        try {
            file = new FileOutputStream(new File(auditorDir + "/music.xml"));
            AuditorMusicXmlRenderer renderer = new AuditorMusicXmlRenderer();
            MusicStringParser parser = new MusicStringParser();
            parser.addParserListener(renderer);

            Pattern pattern = new Pattern(musicString);
            parser.parse(pattern);

            Serializer serializer = new Serializer(file, "UTF-8");
            serializer.setIndent(4);
            serializer.write(renderer.getMusicXMLDoc());

            file.flush();
            file.close();
        }
        catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "file not found");
        }
        catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "unsupported encoding exception");
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "io exception");
        }

        Log.d(LOG_TAG, musicString);

        Log.i(LOG_TAG, "Pitch detect successfully!");
        notationAndTimeList.clear();
        notationAndTimeResultList.clear();
    }

    private String pitchToNotation(float pitch) {
        return NotesFrequency.getNote(pitch);
    }

    private NoteResult timeToNotes(String noteName, float time) {
        ArrayList<Pair<String, Integer>> timeResults = new ArrayList<>();
        NotesLong[] notesLongs = NotesLong.values();
        float remainder = time / (secondsPerMinute * beatsPerBar / beatsPerMinute);

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

// TODO list all the event will encounter when handle the pitch, and solve it one by one