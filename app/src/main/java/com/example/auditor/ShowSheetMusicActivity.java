package com.example.auditor;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.example.auditor.score.AccidentalView;
import com.example.auditor.score.BeamView;
import com.example.auditor.score.DottedView;
import com.example.auditor.score.NumberNoteView;
import com.example.auditor.score.OctaveView;

import java.io.File;
import java.util.ArrayList;

public class ShowSheetMusicActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ShowSheetMusicActivity";
    private File musicDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");
    private final boolean SHOW_COLOR = false;

    // width:height = 2:3
    private int noteHeight = 500;
    private int noteWidth = noteHeight / 3 * 2;

    private ArrayList<RelativeLayout> noteViewGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sheet_music);

        noteViewGroups = new ArrayList<>();
        RelativeLayout n = new RelativeLayout(this);
        RelativeLayout n2 = new RelativeLayout(this);
        RelativeLayout n3 = new RelativeLayout(this);
        noteViewGroups.add(n);
        noteViewGroups.add(n2);
        noteViewGroups.add(n3);

        String octave = "0";
        String note = "A";
        String accidental = "";
        String dot = ".";
        String duration = "x"; // i, s, ,t ,x

        // root view
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_show_sheet_music);

        // measure view
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(noteWidth * 3, noteHeight);
        RelativeLayout Measure = new RelativeLayout(this);
        Measure.setBackgroundColor(Color.parseColor("#A4A4A4"));
        Measure.setLayoutParams(lp);
        rl.addView(Measure);

        printNoteViewGroup(note, accidental, dot, octave, duration, 0);

        octave = "7";
        note = "D";
        accidental = "";
        duration = "i";
        dot = "";

        printNoteViewGroup(note, accidental, dot, octave, duration, 1);

        octave = "4";
        note = "F";
        accidental = "";
        duration = "s";
        dot = "";

        printNoteViewGroup(note, accidental, dot, octave, duration, 2);

        Measure.addView(noteViewGroups.get(0));
        Measure.addView(noteViewGroups.get(1));
        Measure.addView(noteViewGroups.get(2));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_sheet_music, menu);
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }

    private void printNumberNoteView(String note, int i, boolean hasAccidentalView) {
        // Number note view
        NumberNoteView n = new NumberNoteView(this);
        n.setId(R.id.number_note_view);
        n.setNote(note);
        RelativeLayout.LayoutParams nlp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.5), (int) (noteHeight * 0.4));
        nlp.topMargin = (int)(noteHeight * 0.2);
        nlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        if(hasAccidentalView)
            nlp.leftMargin = (int)(noteWidth * 0.25);
        nlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        n.setLayoutParams(nlp);
        if(SHOW_COLOR)
            n.setBackgroundColor(Color.parseColor("#F5DA81"));
        noteViewGroups.get(i).addView(n);
    }

    private void printAccidentalView(String accidental, int i) {
        // Accidental view
        AccidentalView a = new AccidentalView(this);
        a.setId(R.id.accidental_view);
        a.setAccidental(accidental);
        RelativeLayout.LayoutParams alp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.25), (int) (noteHeight * 0.4));
        alp.topMargin = (int)(noteHeight * 0.2);
        alp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        alp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        a.setLayoutParams(alp);
        if(SHOW_COLOR)
            a.setBackgroundColor(Color.parseColor("#CEF6EC"));
        noteViewGroups.get(i).addView(a);
    }

    private void printDottedView(String length, int i) {
        // Length view
        DottedView d = new DottedView(this);
        d.setId(R.id.dotted_view);
        d.setLength(length);
        RelativeLayout.LayoutParams dlp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.25), (int) (noteHeight * 0.4));
        dlp.topMargin = (int)(noteHeight * 0.2);
        dlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        dlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        d.setLayoutParams(dlp);
        if(SHOW_COLOR)
            d.setBackgroundColor(Color.parseColor("#F5A9A9"));
        noteViewGroups.get(i).addView(d);
    }

    private void printOctaveView(int octave, int i, boolean hasBeamView, boolean hasAccidentalView) {
        RelativeLayout.LayoutParams olp;

        // Octave view
        if(octave != 4) {
            OctaveView o = new OctaveView(this);
            o.setOctave(octave);
            o.setId(R.id.octave_view);
            olp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.5), (int) (noteHeight * 0.2));

            if(octave > 4) {
                olp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            else if(octave < 4 && hasBeamView) {
                olp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
            else if(octave < 4 && !hasBeamView){
                olp.addRule(RelativeLayout.BELOW, R.id.number_note_view);
            }

            if(hasAccidentalView)
                olp.leftMargin = (int)(noteWidth * 0.25);
            olp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            o.setLayoutParams(olp);
            if(SHOW_COLOR)
                o.setBackgroundColor(Color.parseColor("#CCCCCC"));
            noteViewGroups.get(i).addView(o);
        }
    }

    private void printBeamView(String beams, int i) {
        // Beam view
        BeamView b = new BeamView(this);
        b.setId(R.id.beam_view);
        switch (beams) {
            case "i":
                b.setBeams(1);
                break;
            case "s":
                b.setBeams(2);
                break;
            case "t":
                b.setBeams(3);
                break;
            case "x":
                b.setBeams(4);
                break;
            default:
                return;
        }
        RelativeLayout.LayoutParams blp = new RelativeLayout.LayoutParams(noteWidth, (int) (noteHeight * 0.2));
        blp.addRule(RelativeLayout.BELOW, R.id.number_note_view);
        blp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        b.setLayoutParams(blp);
        if(SHOW_COLOR)
            b.setBackgroundColor(Color.parseColor("#81F7D8"));
        noteViewGroups.get(i).addView(b);
    }

    private void printNoteViewGroup(String note, String accidental, String dot, String octave, String duration, int i) {
        int o = Integer.parseInt(octave);
        RelativeLayout.LayoutParams rlp;
        float adaptWidth = noteWidth;

        if(accidental.equals(""))
            adaptWidth -= noteWidth*0.25f;
        if(dot.equals(""))
            adaptWidth -= noteWidth*0.25f;
        rlp = new RelativeLayout.LayoutParams((int)adaptWidth, noteHeight);

        noteViewGroups.get(i).setId(i + 1); // id must be a positive number, so i + 1 (first i is 0)
        if(i > 0) {
            rlp.addRule(RelativeLayout.RIGHT_OF, noteViewGroups.get(i - 1).getId());
        }
        else if(i == 0) {
            rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        noteViewGroups.get(i).setLayoutParams(rlp);
        if(SHOW_COLOR)
            noteViewGroups.get(i).setBackgroundColor(Color.parseColor("#DDDDDD"));

        printNumberNoteView(note, i, !accidental.equals(""));
        if(!accidental.equals("")) { // has accidental view
            printAccidentalView(accidental, i);
        }
        if(!dot.equals("")) // has dot view
            printDottedView(dot, i);
        printBeamView(duration, i);
        printOctaveView(o, i, !duration.equals(""), !accidental.equals(""));
    }
}
