package com.example.auditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;
import com.example.auditor.convert.NumberedMusicalNotationParser;

import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/8/20.
 * A part contains numbers of measures.
 */
public class PartViewGroup extends RelativeLayout {
    private static final String LOG_TAG = PartViewGroup.class.getName();
    private static int tieStrokeWidth;
    private static GestureDetector gestureDetector;
    private ShowScoreActivity showScoreActivity;

    private Paint mPaint;
    private ArrayList<Pair<Integer, String>> tieInfo;
    private TieViewGroup tieViewGroup;

    private static PartViewGroup clickPart;
    private static MeasureViewGroup clickMeasure;
    private static WordView clickWord;

    public static WordView lyricEditStartWord = null;
    private static boolean lyricEditing;

    private static ImageView arrow;

    public static int barStrokeWidth;
    public static int tieViewHeight;

    public PartViewGroup(Context context) {
        super(context);
        init();
    }

    public PartViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PartViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PartViewGroup(ShowScoreActivity showScoreActivity) {
        super(showScoreActivity);
        this.showScoreActivity = showScoreActivity;
        init();
    }

    private void init() {
        tieInfo = new ArrayList<>();
        gestureDetector = new GestureDetector(showScoreActivity, new mGestureDetector());

        tieViewHeight = ShowScoreActivity.NoteChildViewDimension.TIE_VIEW_HEIGHT;
        tieStrokeWidth = ShowScoreActivity.NoteChildViewDimension.TIE_STROKE_WIDTH;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        tieViewGroup = new TieViewGroup(showScoreActivity);
        tieViewGroup.setId(R.id.tie_view_group);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                tieViewHeight);
        tieViewGroup.setLayoutParams(lp);
        this.addView(tieViewGroup);

        arrow = new ImageView(getContext());
        arrow.setId(R.id.lyric_arrow);
        arrow.setImageResource(R.drawable.lyric_arrow);
        arrow.setEnabled(false);

        lyricEditing = false;
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
        if(ShowScoreActivity.lyricEditMode) clickPart = this;
        return gestureDetector.onTouchEvent(event);
    }

    public void printBarView(int measureViewGroupId) {
        BarView barView = new BarView(getContext());
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
        MeasureViewGroup measureViewGroup = new MeasureViewGroup(getContext());

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
                TieView tieView = new TieView(getContext(), rectF);

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
            ShowScoreActivity.mx = e.getRawX(); // x position relative to screen
            ShowScoreActivity.my = e.getRawY(); // y position relative to screen
            return true; // onDown must return true
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            /* single touch down on lyric area */
            if (y > getHeight() - ShowScoreActivity.NoteChildViewDimension.WORD_VIEW_HEIGHT) {
                if(ShowScoreActivity.lyricEditMode) {
                    if(lyricEditing) return true; // is editing, do nothing

                    searchClickWordView(x);
                    if(clickWord == null) return true; // click on not word view area

                    NoteViewGroup note = (NoteViewGroup)clickMeasure.findViewById(
                            clickWord.getId() - ShowScoreActivity.wordStartId + ShowScoreActivity.noteStartId);
                    NumberView numberView = (NumberView)note.findViewById(R.id.number_view);

                    // if the word is empty, note is not tie end, and the note is not Rest or duration bar
                    if(clickWord.getWord().equals("") && !note.isTieEnd() && !"R-".contains(numberView.getNote())) {
                        lyricEditStartWord = clickWord;
                        addArrow();

                        /* show editNoteKeyboard */
                        lyricEditing = true;
                        ShowScoreActivity.lyricInputACTextView.setFocusableInTouchMode(true);
                        ShowScoreActivity.lyricInputACTextView.requestFocus();
                        ShowScoreActivity.setLyricRecommendGroupVisibility(true);
                        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(ShowScoreActivity.lyricInputACTextView, InputMethodManager.SHOW_IMPLICIT);

                        ShowScoreActivity.mx = e.getRawX(); // x position relative to screen
                        ShowScoreActivity.my = e.getRawY(); // y position relative to screen
                        return true;
                    }
                    else if(clickWord.getWord().equals("") && (note.isTieEnd() || "R-".contains(numberView.getNote())) ) {
                        ShowScoreActivity.mx = e.getRawX(); // x position relative to screen
                        ShowScoreActivity.my = e.getRawY(); // y position relative to screen
                        return true;
                    }

                    findLyricEditStartPosition();

                    String originalLyrics = collectWords();

                    if(!originalLyrics.isEmpty() && originalLyrics.charAt(originalLyrics.length() - 1) == NumberedMusicalNotationParser.sentenceEndTag)
                        originalLyrics = originalLyrics.substring(0, originalLyrics.length() - 1);

                    ShowScoreActivity.lyricInputACTextView.setText(originalLyrics);
                    ShowScoreActivity.lyricInputACTextView.setSelection(originalLyrics.length());
                    ShowScoreActivity.lyricInputACTextView.requestFocus();

                    // show editNoteKeyboard
                    addArrow();
                    lyricEditing = true;
                    ShowScoreActivity.setLyricRecommendGroupVisibility(true);
                    ShowScoreActivity.lyricInputACTextView.setFocusableInTouchMode(true);
                    ShowScoreActivity.lyricInputACTextView.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(ShowScoreActivity.lyricInputACTextView, InputMethodManager.SHOW_IMPLICIT);
                }
            }

            return super.onSingleTapUp(e);
        }
    }

    private void searchClickWordView(float touchX) {
        /* search click word view */
        searchClickWordView:
        for(int i = 0; i < ShowScoreActivity.partStartId - ShowScoreActivity.measureStartId; i++) {
            MeasureViewGroup curSearchMeasure = (MeasureViewGroup)clickPart.findViewById(i + ShowScoreActivity.measureStartId);
            if(curSearchMeasure == null) break;

            if(touchX > curSearchMeasure.getLeft() && touchX < curSearchMeasure.getRight()) {
                clickMeasure = curSearchMeasure;

                for(int j = 0; j < ShowScoreActivity.measureStartId - ShowScoreActivity.wordStartId; j++) {
                    WordView curSearchWord = (WordView)clickMeasure.findViewById(j + ShowScoreActivity.wordStartId);
                    if(curSearchWord == null) break;

                    if(touchX > curSearchWord.getLeft() + curSearchMeasure.getLeft() &&
                            touchX < curSearchWord.getRight() + curSearchMeasure.getLeft()) {
                        clickWord = curSearchWord;
                        break searchClickWordView;
                    }
                }
            }
        }
    }

    private void findLyricEditStartPosition() {
        int measureSearchStartId = clickMeasure.getId();
        int wordSearchStartId = clickWord.getId();
        WordView preSearchWord = clickWord;

        findLyricEditStartPosition:
        for(int p = clickPart.getId(); p >= ShowScoreActivity.partStartId; p--) {
            PartViewGroup curSearchPart = (PartViewGroup) ShowScoreActivity.score.findViewById(p);
            // if the part is not the click part, set the measure search id to the last measure
            // of the pre part
            if (curSearchPart != clickPart) {
                if(curSearchPart.getId() == ShowScoreActivity.partStartId) {
                    measureSearchStartId = (curSearchPart.getChildCount() - 3) + ShowScoreActivity.measureStartId;
                }
                else {
                    // child count -1 for TieViewGroup
                    measureSearchStartId = (curSearchPart.getChildCount() - 2) + ShowScoreActivity.measureStartId;
                }
            }

            for (int m = measureSearchStartId; m >= ShowScoreActivity.measureStartId; m--) {
                MeasureViewGroup curSearchMeasure = (MeasureViewGroup) curSearchPart.findViewById(m);

                // if the measure is not the click measure, set the word search id to the last word
                // of the pre measure
                if (curSearchMeasure != clickMeasure) {
                    // child count - 1 for BarWidthView
                    wordSearchStartId = ((curSearchMeasure.getChildCount() - 1) / 2 - 1) + ShowScoreActivity.wordStartId;
                }

                for (int w = wordSearchStartId; w >= ShowScoreActivity.wordStartId; w--) {
                    WordView curSearchWord = (WordView) curSearchMeasure.findViewById(w);
                    if(curSearchWord == clickWord) continue;

                    NoteViewGroup curSearchNote = (NoteViewGroup) curSearchMeasure.findViewById(w - ShowScoreActivity.wordStartId + ShowScoreActivity.noteStartId);
                    NumberView curSearchNumberView = (NumberView) curSearchNote.findViewById(R.id.number_view);

                    // skip the note that can't input lyric
                    if (curSearchNumberView.getNote().equals("R") || curSearchNote.isTieEnd())
                        continue;

                    // if current search word is empty or including the sentence end tag "~", we got
                    // the edit start word!
                    if (curSearchWord.getWord().contains(NumberedMusicalNotationParser.sentenceEndTag.toString()) || curSearchWord.getWord().equals("")) {
                        lyricEditStartWord = preSearchWord;
                        break findLyricEditStartPosition;
                    }

                    preSearchWord = curSearchWord;
                }
            }
        }

        // search to the beginning part and measure
        if(lyricEditStartWord == null) {
            lyricEditStartWord = preSearchWord;
        }
    }

    private String collectWords() {
        // collect word views' words into original lyrics
        String originalLyrics = "";
        MeasureViewGroup startMeasure = (MeasureViewGroup)lyricEditStartWord.getParent();
        PartViewGroup startPart= (PartViewGroup)startMeasure.getParent();

        int partCollectStartIndex = startPart.getId() - ShowScoreActivity.partStartId;
        int measureCollectStartIndex = startMeasure.getId() - ShowScoreActivity.measureStartId;
        int wordCollectStartIndex = lyricEditStartWord.getId() - ShowScoreActivity.wordStartId;

        collectWords:
        for(int p = partCollectStartIndex; p < ShowScoreActivity.partMaxNumber; p++) {
            PartViewGroup curCollectPart = (PartViewGroup) ShowScoreActivity.score.findViewById(p + ShowScoreActivity.partStartId);
            if(curCollectPart == null) break;

            for (int m = measureCollectStartIndex; m < ShowScoreActivity.partStartId - ShowScoreActivity.measureStartId; m++) {
                MeasureViewGroup curCollectMeasure = (MeasureViewGroup) curCollectPart.findViewById(m + ShowScoreActivity.measureStartId);

                // collect to the end of the part, reset measure index, step to the next part and
                // continue collect words
                if (curCollectMeasure == null) {
                    measureCollectStartIndex = 0;
                    continue;
                }
                if (curCollectMeasure != startMeasure) wordCollectStartIndex = 0;

                for (int w = wordCollectStartIndex; w < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; w++) {
                    WordView curCollectWord = (WordView) curCollectMeasure.findViewById(w + ShowScoreActivity.wordStartId);
                    if (curCollectWord == null) break;

                    if (curCollectWord.getWord().contains(NumberedMusicalNotationParser.sentenceEndTag.toString())) {
                        originalLyrics += curCollectWord.getWord();
                        curCollectWord.setWord("");
                        curCollectWord.invalidate();
                        break collectWords;
                    }
                    originalLyrics += curCollectWord.getWord();
                    curCollectWord.setWord("");
                    curCollectWord.invalidate();
                }
            }
        }

        return originalLyrics;
    }

    private void addArrow(){
        MeasureViewGroup startMeasure = (MeasureViewGroup)lyricEditStartWord.getParent();
        PartViewGroup startPart = (PartViewGroup)startMeasure.getParent();

        RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(
                        lyricEditStartWord.getWidth(),
                        lyricEditStartWord.getHeight());
        lp.leftMargin = lyricEditStartWord.getLeft() + startMeasure.getLeft();
        lp.topMargin = lyricEditStartWord.getTop() + lyricEditStartWord.getHeight();
        arrow.setLayoutParams(lp);

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.floating);
        arrow.startAnimation(animation);

        startPart.addView(arrow);
    }

    public static void saveWordsIntoWordView() {
        if(lyricEditStartWord == null) return;

        MeasureViewGroup startMeasure = (MeasureViewGroup)lyricEditStartWord.getParent();
        PartViewGroup startPart = (PartViewGroup)startMeasure.getParent();
        String lyric = ShowScoreActivity.lyricInputACTextView.getText().toString();

        /* if user input lyric is empty */
        if(lyric.length() == 0) {
            // set the current edit word to empty string
            lyricEditStartWord.setWord("");
            lyricEditStartWord.invalidate();

            // remove arrow animation
            arrow.setAnimation(null);
            startPart.removeView(arrow);

            ShowScoreActivity.lyricInputACTextView.setFocusable(false);
            lyricEditStartWord = null;
            lyricEditing = false;
            return;
        }

        /* user input lyric is not empty */
        int i = 0;
        int partStartIndex = startPart.getId() - ShowScoreActivity.partStartId;
        int measureStartIndex = startMeasure.getId() - ShowScoreActivity.measureStartId;
        int wordStartIndex = lyricEditStartWord.getId() - ShowScoreActivity.wordStartId;

        saveWords:
        for (int p = partStartIndex; p < ShowScoreActivity.partMaxNumber; p++) {
            PartViewGroup curSearchPart = (PartViewGroup) ShowScoreActivity.score.findViewById(p + ShowScoreActivity.partStartId);
            if (curSearchPart == null) break;

            for (int m = measureStartIndex; m < ShowScoreActivity.partStartId - ShowScoreActivity.measureStartId; m++) {
                MeasureViewGroup curSearchMeasure = (MeasureViewGroup) curSearchPart.findViewById(m + ShowScoreActivity.measureStartId);

                // save to the end of the part, reset measure index, step to the next part and
                // continue saving words
                if (curSearchMeasure == null) {
                    measureStartIndex = 0;
                    continue;
                }

                if (curSearchMeasure != startMeasure) wordStartIndex = 0;

                /* start to save words */
                for (int w = wordStartIndex; w < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; w++) {
                    WordView curSearchWord = (WordView) curSearchMeasure.findViewById(w + ShowScoreActivity.wordStartId);
                    if (curSearchWord == null) break;

                    NoteViewGroup note = (NoteViewGroup) curSearchMeasure.findViewById(w + ShowScoreActivity.noteStartId);
                    if (note == null) break;
                    if (note.isTieEnd()) continue; // skip tie end
                    NumberView numberView = (NumberView) note.findViewById(R.id.number_view);
                    if ("R-".contains(numberView.getNote())) continue; // skip rest note


                    if (i == lyric.length() - 1) { // save the last word
                        curSearchWord.setWord(lyric.substring(i, i + 1) + NumberedMusicalNotationParser.sentenceEndTag);
                        curSearchWord.invalidate();
                        break saveWords;
                    }
                    else { // save a word
                        curSearchWord.setWord(lyric.substring(i, i + 1));
                        curSearchWord.invalidate();
                        i++;
                    }
                }
            }
        }

        // remove arrow animation
        arrow.setAnimation(null);
        startPart.removeView(arrow);


        // prevent warning
        if(ShowScoreActivity.lyricInputACTextView.getText().length() > 0)
            TextKeyListener.clear(ShowScoreActivity.lyricInputACTextView.getText());
        ShowScoreActivity.lyricInputACTextView.setFocusable(false);
        lyricEditStartWord = null;
        lyricEditing = false;
    }

    class BarView extends View {
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

    class TieView extends View {
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
}