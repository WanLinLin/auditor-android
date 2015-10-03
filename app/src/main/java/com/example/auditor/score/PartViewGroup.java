package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.text.method.TextKeyListener;
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

import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/8/20.
 * A part contains numbers of measures.
 */
public class PartViewGroup extends RelativeLayout {
    private static final String LOG_TAG = PartViewGroup.class.getName();
    private static final String LYRIC_EDIT_MODE = "歌詞編輯";
    private static final String MEASURE_EDIT_MODE = "樂譜編輯";
    private static final boolean SHOW_TIE_VIEW_COLOR = false;
    private static int tieStrokeWidth;
    public static GestureDetector gestureDetector;
    private Context context;
    private Paint mPaint;
    private ArrayList<Pair<Integer, String>> tieInfo;
    private TieViewGroup tieViewGroup;

    private MeasureViewGroup clickMeasure;
    private WordView clickWord;
    public static MeasureViewGroup lyricEditStartMeasure = null;
    public static WordView lyricEditStartWord = null;
    private static boolean lyricEditing;

    private static ImageView arrow;

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
        return gestureDetector.onTouchEvent(event);
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

            /* single touch down on lyric area */
            if (y > getHeight() - ShowScoreActivity.NoteChildViewDimension.WORD_VIEW_HEIGHT) {
                if(ShowScoreActivity.lyricEditMode) {
                    if(lyricEditing) return true;

                    searchClickWordView(x);

                    /* if clicked word is empty, return */
                    if(clickWord == null) return true; // click on not word view area
                    NoteViewGroup note =
                            (NoteViewGroup)clickMeasure.findViewById(clickWord.getId() - ShowScoreActivity.wordStartId + ShowScoreActivity.noteStartId);
                    NumberView numberView = (NumberView)note.findViewById(R.id.number_view);
                    if(clickWord.getWord().equals("") && !note.isTieEnd() && !"R-".contains(numberView.getNote())) {
                        lyricEditStartMeasure = clickMeasure;
                        lyricEditStartWord = clickWord;
                        addArrow();

                        /* show keyboard */
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

                    /* show keyboard */
                    addArrow();
                    lyricEditing = true;
                    ShowScoreActivity.setLyricRecommendGroupVisibility(true);
                    ShowScoreActivity.lyricInputACTextView.setFocusableInTouchMode(true);
                    ShowScoreActivity.lyricInputACTextView.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(ShowScoreActivity.lyricInputACTextView, InputMethodManager.SHOW_IMPLICIT);
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
                if(ShowScoreActivity.measureEditMode) ShowScoreActivity.measureEditMode = false;
            }
            else if(y < ShowScoreActivity.NoteChildViewDimension.TIE_VIEW_HEIGHT + ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_HEIGHT) { // long press on note view
                ShowScoreActivity.measureEditMode = !ShowScoreActivity.measureEditMode;
                if(ShowScoreActivity.lyricEditMode) ShowScoreActivity.lyricEditMode = false;
            }

            if(ShowScoreActivity.lyricEditMode) {
                ShowScoreActivity.actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#64B5F6")));
                ShowScoreActivity.actionBar.setTitle(LYRIC_EDIT_MODE);
                ShowScoreActivity.menu.findItem(R.id.action_zoom_in).setVisible(false);
                ShowScoreActivity.menu.findItem(R.id.action_zoom_out).setVisible(false);

                ShowScoreActivity.setLyricRecommendGroupVisibility(true);
            }
            else {
                /* hide lyric input text view and recommend button */
                ShowScoreActivity.setLyricRecommendGroupVisibility(false);

                /* close keyboard */
                InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ShowScoreActivity.lyricInputACTextView.getWindowToken(), 0);

                if(lyricEditStartMeasure != null) saveWordsIntoWordView();
            }

            if(ShowScoreActivity.measureEditMode) {
                ShowScoreActivity.actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#64B5F6")));
                ShowScoreActivity.actionBar.setTitle(MEASURE_EDIT_MODE);
                ShowScoreActivity.menu.findItem(R.id.action_zoom_in).setVisible(false);
                ShowScoreActivity.menu.findItem(R.id.action_zoom_out).setVisible(false);

                ShowScoreActivity.keyboard.setVisibility(VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.keyboard_swipe_in);
                ShowScoreActivity.keyboard.setAnimation(animation);
                ShowScoreActivity.keyboard.animate();
            }
            else if(!ShowScoreActivity.measureEditMode && ShowScoreActivity.keyboard.isShown()){
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.keyboard_swipe_out);
                ShowScoreActivity.keyboard.setAnimation(animation);
                ShowScoreActivity.keyboard.animate();
                ShowScoreActivity.keyboard.setVisibility(GONE);
            }

            if(!ShowScoreActivity.measureEditMode && !ShowScoreActivity.lyricEditMode) {
                ShowScoreActivity.actionBar.setBackgroundDrawable(new ColorDrawable(Color.DKGRAY));
                ShowScoreActivity.actionBar.setTitle(ShowScoreActivity.scoreName);
            }
        }
    }

    private String collectWords() {
        /* collect word views' words into original lyrics */
        String originalLyrics = "";
        if(lyricEditStartMeasure == null) lyricEditStartMeasure = (MeasureViewGroup)findViewById(ShowScoreActivity.measureStartId);
        if(lyricEditStartWord == null) lyricEditStartWord = (WordView)lyricEditStartMeasure.findViewById(ShowScoreActivity.wordStartId);
        int measureCollectStartIndex = lyricEditStartMeasure.getId() - ShowScoreActivity.measureStartId;
        int wordCollectStartIndex = lyricEditStartWord.getId() - ShowScoreActivity.wordStartId;

        collectWords:
        for(int i = measureCollectStartIndex; i < ShowScoreActivity.partStartId - ShowScoreActivity.measureStartId; i++) {
            MeasureViewGroup curCollectMeasure = (MeasureViewGroup)findViewById(i + ShowScoreActivity.measureStartId);
            if(curCollectMeasure == null) break;
            if(curCollectMeasure != lyricEditStartMeasure) wordCollectStartIndex = 0;

            for (int j = wordCollectStartIndex; j < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; j++) {
                WordView curCollectWord = (WordView) curCollectMeasure.findViewById(j + ShowScoreActivity.wordStartId);
                if(curCollectWord == null) break;

                if(curCollectWord.getWord().contains(NumberedMusicalNotationParser.sentenceEndTag.toString())) {
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

        return originalLyrics;
    }

    private void findLyricEditStartPosition() {
        int wordSearchStartId = clickWord.getId();
        MeasureViewGroup preMeasure = null;
        WordView preWord = null;

        findLyricEditStartPosition:
        for(int m = clickMeasure.getId(); m >= ShowScoreActivity.measureStartId; m--) {
            MeasureViewGroup curSearchMeasure = (MeasureViewGroup)findViewById(m);
            if(curSearchMeasure != clickMeasure) wordSearchStartId = (curSearchMeasure.getChildCount() / 2 - 1) + ShowScoreActivity.wordStartId;

            for(int w = wordSearchStartId; w >= ShowScoreActivity.wordStartId; w--) {
                WordView curSearchWord = (WordView)curSearchMeasure.findViewById(w);
                if(curSearchWord == clickWord) {
                    preMeasure = curSearchMeasure;
                    preWord = curSearchWord;
                    continue;
                }

                NoteViewGroup curSearchNote = (NoteViewGroup)curSearchMeasure.findViewById(w - ShowScoreActivity.wordStartId + ShowScoreActivity.noteStartId);
                NumberView curSearchNumberView = (NumberView)curSearchNote.findViewById(R.id.number_view);

                if(curSearchNumberView.getNote().equals("R") || curSearchNote.isTieEnd()) continue;

                if(curSearchWord.getWord().contains(NumberedMusicalNotationParser.sentenceEndTag.toString()) || curSearchWord.getWord().equals("")) {
                    if(preMeasure == null) lyricEditStartMeasure = curSearchMeasure;
                    else lyricEditStartMeasure = preMeasure;

                    if(preWord == null) lyricEditStartWord = curSearchWord;
                    else lyricEditStartWord = preWord;

                    break findLyricEditStartPosition;
                }

                preMeasure = curSearchMeasure;
                preWord = curSearchWord;
            }
        }
    }

    private void searchClickWordView(float x) {
        /* search click word view */
        searchClickWordView:
        for(int i = 0; i < ShowScoreActivity.partStartId - ShowScoreActivity.measureStartId; i++) {
            MeasureViewGroup curSearchMeasure = (MeasureViewGroup)findViewById(i + ShowScoreActivity.measureStartId);
            if(curSearchMeasure == null) break;

            if(x > curSearchMeasure.getLeft() && x < curSearchMeasure.getRight()) {
                clickMeasure = curSearchMeasure;

                for(int j = 0; j < ShowScoreActivity.measureStartId - ShowScoreActivity.wordStartId; j++) {
                    WordView curSearchWord = (WordView)clickMeasure.findViewById(j + ShowScoreActivity.wordStartId);
                    if(curSearchWord == null) break;

                    if(x > curSearchWord.getLeft() + curSearchMeasure.getLeft() &&
                            x < curSearchWord.getRight() + curSearchMeasure.getLeft()) {
                        clickWord = curSearchWord;
                        break searchClickWordView;
                    }
                }
            }
        }
    }

    private void addArrow(){
        arrow = new ImageView(context);
        arrow.setId(R.id.lyric_arrow);
        arrow.setImageResource(R.drawable.lyric_arrow);
        arrow.setEnabled(false);

        RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(
                        lyricEditStartWord.getWidth(),
                        lyricEditStartWord.getHeight());
        lp.leftMargin = lyricEditStartWord.getLeft() + lyricEditStartMeasure.getLeft();
        lp.topMargin = lyricEditStartWord.getTop() + lyricEditStartWord.getHeight();
        arrow.setLayoutParams(lp);

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.floating);
        arrow.startAnimation(animation);

        this.addView(arrow);
    }

    public static boolean saveWordsIntoWordView() {
        if(lyricEditStartMeasure == null || lyricEditStartWord == null) return false;

        PartViewGroup part = (PartViewGroup)lyricEditStartMeasure.getParent();
        String lyric = ShowScoreActivity.lyricInputACTextView.getText().toString();
        if(lyric.length() == 0) {
            lyricEditStartWord.setWord("");
            lyricEditStartWord.invalidate();
            arrow.setAnimation(null);
            part.removeView(arrow);

            if(ShowScoreActivity.lyricInputACTextView.getText().length() > 0)
                TextKeyListener.clear(ShowScoreActivity.lyricInputACTextView.getText());
            ShowScoreActivity.lyricInputACTextView.setFocusable(false);
            lyricEditStartMeasure = null;
            lyricEditStartWord = null;
            lyricEditing = false;
            return true;
        }

        int i = 0;
        int wordStartIndex = lyricEditStartWord.getId() - ShowScoreActivity.wordStartId;
        int measureStartIndex = lyricEditStartMeasure.getId() - ShowScoreActivity.measureStartId;

        saveWords:
        for (int m = measureStartIndex; m < ShowScoreActivity.partStartId - ShowScoreActivity.measureStartId; m++) {
            MeasureViewGroup curSearchMeasure = (MeasureViewGroup)part.findViewById(m + ShowScoreActivity.measureStartId);
            if(curSearchMeasure == null) break;
            if(curSearchMeasure != lyricEditStartMeasure) wordStartIndex = 0;

            /* find word */
            for (int w = wordStartIndex; w < ShowScoreActivity.wordStartId - ShowScoreActivity.noteStartId; w++) {
                WordView curSearchWord = (WordView)curSearchMeasure.findViewById(w + ShowScoreActivity.wordStartId);
                if(curSearchWord == null) break;

                NoteViewGroup noteViewGroup = (NoteViewGroup)curSearchMeasure.findViewById(w + ShowScoreActivity.noteStartId);
                if(noteViewGroup == null) break;
                if(noteViewGroup.isTieEnd()) continue; // skip tie end
                NumberView numberView = (NumberView)noteViewGroup.findViewById(R.id.number_view);
                if("R-".contains(numberView.getNote())) continue; // skip rest note


                if(i == lyric.length() - 1) {
                    curSearchWord.setWord(lyric.substring(i, i + 1) + NumberedMusicalNotationParser.sentenceEndTag);
                    break saveWords;
                }
                else curSearchWord.setWord(lyric.substring(i, i + 1));
                curSearchWord.invalidate();
                i++;
            }
        }

        arrow.setAnimation(null);
        part.removeView(arrow);

        if(ShowScoreActivity.lyricInputACTextView.getText().length() > 0)
            TextKeyListener.clear(ShowScoreActivity.lyricInputACTextView.getText());
        ShowScoreActivity.lyricInputACTextView.setFocusable(false);
        lyricEditStartMeasure = null;
        lyricEditStartWord = null;
        lyricEditing = false;

        return true;
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