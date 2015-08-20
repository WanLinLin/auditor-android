package com.example.auditor;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.auditor.score.AccidentalView;
import com.example.auditor.score.BeamView;
import com.example.auditor.score.DottedView;
import com.example.auditor.score.NumberNoteView;
import com.example.auditor.score.OctaveView;
import com.example.auditor.score.PartViewGroup;

import java.io.File;

public class ShowSheetMusicActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ShowSheetMusicActivity";
    private File musicDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");
    private final boolean SHOW_NOTE_VIEW_GROUP_COLOR = false;

    // width:height = 2:3
    public static int noteHeight = 200;
    public int noteWidth = noteHeight / 3 * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sheet_music);

        String octave = "0";
        String note = "A";
        String accidental = "";
        String dot = ".";
        String duration = "x"; // i, s, ,t ,x

        // root view
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_show_sheet_music);

        // part view
        RelativeLayout.LayoutParams plp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        PartViewGroup part = new PartViewGroup(this);
        part.setBackgroundColor(Color.parseColor("#E6E6E6"));
        part.setLayoutParams(plp);
        rl.addView(part);

        // measure view
        for(int i = 0; i < 2; i++) {
            RelativeLayout.LayoutParams mlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            RelativeLayout measure = new RelativeLayout(this);
//        measure.setBackgroundColor(Color.parseColor("#FFFFFF"));
            measure.setId(i + 1);
            measure.setLayoutParams(mlp);
            part.addMeasure(measure, i);

            for (int j = 0; j < 4; j++) { // insert four notes into a measure
                // adjust note view group width
                float adaptWidth = noteWidth;
                if (accidental.equals(""))
                    adaptWidth -= noteWidth * 0.25f;
                if (dot.equals(""))
                    adaptWidth -= noteWidth * 0.25f;

                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams((int) adaptWidth, noteHeight);

                if (j > 0) { // note the first note
                    rlp.addRule(RelativeLayout.RIGHT_OF, measure.findViewById(j).getId());
                } else if (j == 0) { // is the first note
                    rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                }

                RelativeLayout aNote = printNoteViewGroup(note, accidental, dot, octave, duration);
                aNote.setLayoutParams(rlp);

                // id must be a positive number
                aNote.setId(j + 1);
                measure.addView(aNote);
            }
        }
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

    private View printNumberNoteView(String note, boolean hasAccidentalView) {
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
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            n.setBackgroundColor(Color.parseColor("#F5DA81"));

        return n;
//        noteViewGroups.get(i).addView(n);
    }

    private View printAccidentalView(String accidental) {
        // Accidental view
        AccidentalView a = new AccidentalView(this);
        a.setId(R.id.accidental_view);
        a.setAccidental(accidental);
        RelativeLayout.LayoutParams alp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.25), (int) (noteHeight * 0.4));
        alp.topMargin = (int)(noteHeight * 0.2);
        alp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        alp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        a.setLayoutParams(alp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            a.setBackgroundColor(Color.parseColor("#CEF6EC"));

        return a;
//        noteViewGroups.get(i).addView(a);
    }

    private View printDottedView(String length) {
        // Length view
        DottedView d = new DottedView(this);
        d.setId(R.id.dotted_view);
        d.setLength(length);
        RelativeLayout.LayoutParams dlp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.25), (int) (noteHeight * 0.4));
        dlp.topMargin = (int)(noteHeight * 0.2);
        dlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        dlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        d.setLayoutParams(dlp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            d.setBackgroundColor(Color.parseColor("#F5A9A9"));

        return d;
//        noteViewGroups.get(i).addView(d);
    }

    private View printOctaveView(int octave, boolean hasBeamView, boolean hasAccidentalView) {
        // Octave view
        RelativeLayout.LayoutParams olp;
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
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            o.setBackgroundColor(Color.parseColor("#CCCCCC"));

        return o;
//            noteViewGroups.get(i).addView(o);
    }

    private View printBeamView(String beams) {
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
        }
        RelativeLayout.LayoutParams blp = new RelativeLayout.LayoutParams(noteWidth, (int) (noteHeight * 0.2));
        blp.addRule(RelativeLayout.BELOW, R.id.number_note_view);
        blp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        b.setLayoutParams(blp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            b.setBackgroundColor(Color.parseColor("#81F7D8"));

        return b;
//        noteViewGroups.get(i).addView(b);
    }

    private RelativeLayout printNoteViewGroup(String note, String accidental, String dot, String octave, String duration) {
        int o = Integer.parseInt(octave);

        // set horizontal align
        RelativeLayout singleNoteView = new RelativeLayout(this);

        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            singleNoteView.setBackgroundColor(Color.parseColor("#DDDDDD"));

        singleNoteView.addView(printNumberNoteView(note, !accidental.equals("")));
        if(!accidental.equals("")) { // has accidental view
            singleNoteView.addView(printAccidentalView(accidental));
        }
        if(!dot.equals("")) // has dot view
            singleNoteView.addView(printDottedView(dot));
        if("istx".contains(duration))
            singleNoteView.addView(printBeamView(duration));
        if(o >= 0 && o <= 8 && o != 4)
            singleNoteView.addView(printOctaveView(o, !duration.equals(""), !accidental.equals("")));

        return singleNoteView;
    }
}
