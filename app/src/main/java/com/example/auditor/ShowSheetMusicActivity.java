package com.example.auditor;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.example.auditor.score.AccidentalView;
import com.example.auditor.score.BeamView;
import com.example.auditor.score.DottedView;
import com.example.auditor.score.NumberView;
import com.example.auditor.score.OctaveView;
import com.example.auditor.score.PartViewGroup;
import com.example.auditor.score.Score;

import java.io.File;

public class ShowSheetMusicActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ShowSheetMusicActivity";
    private File musicDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");
    private final boolean SHOW_PARENT_VIEW_GROUP_COLOR = false;
    private final boolean SHOW_NOTE_VIEW_GROUP_COLOR = false;

    public static final int partStartId =       10001;
    public static final int measureStartId =    101;
    public static final int noteStartId =       1;

    // width:height = 2:3
    public static int noteHeight = 200;
    public int noteWidth = noteHeight / 3 * 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sheet_music);

        String note = "A";
        String octave = "8";
        String accidental = "b";
        String tieEnd = "";
        String duration = "x"; // i, s, ,t ,x
        String tieStart = "";
        String dot = ".";

        // root view
        RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_show_sheet_music);

        ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
        Score score = new Score(this);

        ScrollView.LayoutParams slp =
                new ScrollView.LayoutParams(
                        ScrollView.LayoutParams.WRAP_CONTENT,
                        ScrollView.LayoutParams.WRAP_CONTENT);
        score.setLayoutParams(slp);
        scrollView.addView(score);

        /* add i part views into this score */
        for(int i = 0; i < 8; i++) {
            // relative to measure view
            int curX = 0;

            // part view
            RelativeLayout.LayoutParams plp =
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            PartViewGroup part = new PartViewGroup(this, noteHeight);
            if(SHOW_PARENT_VIEW_GROUP_COLOR)
                part.setBackgroundColor(Color.parseColor("#E0F8E0"));
            part.setId(i + partStartId);

            if(part.getId() == partStartId) {
                plp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            else {
                plp.addRule(RelativeLayout.BELOW, part.getId() - 1);
            }
            part.setLayoutParams(plp);
            score.addView(part);

            /* add j measure views into this part */
            for (int j = 0; j < 2; j++) {
                RelativeLayout measure = new RelativeLayout(this);
                if(SHOW_PARENT_VIEW_GROUP_COLOR)
                    measure.setBackgroundColor(Color.parseColor("#E0F8F7"));
                measure.setId(j + measureStartId);
                part.addMeasure(measure, j + measureStartId);
                curX += PartViewGroup.barStrokeWidth * 3;

                /* add k note view groups into this measure */
                for (int k = 0; k < 4; k++) {
                    // adjust note view group width
                    float adaptWidth = noteWidth;
                    if (accidental.equals(""))
                        adaptWidth -= noteWidth * 0.25f;
                    if (dot.equals(""))
                        adaptWidth -= noteWidth * 0.25f;

                    RelativeLayout.LayoutParams rlp =
                            new RelativeLayout.LayoutParams((int) adaptWidth, noteHeight);

                    if (k > 0) { // not the first note, make it right stick to the left one
                        rlp.addRule(RelativeLayout.RIGHT_OF,
                                measure.findViewById(k + noteStartId - 1).getId());
                    } else if (k == 0) { // is the first note, make it align left
                        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    }

                    RelativeLayout aNote = printNoteViewGroup(note, accidental, dot, octave, duration);
                    aNote.setLayoutParams(rlp);

                    // id must be a positive number
                    aNote.setId(k + noteStartId);
                    measure.addView(aNote);

                    duration = "s";

                    if(j == 0 && k == 3) {
                        tieStart = "-";
                    }
                    if(j == 1 && k == 0) {
                        tieEnd = "-";
                    }

                    curX += adaptWidth / 2;
                    if(tieStart.equals("-")) {
                        part.addTieInfo(new Pair<>(curX, "start"));
                        tieStart = "";
                    }

                    if(tieEnd.equals("-")) {
                        part.addTieInfo(new Pair<>(curX, "end"));
                        tieEnd = "";
                    }
                    curX += adaptWidth / 2;
                }
            }
            part.addTieView();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
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

    private NumberView printNumberView(String note, boolean hasAccidentalView) {
        // Number note view
        NumberView n = new NumberView(this);
        n.setId(R.id.number_view);
        n.setNote(note);
        RelativeLayout.LayoutParams nlp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.5), (int) (noteHeight * 0.4));
        nlp.topMargin = (int)(noteHeight * 0.225);
        nlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        if(hasAccidentalView)
            nlp.addRule(RelativeLayout.RIGHT_OF, R.id.accidental_view);
        else
            nlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        n.setLayoutParams(nlp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            n.setBackgroundColor(Color.parseColor("#F5DA81"));

        return n;
    }

    private AccidentalView printAccidentalView(String accidental) {
        // Accidental view
        AccidentalView a = new AccidentalView(this);
        a.setId(R.id.accidental_view);
        a.setAccidental(accidental);
        RelativeLayout.LayoutParams alp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.25), (int) (noteHeight * 0.4));
        alp.topMargin = (int)(noteHeight * 0.225);
        alp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        alp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        a.setLayoutParams(alp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            a.setBackgroundColor(Color.parseColor("#CEF6EC"));

        return a;
    }

    private DottedView printDottedView(String length) {
        // Length view
        DottedView d = new DottedView(this);
        d.setId(R.id.dotted_view);
        d.setLength(length);
        RelativeLayout.LayoutParams dlp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.25), (int) (noteHeight * 0.4));
        dlp.topMargin = (int)(noteHeight * 0.225);
        dlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        dlp.addRule(RelativeLayout.RIGHT_OF, R.id.number_view);

        d.setLayoutParams(dlp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            d.setBackgroundColor(Color.parseColor("#F5A9A9"));

        return d;
    }

    private OctaveView printOctaveView(int octave, boolean hasBeamView, boolean hasAccidentalView) {
        // Octave view
        OctaveView o = new OctaveView(this);
        o.setOctave(octave);
        o.setId(R.id.octave_view);
        RelativeLayout.LayoutParams olp;
        olp = new RelativeLayout.LayoutParams((int)(noteWidth * 0.5), RelativeLayout.LayoutParams.WRAP_CONTENT);

        if(octave > 4) {
            olp.topMargin = (int)(noteHeight * 0.225f - o.geth());
            olp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        else if(octave < 4 && hasBeamView) {
            olp.addRule(RelativeLayout.BELOW, R.id.beam_view);
        }
        else if(octave < 4 && !hasBeamView){
            olp.addRule(RelativeLayout.BELOW, R.id.number_view);
        }

        if(hasAccidentalView)
            olp.leftMargin = (int)(noteWidth * 0.25);
        olp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        o.setLayoutParams(olp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            o.setBackgroundColor(Color.parseColor("#CCCCCC"));

        return o;
    }

    private BeamView printBeamView(String beams) {
        // Beam view
        BeamView b = new BeamView(this, noteHeight * 0.15f);
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
        RelativeLayout.LayoutParams blp = new RelativeLayout.LayoutParams(noteWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        blp.addRule(RelativeLayout.BELOW, R.id.number_view);
        blp.addRule(RelativeLayout.CENTER_HORIZONTAL);

        b.setLayoutParams(blp);
        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            b.setBackgroundColor(Color.parseColor("#81F7D8"));

        return b;
    }

    private RelativeLayout printNoteViewGroup(String note, String accidental, String dot, String octave, String duration) {
        int o = Integer.parseInt(octave);
        RelativeLayout singleNoteView = new RelativeLayout(this);

        if(SHOW_NOTE_VIEW_GROUP_COLOR)
            singleNoteView.setBackgroundColor(Color.parseColor("#DDDDDD"));

        // add number view
        singleNoteView.addView(printNumberView(note, !accidental.equals("")));

        if(!accidental.equals("")) { // has accidental
            // add accidental view
            singleNoteView.addView(printAccidentalView(accidental));
        }

        if(!dot.equals("")) { // has dot
            // add dot view
            singleNoteView.addView(printDottedView(dot));
        }

        if("istx".contains(duration)) { // has duration shorter than quarter note
            // add beam view
            singleNoteView.addView(printBeamView(duration));
        }

        if(o >= 0 && o <= 8 && o != 4) { // octave is 0 ~ 3, 5 ~ 8
            // add octave view
            singleNoteView.addView(printOctaveView(o, !duration.equals(""), !accidental.equals("")));
        }

        return singleNoteView;
    }
}