package com.example.auditor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.auditor.score.AccidentalView;
import com.example.auditor.score.BeamView;
import com.example.auditor.score.BlackMask;
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
import org.jfugue.Pattern;
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
    private static final String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";

    public static ScoreViewGroup score;
    private RelativeLayout scoreContainer;
    private Pattern pattern;
    private NumberedMusicalNotationParser numberedMusicalNotationParser;

    /* using for two dimension scroll */
    private VScrollView vScroll;
    private HScrollView hScroll;
    public static float mx;
    public static float my;

    public static ActionBar actionBar;
    public static Menu menu;
    public static String scoreName;

    /* View id index */
    public static final int partMaxNumber = 5000;
    public static final int partStartId = 10001;
    public static final int measureStartId = 201;
    public static final int wordStartId = 101;
    public static final int noteStartId = 1;

    /* View size */
    public static int defaultNoteHeight = 300;
    public static int noteHeight = defaultNoteHeight;
    public static int noteWidth = noteHeight / 3 * 2;

    /* Edit parameter */
    public static boolean partEditMode;
    public static boolean measureEditMode;
    public static boolean noteEditMode;
    public static boolean lyricEditMode;

    /* lyric recommend views */
    public static Button recommendButton;
    public static Button completeButton;
    public static AutoCompleteTextView lyricInputACTextView;

    private ProgressDialog dialog;
    private ArrayList<String> suggestWords = new ArrayList<>();
    private WordAdapter wordAdapter;

    private String inputSentence = "";
    private String inputNumber = "2";
    private String inputRhyme = "一";

    private static final int secondsPerMinute = 60; // seconds per minutes
    private int beatsPerMinute = 90; // bits per minute, speed
    private int beatsPerMeasure = 4; // 4 beats per bar
    private int beatUnit = 4; // quarter notes per bit
    private int measureDuration = secondsPerMinute / beatsPerMinute * beatsPerMeasure;
    public static int screenWidth;

    public static RelativeLayout rootView;
    public static int screenHeight;

//    private ScaleGestureDetector mScaleDetector;
    public static float mScaleFactor = 1.f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        File scoreDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/score");
        scoreDir.mkdirs();

        partEditMode = false;
        measureEditMode = false;
        noteEditMode = false;
        lyricEditMode = false;

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
        screenWidth = displaymetrics.widthPixels;

        // reset parameters
        mScaleFactor = 1.f;
        setDimensions();

        vScroll = (VScrollView) findViewById(R.id.vScroll);
        hScroll = (HScrollView) findViewById(R.id.hScroll);
        scoreContainer = (RelativeLayout)findViewById(R.id.score_container);
        rootView = (RelativeLayout)findViewById(R.id.activity_show_score);

        scoreName = getIntent().getStringExtra("score name");

        try {
            pattern = Pattern.loadPattern(new File(auditorDir + scoreName + ".txt"));

            numberedMusicalNotationParser =
                    new NumberedMusicalNotationParser(this, pattern.getMusicString());

            numberedMusicalNotationParser.parse();
            score = numberedMusicalNotationParser.getScoreViewGroup();
            scoreContainer.addView(score);
        }
        catch (IOException e) {
            Log.e(getClass().getName(), e.getMessage());
        }

        /* set action bar title */
        actionBar = getSupportActionBar();
        actionBar.setTitle(scoreName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setUpLyricRecommend();
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
            case android.R.id.home:
                if(measureEditMode || lyricEditMode) {
                    lyricEditMode = false;
                    measureEditMode = false;

                    /* hide lyric input text view and recommend button */
                    lyricInputACTextView.setVisibility(View.GONE);
                    recommendButton.setVisibility(View.GONE);
                    completeButton.setVisibility(View.GONE);

                    /* close keyboard */
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(ShowScoreActivity.lyricInputACTextView.getWindowToken(), 0);

                    if(PartViewGroup.lyricEditStartMeasure != null) PartViewGroup.saveWordsIntoWordView();

                    BlackMask b = (BlackMask)ShowScoreActivity.rootView.findViewById(R.id.black_mask);
                    if(b != null) ShowScoreActivity.rootView.removeView(b);

                    actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                    actionBar.setTitle(ShowScoreActivity.scoreName);
                    ShowScoreActivity.menu.findItem(R.id.action_zoom_in).setVisible(true);
                    ShowScoreActivity.menu.findItem(R.id.action_zoom_out).setVisible(true);
                }
                else {
                    Intent intent = new Intent(this, ScoreFileListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float curX;
        float curY;

//        if(event.getY() < scoreContainer.getBottom()) {
//
//        }

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
        try { pattern.savePattern(new File(auditorDir + scoreName + ".txt")); }
        catch (IOException e) { Log.e(LOG_TAG, "IOE"); }
    }

    private void setUpLyricRecommend() {
        recommendButton = new Button(this);
        recommendButton.setVisibility(View.GONE);
        recommendButton.setId(R.id.recommend_button);
        recommendButton.setText("推薦歌詞");
        recommendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lyricInputACTextView.getText().length() == 0) return;
                RecommendTask recommendTask = new RecommendTask();
                String[] args = {lyricInputACTextView.getText().toString(), inputNumber, inputRhyme};
                recommendTask.execute(args);
            }
        });

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        recommendButton.setLayoutParams(lp);
        rootView.addView(recommendButton);

        completeButton = new Button(this);
        completeButton.setVisibility(View.GONE);
        completeButton.setId(R.id.complete_button);
        completeButton.setText("完成句子");
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PartViewGroup.lyricEditStartMeasure != null) PartViewGroup.saveWordsIntoWordView();
            }
        });

        RelativeLayout.LayoutParams clp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        clp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        completeButton.setLayoutParams(clp);
        rootView.addView(completeButton);


        lyricInputACTextView = new AutoCompleteTextView(this);
        lyricInputACTextView.setVisibility(View.GONE);
        RelativeLayout.LayoutParams elp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        elp.bottomMargin = 120;
        elp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lyricInputACTextView.setLayoutParams(elp);
        lyricInputACTextView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        rootView.addView(lyricInputACTextView);

        wordAdapter = new WordAdapter(this, android.R.layout.simple_list_item_1, suggestWords);
        lyricInputACTextView.setAdapter(wordAdapter);
        lyricInputACTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lyricInputACTextView.setText(inputSentence + wordAdapter.getItem(position));
                lyricInputACTextView.setSelection(lyricInputACTextView.getText().length());
            }
        });

        lyricInputACTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (PartViewGroup.lyricEditStartMeasure != null) PartViewGroup.saveWordsIntoWordView();

                    /* hide lyric input text view and recommend button */
                    ShowScoreActivity.lyricInputACTextView.setVisibility(View.GONE);
                    ShowScoreActivity.recommendButton.setVisibility(View.GONE);
                    ShowScoreActivity.completeButton.setVisibility(View.GONE);

                    /* close keyboard */
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(ShowScoreActivity.lyricInputACTextView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    class RecommendTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... arg) {
            inputSentence = arg[0];
            inputNumber = arg[1];
            inputRhyme = arg[2];

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

            double dbQueryTime;
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

    // TODO move part view group gesture detector to show score activity
}
