package com.example.auditor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.auditor.score.MeasureViewGroup;
import com.example.auditor.score.NoteViewGroup;
import com.example.auditor.score.NumberedMusicalNotationParser;
import com.example.auditor.score.PartViewGroup;
import com.example.auditor.score.ScoreViewGroup;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShowScoreActivity extends ActionBarActivity {
    private static final String LOG_TAG = ShowScoreActivity.class.getName();
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private String scoreName;

    // two dimension scroll view
    private ScoreViewGroup score;
    private RelativeLayout scoreContainer;
    private Pattern pattern;
    private NumberedMusicalNotationParser numberedMusicalNotationParser;

    private VScrollView vScroll;
    private HScrollView hScroll;
    public static float mx;
    public static float my;

    public static ActionBar actionBar;

    public static final int partStartId = 10001;
    public static final int measureStartId = 201;
    public static final int wordStartId = 101;
    public static final int noteStartId = 1;

    public static int defaultNoteHeight = 300;
    public static int noteHeight = defaultNoteHeight;
    public static int noteWidth = noteHeight / 3 * 2;

    public static boolean partEditMode = false;
    public static boolean measureEditMode = false;
    public static boolean noteEditMode = false;
    public static boolean lyricEditMode = false;

    private ProgressDialog dialog;
    private AutoCompleteTextView tv;
    private ArrayList<String> suggestWords = new ArrayList<>();
    private WordAdapter wordAdapter;
    private String inputSentence;

    // pinch to zoom
//    private ScaleGestureDetector mScaleDetector;
    public static float mScaleFactor = 1.f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_score);

        File scoreDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/score");
        scoreDir.mkdirs();

        // reset parameters
        mScaleFactor = 1.f;
        setDimensions();

        actionBar = getSupportActionBar();

        Intent intent = getIntent();
        scoreName  = intent.getStringExtra("score name");
//        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());

        vScroll = (VScrollView) findViewById(R.id.vScroll);
        hScroll = (HScrollView) findViewById(R.id.hScroll);
        scoreContainer = (RelativeLayout)findViewById(R.id.score_container);

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

        RelativeLayout root = (RelativeLayout)findViewById(R.id.activity_show_score);
        Button bt = new Button(this);
        bt.setText("Request");
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecommendTask recommendTask = new RecommendTask();
                recommendTask.execute(tv.getText().toString());
            }
        });

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bt.setLayoutParams(lp);
        root.addView(bt);

        tv = new AutoCompleteTextView(this);
        RelativeLayout.LayoutParams elp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        elp.bottomMargin = 120;
        elp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tv.setLayoutParams(elp);
        root.addView(tv);

        suggestWords.add("fuck");
        wordAdapter = new WordAdapter(this, android.R.layout.simple_list_item_1, suggestWords);
        tv.setAdapter(wordAdapter);
        tv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                tv.setText(inputSentence + wordAdapter.getItem(position));
                tv.setSelection(tv.getText().length());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_score, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_zoom_in:
                if(mScaleFactor < 3) {
                    mScaleFactor += 0.5;
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
                if (mScaleFactor > 0.5) {
                    mScaleFactor -= 0.5;
                    zoom();

                    if(mScaleFactor == 0.5) {
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

                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try{
                            FileOutputStream out = null;
                            try {
                                out = new FileOutputStream(auditorDir + "score/" + scoreName + ".png");
                                getBitmapFromView(scoreContainer).compress(Bitmap.CompressFormat.PNG, 100, out);
                                // PNG is a lossless format, the compression factor (100) is ignored
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (out != null) {
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        finally {
                            dialog.dismiss();
                        }
                    }
                }).start();

                return true;
            default:
                return super.onOptionsItemSelected(item);
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

//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        @Override
//        public boolean onScale(ScaleGestureDetector detector) {
//            mScaleFactor *= detector.getScaleFactor();
//
//            // Don't let the object get too small or too large.
//            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));
//
//            if(mScaleFactor % 0.5 <= 0.03) {
//                setDimensions();
//
//                for (int i = 0; i < 1; i++) {
//                    PartViewGroup part = (PartViewGroup) score.findViewById(i + partStartId);
//                    if (part == null)
//                        break;
//                    part.requestLayout();
//                }
//            }
//
//            return true;
//        }
//    }

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

    public void zoom() {
        setDimensions();

        for (int i = 0; i < 1; i++) {
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
        String musicString = "C4你";

        pattern = new Pattern(musicString);
        try {
            pattern.savePattern(new File(auditorDir + "test" + ".txt"));
        }
        catch (IOException e) {
            Log.e(getClass().getName(), "IOE");
        }
    }

    public static Bitmap getBitmapFromView(View view) {
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

    private class RecommendTask extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... sentences) {

//            Log.e(LOG_TAG, sentences[0]);

//            JiebaSegmenter segmenter = new JiebaSegmenter(ShowScoreActivity.this);
//            String sentence = sentences[0];
//
//            List<SegToken> list = segmenter.process(sentence, JiebaSegmenter.SegMode.SEARCH);
//
//            for(SegToken s : list) {
//                Log.e(LOG_TAG, s.word);
//            }
            long startTime = System.currentTimeMillis();

            String result = "";
            String url = "http://140.117.71.221/auditor/stest/client.php";

            String rhyme = "ㄧ";
            inputSentence = sentences[0];


            ArrayList<String> resultList = new ArrayList<>();;

            //the year data to send
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("rhyme", rhyme));
            nameValuePairs.add(new BasicNameValuePair("sentence", inputSentence));

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

                result = sb.toString();
//                Log.e(LOG_TAG, result);
            }
            catch(Exception e){
                Log.e(LOG_TAG, "Error converting result " + e.toString());
            }

            //parse json data
            try{
                JSONArray jArray = new JSONArray(result);
                int count = 0;

//                for(int i = 0; i < jArray.length(); i++) {
//                    JSONObject json_data = jArray.getJSONObject(i);
//                    Log.i(LOG_TAG, "number: " + json_data.getInt("number") + ", tag: " + json_data.getString("tag"));
//                }

                for(int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Log.i(LOG_TAG, "id: " + json_data.getInt("id") + ", tag: " + json_data.getString("tag"));
                    resultList.add(json_data.getString("tag"));
                }
            }
            catch(JSONException e){
                Log.e(LOG_TAG, "Error parsing data " + e.toString());
            }

            long finishTime = System.currentTimeMillis();
            double duration = (finishTime - startTime) / 1000d;
            Log.e(LOG_TAG, "total recommend time: " + duration + " seconds.");

            return resultList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> a) {
            super.onPostExecute(a);
            suggestWords.clear();
            suggestWords.addAll(a);
            wordAdapter.notifyDataSetChanged();
            tv.showDropDown();
        }
    }
}
