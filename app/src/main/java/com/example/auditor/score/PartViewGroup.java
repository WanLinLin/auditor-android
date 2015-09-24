package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;

import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/8/20.
 * A part contains numbers of measures.
 */
public class PartViewGroup extends RelativeLayout {
    private static final String LOG_TAG = PartViewGroup.class.getName();
    private static final boolean SHOW_TIE_VIEW_COLOR = false;
    private static int tieStrokeWidth;
    private static GestureDetector gestureDetector;
    private Context context;
    private Paint mPaint;
    private ArrayList<Pair<Integer, String>> tieInfo;
    private TieViewGroup tieViewGroup;
    private String originalLyrics = "";
    private MeasureViewGroup editMeasure;
    private boolean lyricEditing;

    public static int barStrokeWidth;
    public static int tieViewHeight;

    public PartViewGroup(Context context) {
        super(context);
        this.context = context;
        tieInfo = new ArrayList<>();
        gestureDetector = new GestureDetector(context, new mGestureDetector());

        tieViewHeight = ShowScoreActivity.NoteChildViewDimension.TIE_VIEW_HEIGHT;
        tieStrokeWidth = ShowScoreActivity.NoteChildViewDimension.TIE_STROKE_WIDTH;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        tieViewGroup = new TieViewGroup(context);
        tieViewGroup.setId(R.id.tie_view_group);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                tieViewHeight);
        tieViewGroup.setLayoutParams(lp);
        this.addView(tieViewGroup);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        for(int i = 0; i < getChildCount(); i++) {
            getChildAt(i).requestLayout();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void saveWordsIntoWordView() {
        int i = 0;

        EditText et = (EditText)editMeasure.findViewById(R.id.input_text_view);

        for (int w = 0; w < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; w++) {
            WordView wordView = (WordView) editMeasure.findViewById(w + ShowScoreActivity.wordStartId);
            if (wordView == null)
                break;

            if(i < et.getText().length()) {
                wordView.setWord(et.getText().toString().substring(i, i + 1));
                i++;
            }
        }

        editMeasure.removeView(et);
        editMeasure = null;
    }

    public void printBarView(int measureViewGroupId) {
        BarView barView = new BarView(context);
        RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.BELOW, R.id.tie_view_group);
        if (measureViewGroupId == -1)
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        else
            lp.addRule(RelativeLayout.RIGHT_OF, measureViewGroupId);
        barView.setLayoutParams(lp);
        this.addView(barView);
    }

    public void printMeasure(int i) {
        int measureViewGroupId = i + ShowScoreActivity.measureStartId;
        MeasureViewGroup measureViewGroup = new MeasureViewGroup(context);

        RelativeLayout.LayoutParams rlp =
                new LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.BELOW, R.id.tie_view_group);
        if(measureViewGroupId > ShowScoreActivity.measureStartId)
            rlp.addRule(RIGHT_OF, measureViewGroupId - 1);
        else
            rlp.addRule(ALIGN_PARENT_LEFT);
        measureViewGroup.setLayoutParams(rlp);
        measureViewGroup.setId(measureViewGroupId);
        this.addView(measureViewGroup);
    }

    public void addTieInfo(Pair<Integer, String> tieInfo) {
        this.tieInfo.add(tieInfo);
    }

    public void printTieView() {
        int rectStartX = 0;

        for(Pair<Integer, String> p: tieInfo) {
            int x = p.first;
            String tie = p.second;


            if(tie.equals("start")) {
                rectStartX = x;
            }
            else if(tie.equals("end")) {
                int rectWidth = x - rectStartX;

                tieStrokeWidth = ShowScoreActivity.NoteChildViewDimension.TIE_STROKE_WIDTH;
                tieViewHeight = ShowScoreActivity.NoteChildViewDimension.TIE_VIEW_HEIGHT;

                RectF rectF = new RectF(tieStrokeWidth/2, tieViewHeight/2, rectWidth + tieStrokeWidth/2, tieViewHeight*3/2);
                TieView tieView = new TieView(context, rectF);
                if(SHOW_TIE_VIEW_COLOR)
                    tieView.setBackgroundColor(Color.RED);

                RelativeLayout.LayoutParams rlp =
                        new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                rlp.leftMargin = rectStartX - tieStrokeWidth/2;

                tieView.setLayoutParams(rlp);
                tieViewGroup.addView(tieView);
            }
        }
    }

    public void clearTieInfo() {
        this.tieInfo.clear();
    }

    private class mGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            // single touch down on lyric area
            if (y > getHeight() - ShowScoreActivity.NoteChildViewDimension.WORD_VIEW_HEIGHT) {
                // lyric edit mode is on
                if(ShowScoreActivity.lyricEditMode) {
                    if (lyricEditing) {
                        saveWordsIntoWordView();
                        lyricEditing = false;
                    }

                    lyricEditing = true;

                    /* i=0: tieViewGroup, i=odd: barView, i=even: measureViewGroup */
                    for (int i = 2; i < getChildCount(); i += 2) {
                        if (x > getChildAt(i).getLeft() && x < getChildAt(i).getRight()) {
                            originalLyrics = "";
                            editMeasure = (MeasureViewGroup) findViewById((i / 2 - 1) + ShowScoreActivity.measureStartId);

                            /* find words */
                            for (int w = 0; w < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; w++) {
                                WordView wordView = (WordView) editMeasure.findViewById(w + ShowScoreActivity.wordStartId);
                                if (wordView == null)
                                    break;

                                originalLyrics += wordView.getWord();
                                wordView.setWord("");
                            }

                            EditText et = new EditText(getContext());
                            et.setId(R.id.input_text_view);
                            et.setInputType(InputType.TYPE_CLASS_TEXT);
                            et.setText(originalLyrics);
                            RelativeLayout.LayoutParams lp =
                                    new RelativeLayout.LayoutParams(
                                            editMeasure.getWidth() - ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH,
                                            (int) (ShowScoreActivity.NoteChildViewDimension.WORD_VIEW_HEIGHT * 1.4));
                            lp.addRule(RelativeLayout.RIGHT_OF, R.id.bar_width_view);
                            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            et.setLayoutParams(lp);
                            et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                                        lyricEditing = false;
                                        EditText et = (EditText) editMeasure.findViewById(R.id.input_text_view);
                                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                                        saveWordsIntoWordView();
                                        return true;
                                    }
                                    return false;
                                }
                            });

                            editMeasure.addView(et);
                            et.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
                        }
                    }
                }
                // lyric edit mode is off
                else {
                    if (editMeasure != null) {
                        lyricEditing = false;

                        EditText et = (EditText) editMeasure.findViewById(R.id.input_text_view);
                        if(et != null) {
                            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

                            saveWordsIntoWordView();
                        }
                    }
                }
            }

            // single touch down on note area
            else if(y < getHeight() - ShowScoreActivity.NoteChildViewDimension.WORD_VIEW_HEIGHT) {
                if(ShowScoreActivity.noteEditMode) {

                }
                else {

                }
            }

            ShowScoreActivity.mx = e.getRawX(); // x position relative to screen
            ShowScoreActivity.my = e.getRawY(); // y position relative to screen
            return true; // onDown must return true
        }

        @Override
        public void onLongPress(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            // long press on lyric view
            if(y > getHeight() - ShowScoreActivity.NoteChildViewDimension.WORD_VIEW_HEIGHT) {
                ShowScoreActivity.lyricEditMode = !ShowScoreActivity.lyricEditMode;
                if(ShowScoreActivity.lyricEditMode) {
                    for (int i = 2; i < getChildCount(); i += 2) {
                        if (x > getChildAt(i).getLeft() && x < getChildAt(i).getRight()) {
                            editMeasure = (MeasureViewGroup) findViewById((i / 2 - 1) + ShowScoreActivity.measureStartId);
                            // TODO add a black mask on the whole score view, except the edit measure
                        }
                    }
                }
            }
            else { // long press on note view
                ShowScoreActivity.measureEditMode = !ShowScoreActivity.measureEditMode;
            }

            if(ShowScoreActivity.lyricEditMode) {
                ShowScoreActivity.actionBar.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
                ShowScoreActivity.actionBar.setTitle("LyricEditMode");

            }
            else if(ShowScoreActivity.measureEditMode) {
                ShowScoreActivity.actionBar.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
                ShowScoreActivity.actionBar.setTitle("MeasureEditMode");
            }
            else {
                ShowScoreActivity.actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                ShowScoreActivity.actionBar.setTitle(ShowScoreActivity.scoreName);
            }
        }
    }

    private class BarView extends View {
        private int width;
        private int height;

        public BarView(Context context) {
            super(context);

            barStrokeWidth = ShowScoreActivity.NoteChildViewDimension.BAR_STROKE_WIDTH;
            width = ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;
            height = ShowScoreActivity.noteHeight;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            barStrokeWidth = ShowScoreActivity.NoteChildViewDimension.BAR_STROKE_WIDTH;
            width = ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;
            height = ShowScoreActivity.noteHeight;
            setMeasuredDimension(width, height);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(barStrokeWidth);
            canvas.drawLine(width / 2, 0, width / 2, height, mPaint);
        }
    }

    private class TieView extends View {
        private RectF rectF;

        public TieView(Context context, RectF rectF) {
            super(context);
            this.rectF = rectF;

            tieStrokeWidth = ShowScoreActivity.NoteChildViewDimension.TIE_STROKE_WIDTH;
            tieViewHeight = ShowScoreActivity.NoteChildViewDimension.TIE_VIEW_HEIGHT;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            tieStrokeWidth = ShowScoreActivity.NoteChildViewDimension.TIE_STROKE_WIDTH;
            tieViewHeight = ShowScoreActivity.NoteChildViewDimension.TIE_VIEW_HEIGHT;

            setMeasuredDimension(Math.round(rectF.width() + tieStrokeWidth), tieViewHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(tieStrokeWidth);
            canvas.drawArc(rectF, 180, 180, false, mPaint);
        }
    }

    public ArrayList<Pair<Integer, String>> getTieInfo() {
        return tieInfo;
    }

    public TieViewGroup getTieViewGroup() {
        return tieViewGroup;
    }

    // TODO 推薦詞可選字數
}