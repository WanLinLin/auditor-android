package com.example.auditor;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import com.example.auditor.score.AccidentalView;
import com.example.auditor.score.LengthView;
import com.example.auditor.score.NoteViewGroup;
import com.example.auditor.score.NumberNoteView;
import com.example.auditor.score.OctaveView;

import java.io.File;

public class ShowSheetMusicActivity extends ActionBarActivity {
    private static final String LOG_TAG = "ShowSheetMusicActivity";
    private File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sheet_music);

        int octave = Integer.parseInt("1");
        String note = "A";
        String accidental = "#";
        String length = "-";

        int noteWidth = 200;
        int noteHeight = 200;

        RelativeLayout rl = (RelativeLayout)findViewById(R.id.activity_show_sheet_music);
        NoteViewGroup noteViewGroup = new NoteViewGroup(this);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(noteWidth, noteHeight);
        rlp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        rlp.addRule(RelativeLayout.CENTER_VERTICAL);

        noteViewGroup.setLayoutParams(rlp);
        noteViewGroup.getRootView().setBackgroundColor(Color.parseColor("#DDDDDD"));
        rl.addView(noteViewGroup);

        // Number note view
        NumberNoteView n = new NumberNoteView(this);
        n.setNote(note);
        NoteViewGroup.LayoutParams nlp = new NoteViewGroup.LayoutParams(noteWidth / 3, (int) (noteHeight * 0.5));
        nlp.gravity = Gravity.CENTER_VERTICAL;
        nlp.position = NoteViewGroup.LayoutParams.POSITION_MIDDLE;
        n.setLayoutParams(nlp);
        n.setBackgroundColor(Color.parseColor("#F5DA81"));
        noteViewGroup.addView(n);

        // Accidental view
        AccidentalView a = new AccidentalView(this);
        a.setAccidental(accidental);
        NoteViewGroup.LayoutParams alp = new NoteViewGroup.LayoutParams(noteWidth / 3, (int) (noteHeight * 0.5));
        alp.gravity = Gravity.CENTER_VERTICAL;
        alp.position = NoteViewGroup.LayoutParams.POSITION_LEFT;
        a.setPadding(5, 5, 5, 5);
        a.setLayoutParams(alp);
        a.setBackgroundColor(Color.parseColor("#CEF6EC"));
        noteViewGroup.addView(a);

        // Accidental view
        LengthView l = new LengthView(this);
        l.setLength(length);
        NoteViewGroup.LayoutParams llp = new NoteViewGroup.LayoutParams(noteWidth / 3, (int) (noteHeight * 0.5));
        llp.gravity = Gravity.CENTER_VERTICAL;
        llp.position = NoteViewGroup.LayoutParams.POSITION_RIGHT;
        l.setLayoutParams(llp);
        l.setBackgroundColor(Color.parseColor("#F5A9A9"));
        noteViewGroup.addView(l);

        // Octave view
        if(octave != 4) {
            OctaveView o = new OctaveView(this);
            o.setOctave(octave);
            NoteViewGroup.LayoutParams olp = new NoteViewGroup.LayoutParams(noteWidth / 3, (int) (noteHeight * 0.25));
            if (octave > 4)
                olp.gravity = Gravity.TOP;
            else
                olp.gravity = Gravity.BOTTOM;
            olp.position = NoteViewGroup.LayoutParams.POSITION_MIDDLE;
            o.setLayoutParams(olp);
            o.setBackgroundColor(Color.parseColor("#CCCCCC"));
            noteViewGroup.addView(o);
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
}
