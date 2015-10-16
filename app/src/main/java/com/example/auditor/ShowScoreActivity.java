package com.example.auditor;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.auditor.button.AccidentalButton;
import com.example.auditor.button.BeamButton;
import com.example.auditor.button.DottedButton;
import com.example.auditor.button.NewNoteButton;
import com.example.auditor.button.NumberButton;
import com.example.auditor.button.OctaveButton;
import com.example.auditor.button.PlayButton;
import com.example.auditor.score.AccidentalView;
import com.example.auditor.score.BeamView;
import com.example.auditor.score.DottedView;
import com.example.auditor.score.MeasureViewGroup;
import com.example.auditor.score.NoteViewGroup;
import com.example.auditor.score.NumberView;
import com.example.auditor.score.NumberedMusicalNotationParser;
import com.example.auditor.score.OctaveView;
import com.example.auditor.score.PartViewGroup;
import com.example.auditor.score.ScoreViewGroup;
import com.example.auditor.score.WordView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jfugue.pattern.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShowScoreActivity extends ActionBarActivity {
    private static final String LOG_TAG = ShowScoreActivity.class.getName();
    private static final String txtDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/txt/";
    private static final String midiDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/midi/";

    public static ScoreViewGroup score;
    public static RelativeLayout scoreContainer;
    private Pattern pattern;
    private NumberedMusicalNotationParser numberedMusicalNotationParser;

    /* play midi part */
    private MediaPlayer musicPlayer;
    private Handler updateSeekBarHandler = new Handler();
    private Runnable updateSeekBarTask;
    private boolean playbackPaused = false;
    private MusicController musicController;

    /* using for two dimension scroll */
    private VScrollView vScroll;
    private HScrollView hScroll;
    public static float mx;
    public static float my;

    public static ActionBar actionBar;
    public static Menu menu;
    public String scoreName;

    /* View id index */
    public static final int partMaxNumber = 5000;
    public static final int partStartId = 10001;
    public static final int measureStartId = 201;
    public static final int wordStartId = 101;
    public static final int noteStartId = 1;

    /* View size */
    public static int defaultNoteHeight;
    public static int defaultNoteEditHeight;
    public static int defaultNoteEditWidth;
    public static int noteHeight;
    public static int noteWidth;

    private int screenHeight;

    /* Edit parameter */
    public static boolean scoreEditMode;
    public static boolean lyricEditMode;

    /* lyric recommend views */
    public static Button recommendButton;
    public static Button completeButton;
    public static Spinner rhymeSpinner;
    public static NumberPicker lyricNumberPicker;
    public static RelativeLayout rootView;
    public static AutoCompleteTextView lyricInputACTextView;
    public static RelativeLayout keyboard;

    /* score edit buttons */
    public static AccidentalButton accidentalButton;
    public static NumberButton numberButton;
    public static BeamButton beamButton;
    public static DottedButton dottedButton;
    public static OctaveButton topOctaveButton;
    public static OctaveButton bottomOctaveButton;

    private ProgressDialog dialog;
    private ArrayList<String> suggestWords = new ArrayList<>();
    private WordAdapter wordAdapter;
    private String inputSentence = "";

    private static final int secondsPerMinute = 60; // seconds per minutes
    private int beatsPerMinute = 90; // bits per minute, speed
    private int beatsPerMeasure = 4; // 4 beats per bar
    private int beatUnit = 4; // quarter notes per bit
    private int measureDuration = secondsPerMinute / beatsPerMinute * beatsPerMeasure;

    public static float mScaleFactor = 1.f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        scoreEditMode = false;
        lyricEditMode = false;

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        defaultNoteHeight = screenHeight / 6;
        defaultNoteEditHeight = (int)getResources().getDimension(R.dimen.default_edit_note_group_height);
        defaultNoteEditWidth = defaultNoteEditHeight / 3 * 2;

        // reset parameters
        mScaleFactor = 1.f;
        setDimensions();

        vScroll = (VScrollView) findViewById(R.id.vScroll);
        hScroll = (HScrollView) findViewById(R.id.hScroll);
        scoreContainer = (RelativeLayout)findViewById(R.id.score_container);
        scoreContainer.setVisibility(View.GONE);
        rootView = (RelativeLayout)findViewById(R.id.activity_show_score);
        scoreName = getIntent().getStringExtra("score name");

        // set action bar title
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setUpLyricRecommendGroup();
        setUpEditScoreKeyboard();

        musicController = new MusicController(this);
        musicController.setVisibility(View.GONE);

        initPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        updateSeekBarHandler.removeCallbacks(updateSeekBarTask);

        // pause music
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause();
            musicController.playButton.setPlay(true);
            musicController.playButton.invalidate();
            playbackPaused = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (musicPlayer.isPlaying()) {
            musicPlayer.stop();
            musicPlayer.release();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if(hasFocus && scoreContainer.getWidth() == 0) {
            try {
                pattern = Pattern.load(new File(txtDir + scoreName));

                numberedMusicalNotationParser =
                        new NumberedMusicalNotationParser(this, pattern.toString());

                numberedMusicalNotationParser.parse();
                score = numberedMusicalNotationParser.getScoreViewGroup();
                scoreContainer.addView(score);
            } catch (IOException e) {
                Log.e(getClass().getName(), e.getMessage());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        ShowScoreActivity.menu = menu;
        getMenuInflater().inflate(R.menu.menu_show_score, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_zoom_in:
                if(mScaleFactor < 3) {
                    mScaleFactor += 0.2;
                    zoom();

                    if(mScaleFactor == 3) {
                        Toast.makeText(
                                ShowScoreActivity.this,
                                "最大!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
                return true;
            case R.id.action_zoom_out:
                if (mScaleFactor > 0.6) {
                    mScaleFactor -= 0.2;
                    zoom();

                    if(mScaleFactor == 0.6) {
                        Toast.makeText(
                                ShowScoreActivity.this,
                                "最小!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
                return true;
            case R.id.action_save:
                dialog = ProgressDialog.show(ShowScoreActivity.this,
                        "儲存中", "請稍後...",true);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try { saveScore(); }
                        finally { dialog.dismiss(); }
                    }
                }).start();

//                new Thread(new Runnable(){
//                    @Override
//                    public void run() {
//                        try{
//                            FileOutputStream out = null;
//                            try {
//                                out = new FileOutputStream(auditorDir + "score/" + scoreName + ".png");
//                                getBitmapFromView(scoreContainer).compress(Bitmap.CompressFormat.PNG, 100, out);
//                                // PNG is a lossless format, the compression factor (100) is ignored
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            } finally {
//                                try {
//                                    if (out != null) {
//                                        out.close();
//                                    }
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                        catch(Exception e){
//                            e.printStackTrace();
//                        }
//                        finally {
//                            dialog.dismiss();
//                        }
//                    }
//                }).start();

                return true;

            case R.id.score_play:
                if(!musicController.isShown()) {
                    musicController.setVisibility(View.VISIBLE);
                    Animation animation = AnimationUtils.loadAnimation(this, R.anim.keyboard_swipe_in);
                    musicController.setAnimation(animation);
                    musicController.animate();
                }

                playSong();
                updateSeekBarHandler.post(updateSeekBarTask);

                return true;

            case android.R.id.home:
                if(scoreEditMode || lyricEditMode) {
                    lyricEditMode = false;
                    scoreEditMode = false;

                    /* hide lyric input text view and recommend button */
                    setLyricRecommendGroupVisibility(false);
                    /* close keyboard */
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(ShowScoreActivity.lyricInputACTextView.getWindowToken(), 0);
                    if(PartViewGroup.lyricEditStartMeasure != null) PartViewGroup.saveWordsIntoWordView();

                    // close edit score keyboard
                    if(keyboard.isShown()) {
                        Animation animation = AnimationUtils.loadAnimation(this, R.anim.keyboard_swipe_out);
                        keyboard.setAnimation(animation);
                        keyboard.animate();
                        keyboard.setVisibility(View.GONE);
                    }

                    actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.AuditorColorPrimary)));
                    actionBar.setTitle(scoreName);
                    ShowScoreActivity.menu.findItem(R.id.action_zoom_in).setVisible(true);
                    ShowScoreActivity.menu.findItem(R.id.action_zoom_out).setVisible(true);
                }
                else {
                    Intent intent = new Intent(this, SlidingTabActivity.class);
                    intent.putExtra("initialPosition", SlidingTabAdapter.SCORE_FILE_LIST);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if(scoreEditMode || lyricEditMode) {
            scoreEditMode = false;
            lyricEditMode = false;

            // close edit lyric group
            setLyricRecommendGroupVisibility(false);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(ShowScoreActivity.lyricInputACTextView.getWindowToken(), 0);
            if(PartViewGroup.lyricEditStartMeasure != null) PartViewGroup.saveWordsIntoWordView();

            // close edit score keyboard
            if(keyboard.isShown()) {
                Animation animation = AnimationUtils.loadAnimation(this, R.anim.keyboard_swipe_out);
                keyboard.setAnimation(animation);
                keyboard.animate();
                keyboard.setVisibility(View.GONE);
            }

            // set actionbar back to original color
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.AuditorColorPrimary)));
            actionBar.setTitle(scoreName);
            ShowScoreActivity.menu.findItem(R.id.action_zoom_in).setVisible(true);
            ShowScoreActivity.menu.findItem(R.id.action_zoom_out).setVisible(true);
        }
        else if(musicController.isShown()) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.keyboard_swipe_out);
            musicController.setAnimation(animation);
            musicController.animate();
            musicController.setVisibility(View.GONE);
        }
        else {
            Intent intent = new Intent(this, SlidingTabActivity.class);
            intent.putExtra("initialPosition", SlidingTabAdapter.SCORE_FILE_LIST);
            startActivity(intent);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX;
        float curY;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mx = event.getX();
                my = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                curX = event.getX();
                curY = event.getY();
                vScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                hScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                mx = curX;
                my = curY;
                break;
        }

        return true;
    }

    private void setDimensions() {
        noteHeight = (int) (defaultNoteHeight * mScaleFactor);
        noteWidth = noteHeight / 3 * 2;

        NoteChildViewDimension.NUMBER_VIEW_WIDTH = (int) (noteWidth * 0.5);
        NoteChildViewDimension.NUMBER_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH = (int) (noteWidth * 0.25);
        NoteChildViewDimension.ACCIDENTAL_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        NoteChildViewDimension.BEAM_VIEW_HEIGHT = Math.round(noteHeight * 0.15f);

        NoteChildViewDimension.BLANK_VIEW_WIDTH = (int) (noteWidth * 0.25);
        NoteChildViewDimension.BLANK_VIEW_HEIGHT = (int) (noteHeight * 0.225f);

        NoteChildViewDimension.DOTTED_VIEW_WIDTH = (int) (noteWidth * 0.25);
        NoteChildViewDimension.DOTTED_VIEW_HEIGHT = (int) (noteHeight * 0.4);

        NoteChildViewDimension.OCTAVE_VIEW_WIDTH = (int) (noteWidth * 0.5);
        NoteChildViewDimension.OCTAVE_VIEW_HEIGHT = (int) (noteHeight * 0.225f);

        NoteChildViewDimension.BAR_STROKE_WIDTH = Math.round(noteHeight * 0.03f);
        NoteChildViewDimension.BAR_VIEW_HEIGHT = noteHeight;
        NoteChildViewDimension.BAR_VIEW_WIDTH = NoteChildViewDimension.BAR_STROKE_WIDTH * 7;

        NoteChildViewDimension.TIE_STROKE_WIDTH = Math.round(noteHeight * 0.022f);
        NoteChildViewDimension.TIE_VIEW_HEIGHT = Math.round(noteHeight * 0.225f);

        NoteChildViewDimension.WORD_VIEW_HEIGHT = (int) (noteHeight * 0.3f);
    }

    public static class NoteChildViewDimension {
        public static int NUMBER_VIEW_WIDTH;
        public static int NUMBER_VIEW_HEIGHT;

        public static int ACCIDENTAL_VIEW_WIDTH;
        public static int ACCIDENTAL_VIEW_HEIGHT;

        public static int BEAM_VIEW_HEIGHT;

        public static int BLANK_VIEW_WIDTH;
        public static int BLANK_VIEW_HEIGHT;

        public static int DOTTED_VIEW_WIDTH;
        public static int DOTTED_VIEW_HEIGHT;

        public static int OCTAVE_VIEW_WIDTH;
        public static int OCTAVE_VIEW_HEIGHT;

        public static int BAR_STROKE_WIDTH;
        public static int BAR_VIEW_HEIGHT;
        public static int BAR_VIEW_WIDTH;

        public static int TIE_STROKE_WIDTH;
        public static int TIE_VIEW_HEIGHT;

        public static int WORD_VIEW_HEIGHT;
    }

    private static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ALPHA_8);

        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);

        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);

        // draw the view on the canvas
        view.draw(canvas);

        return returnedBitmap;
    }

    public void zoom() {
        setDimensions();

        for (int i = 0; i < partMaxNumber; i++) {
            PartViewGroup part = (PartViewGroup)score.findViewById(i + partStartId);
            if (part == null) break;

            int curX = 0;
            part.clearTieInfo();
            part.getTieViewGroup().removeAllViews();

            for(int j = 0; j < partStartId - measureStartId; j++) {
                MeasureViewGroup measure = (MeasureViewGroup)part.findViewById(j + measureStartId);
                if (measure == null) break;

                curX += NoteChildViewDimension.BAR_VIEW_WIDTH;

                for(int k = 0; k < measureStartId - noteStartId; k++) {
                    NoteViewGroup note = (NoteViewGroup)measure.findViewById(k + noteStartId);
                    if (note == null) break;

                    int noteViewWidth = NoteChildViewDimension.NUMBER_VIEW_WIDTH;
                    if(note.hasAccidentalView())
                        noteViewWidth += NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
                    if(note.hasDottedView())
                        noteViewWidth += NoteChildViewDimension.DOTTED_VIEW_WIDTH;

                    if(note.isTieEnd()) {
                        part.addTieInfo(new Pair<>((curX + noteViewWidth/2), "end"));
                    }
                    if(note.isTieStart()) {
                        part.addTieInfo(new Pair<>((curX + noteViewWidth/2), "start"));
                    }
                    curX += noteViewWidth;
                }
            }
            part.printTieView();
            part.requestLayout();
        }
    }

    private void saveScore() {
        String keySignature = "KCmaj ";
        String tempo = "T" + beatsPerMinute + " ";
        String musicString = keySignature + tempo;
        String noteContext;

        ScoreViewGroup score = (ScoreViewGroup)scoreContainer.findViewById(R.id.score_view_group);
        for(int i = 0; i < partMaxNumber; i++) {
            PartViewGroup part = (PartViewGroup)score.findViewById(i + partStartId);
            if(part == null) break;

            for(int j = 0; j < partStartId - measureStartId; j++) {
                MeasureViewGroup measure = (MeasureViewGroup)part.findViewById(j + measureStartId);
                if(measure == null) break;

                for(int k = 0; k < measureStartId - noteStartId; k++) {
                    noteContext = "";

                    NoteViewGroup note = (NoteViewGroup)measure.findViewById(k + noteStartId);
                    if(note == null) break;

                    NumberView numberView = (NumberView)note.findViewById(R.id.number_view);
                    if(numberView != null) noteContext += numberView.getNote();

                    AccidentalView accidentalView = (AccidentalView)note.findViewById(R.id.accidental_view);
                    if(accidentalView != null) noteContext += accidentalView.getAccidental();

                    OctaveView octaveView = (OctaveView)note.findViewById(R.id.octave_view);
                    if(octaveView != null) noteContext += octaveView.getOctave();

                    if(note.isTieEnd()) noteContext += "-";

                    BeamView beamView = (BeamView)note.findViewById(R.id.beam_view);
                    if(beamView != null) noteContext += beamView.getDuration();

                    DottedView dottedView = (DottedView)note.findViewById(R.id.dotted_view);
                    if(dottedView != null) noteContext += dottedView.getDot();

                    if(note.isTieStart()) noteContext += "-";

                    WordView wordView = (WordView)measure.findViewById(k + wordStartId);
                    if(wordView != null) noteContext += wordView.getWord();

                    noteContext += " ";
                    musicString += noteContext;
                }

                musicString += "| ";
            }
        }

        pattern = new Pattern(musicString);
        try { pattern.save(new File(txtDir + scoreName)); }
        catch (IOException e) { Log.e(LOG_TAG, "IOE"); }
    }

    /**
     * Recommend group include number picker, rhyme, recommend button, and complete button
     */
    private void setUpLyricRecommendGroup() {
        /* LYRIC NUMBER PICKER */
        lyricNumberPicker = (NumberPicker)findViewById(R.id.lyric_number_picker);
        lyricNumberPicker.setMaxValue(5);
        lyricNumberPicker.setMinValue(1);
        lyricNumberPicker.setValue(2);

        rhymeSpinner = (Spinner)findViewById(R.id.lyric_rhyme_spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.rhymes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rhymeSpinner.setAdapter(adapter);

        /* RECOMMEND BUTTON */
        recommendButton = (Button)findViewById(R.id.recommend_button);
        recommendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lyricInputACTextView.getText().length() == 0) return;
                RecommendTask recommendTask = new RecommendTask();
                String rhyme = rhymeSpinner.getSelectedItem().toString().equals("無") ? "": rhymeSpinner.getSelectedItem().toString();
                String[] args = {lyricInputACTextView.getText().toString(), Integer.toString(lyricNumberPicker.getValue()), rhyme};
                recommendTask.execute(args);
            }
        });

        /* COMPLETE BUTTON */
        completeButton = (Button)findViewById(R.id.complete_button);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PartViewGroup.lyricEditStartMeasure != null) PartViewGroup.saveWordsIntoWordView();

                /* hide lyric input text view and recommend button */
                setLyricRecommendGroupVisibility(false);

                /* close keyboard */
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ShowScoreActivity.lyricInputACTextView.getWindowToken(), 0);

            }
        });

        /* LYRIC INPUT TEXT VIEW */
        lyricInputACTextView = (AutoCompleteTextView)findViewById(R.id.lyric_input_text_view);
        lyricInputACTextView.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);

        wordAdapter = new WordAdapter(this, android.R.layout.simple_list_item_1, suggestWords);
        lyricInputACTextView.setAdapter(wordAdapter);
        lyricInputACTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = inputSentence + wordAdapter.getItem(position);
                lyricInputACTextView.setText(text);
                lyricInputACTextView.setSelection(lyricInputACTextView.getText().length());
            }
        });

        lyricInputACTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (PartViewGroup.lyricEditStartMeasure != null) PartViewGroup.saveWordsIntoWordView();

                    /* hide lyric input text view and recommend button */
                    setLyricRecommendGroupVisibility(false);

                    /* close keyboard */
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(ShowScoreActivity.lyricInputACTextView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        setLyricRecommendGroupVisibility(false);
    }

    /**
     * Score keyboard include a note group view, new a note, and delete a note
     */
    private void setUpEditScoreKeyboard() {
        keyboard = (RelativeLayout)findViewById(R.id.edit_score_keyboard);

        accidentalButton = (AccidentalButton)findViewById(R.id.edit_accident_button);
        numberButton = (NumberButton)findViewById(R.id.edit_number_button);
        beamButton = (BeamButton)findViewById(R.id.edit_beam_button);
        dottedButton = (DottedButton)findViewById(R.id.edit_dot_button);
        topOctaveButton = (OctaveButton)findViewById(R.id.edit_top_octave_button);
        topOctaveButton.setPosition(true);
        bottomOctaveButton = (OctaveButton)findViewById(R.id.edit_bottom_octave_button);
        bottomOctaveButton.setPosition(false);

        final NewNoteButton newNoteButton = (NewNoteButton)findViewById(R.id.new_note_button);
        newNoteButton.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {

                // Create a new ClipData.
                // This is done in two steps to provide clarity. The convenience method
                // ClipData.newPlainText() can create a plain text ClipData in one step.

                // Create a new ClipData.Item from the ImageView object's tag
                ClipData.Item item = new ClipData.Item(v.getTag().toString());

                // Create a new ClipData using the tag as a label, the plain text MIME type, and
                // the already-created item. This will create a new ClipDescription object within the
                // ClipData, and set its MIME type entry to "text/plain"
                ClipData dragData = new ClipData(v.getTag().toString(), new String[]{ ClipDescription.MIMETYPE_TEXT_PLAIN } , item);

                // Instantiates the drag shadow builder.
                View.DragShadowBuilder myShadow = new MyDragShadowBuilder(newNoteButton);

                // Starts the drag

                v.startDrag(dragData,  // the data to be dragged
                        myShadow,  // the drag shadow builder
                        null,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );

                return false;
            }
        });

        keyboard.setVisibility(View.GONE);
    }

    public static void setLyricRecommendGroupVisibility(boolean visible) {
        if(visible) {
            lyricNumberPicker.setVisibility(View.VISIBLE);
            lyricInputACTextView.setVisibility(View.VISIBLE);
            lyricInputACTextView.setInputType(InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
            recommendButton.setVisibility(View.VISIBLE);
            completeButton.setVisibility(View.VISIBLE);
            rhymeSpinner.setVisibility(View.VISIBLE);
        }
        else {
            lyricNumberPicker.setVisibility(View.GONE);
            lyricInputACTextView.setVisibility(View.GONE);
            recommendButton.setVisibility(View.GONE);
            completeButton.setVisibility(View.GONE);
            rhymeSpinner.setVisibility(View.GONE);
        }
    }

    public String getScoreName() {
        return scoreName;
    }

    class RecommendTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... arg) {
            inputSentence = arg[0];
            String inputNumber = arg[1];
            String inputRhyme = arg[2];

            long startTime = System.currentTimeMillis();
            String url = "http://140.117.71.221/auditor/stest/client.php";
            String webRequestResult = ""; // web request result
            ArrayList<String> returnTagsList = new ArrayList<>();

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("sentence", inputSentence));
            nameValuePairs.add(new BasicNameValuePair("number", inputNumber));
            nameValuePairs.add(new BasicNameValuePair("rhyme", inputRhyme));
            InputStream is = null;

            //http post
            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }
            catch(Exception e){
                Log.e(LOG_TAG, "Error in http connection " + e.toString());
            }

            //convert response to string
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();

                webRequestResult = sb.toString();
                Log.e(LOG_TAG, webRequestResult);
            }
            catch(Exception e){
                Log.e(LOG_TAG, "Error converting result " + e.toString());
            }

            double dbQueryTime = -1;
            //parse json data
            try{
                JSONArray jArray = new JSONArray(webRequestResult);

                for(int i = 0; i < jArray.length() - 1; i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Log.i(LOG_TAG, "id: " + json_data.getInt("id") + ", tag: " + json_data.getString("tag"));
                    returnTagsList.add(json_data.getString("tag"));
                }
                dbQueryTime = jArray.getJSONObject(jArray.length()).getDouble("db_query_time");
            }
            catch(JSONException e){
                Log.e(LOG_TAG, "Error parsing data " + e.toString());
            }

            long finishTime = System.currentTimeMillis();
            double duration = (finishTime - startTime) / 1000d;
            Log.i(LOG_TAG, "number: " + inputNumber + ", rhyme: " + inputRhyme);
            Log.i(LOG_TAG, "database query time: " + dbQueryTime);
            Log.e(LOG_TAG, "total recommend time: " + duration + " seconds.");

            return returnTagsList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> a) {
            super.onPostExecute(a);
            suggestWords.clear();
            suggestWords.addAll(a);
            wordAdapter.notifyDataSetChanged();
            lyricInputACTextView.showDropDown();
        }
    }

    class MusicController extends RelativeLayout {
        private PlayButton playButton;
        private SeekBar seekBar;

        public MusicController(Context context) {
            super(context);
            init();
        }

        public MusicController(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public MusicController(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            this.setPadding((int) (getResources().getDimension(R.dimen.controller_container_padding)),
                    (int) (getResources().getDimension(R.dimen.controller_container_padding)),
                    (int) (getResources().getDimension(R.dimen.controller_container_padding)),
                    (int) (getResources().getDimension(R.dimen.controller_container_padding)));
            RelativeLayout.LayoutParams lp =
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            this.setBackgroundColor(getResources().getColor(R.color.AuditorColorPrimaryDark));
            this.setLayoutParams(lp);

            seekBar = new SeekBar(ShowScoreActivity.this);
            seekBar.setId(R.id.seek_bar);
            RelativeLayout.LayoutParams slp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            slp.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.play_button);
            seekBar.setLayoutParams(slp);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    musicPlayer.seekTo(seekBar.getProgress());
                }
            });

            playButton = new PlayButton(ShowScoreActivity.this);
            playButton.setId(R.id.play_button);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (musicPlayer.isPlaying()) { // do pause
                        musicPlayer.pause();
                        playButton.setPlay(true);
                        playButton.invalidate();
                        playbackPaused = true;

                        updateSeekBarHandler.removeCallbacks(updateSeekBarTask);
                    } else { // do play
                        if (playbackPaused) {
                            musicPlayer.start();
                        } else {
                            playSong();
                        }

                        playButton.setPlay(false);
                        playButton.invalidate();
                        playbackPaused = false;
                        updateSeekBarHandler.post(updateSeekBarTask);
                    }
                }
            });
            RelativeLayout.LayoutParams plp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            plp.addRule(RelativeLayout.BELOW, R.id.seek_bar);
            plp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            playButton.setLayoutParams(plp);

            this.addView(playButton);
            this.addView(seekBar);
            rootView.addView(this);
        }
    }

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {
        private static Drawable shadow;

        public MyDragShadowBuilder(View v) {
            super(v);
            shadow = new ColorDrawable(Color.LTGRAY);
        }

        @Override
        public void onProvideShadowMetrics (Point size, Point touch) {
            int width, height;

            width = getView().getWidth() / 2;
            height = getView().getHeight() / 2;

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height / 2);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {

            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }
    }

    private void playSong() {
        musicPlayer.reset();
        String songPath = midiDir + scoreName.substring(0, scoreName.length() - 4) + ".mid";

        try{
            musicPlayer.setDataSource(songPath);
            musicPlayer.prepareAsync();
            musicPlayer.start();
        } catch(Exception e){
            Log.e("MediaPlayer", "Error setting data source", e);
        }
    }

    private void initPlayer() {
        musicPlayer = new MediaPlayer();
        musicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        musicPlayer.setVolume(1, 1);

        updateSeekBarTask = new Runnable() {
            @Override
            public void run() {
                musicController.seekBar.setProgress(musicPlayer.getCurrentPosition());
                updateSeekBarHandler.postDelayed(this, 300);
            }
        };

        musicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int songTime = musicPlayer.getDuration();
                Log.e(LOG_TAG, "song time: " + songTime);
                musicController.seekBar.setMax(songTime);

                updateSeekBarHandler.removeCallbacks(updateSeekBarTask);
                updateSeekBarHandler.post(updateSeekBarTask);

                musicController.playButton.setPlay(false);
                musicController.playButton.invalidate();
                playbackPaused = false;
            }
        });

        musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                musicController.playButton.setPlay(true);
                musicController.playButton.invalidate();
                updateSeekBarHandler.removeCallbacks(updateSeekBarTask);
                musicController.seekBar.setProgress(0);
                playbackPaused = true;
            }
        });

        musicPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
                Log.e(LOG_TAG, "Music service on error and reset!");
                return false;
            }
        });
    }

    // TODO show what note is playing
}