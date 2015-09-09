package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Pair;
import android.view.View;
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
    private static final boolean SHOW_TIE_VIEW_COLOR = false;
    private static int tieStrokeWidth;
    private Context context;
    private Paint mPaint;
    private ArrayList<Pair<Integer, String>> tieInfo;
    private TieViewGroup tieViewLayout;

    public static int barStrokeWidth;
    public static int tieViewHeight;

    public PartViewGroup(Context context) {
        super(context);
        this.context = context;
        tieInfo = new ArrayList<>();

        tieViewHeight = ShowScoreActivity.NoteChildViewDimension.TIE_VIEW_HEIGHT;
        tieStrokeWidth = ShowScoreActivity.NoteChildViewDimension.TIE_STROKE_WIDTH;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        tieViewLayout = new TieViewGroup(context);
        tieViewLayout.setId(R.id.tie_view_layout);

        // add tie view layout
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                tieViewHeight);
        tieViewLayout.setLayoutParams(lp);
        this.addView(tieViewLayout);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        for(int i = 0; i < getChildCount(); i++) {
            getChildAt(i).requestLayout();
        }
    }

    public void printMeasure(MeasureViewGroup measureViewGroup, int i) {
        int measureViewGroupId = i + ShowScoreActivity.measureStartId;

        // add first bar
        if(measureViewGroupId == ShowScoreActivity.measureStartId) {
            BarView barView = new BarView(context);
            RelativeLayout.LayoutParams lp =
                    new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.BELOW, R.id.tie_view_layout);
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            barView.setLayoutParams(lp);
            this.addView(barView);
        }

        // add this measure
        RelativeLayout.LayoutParams rlp =
                new LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.BELOW, R.id.tie_view_layout);
        if(measureViewGroupId > ShowScoreActivity.measureStartId)
            rlp.addRule(RIGHT_OF, measureViewGroupId - 1); // previous measure id
        else
            rlp.addRule(ALIGN_PARENT_LEFT);
        measureViewGroup.setLayoutParams(rlp);
        measureViewGroup.setId(measureViewGroupId);
        this.addView(measureViewGroup);

        // add a new bar right of this measure
        BarView barView = new BarView(context);
        RelativeLayout.LayoutParams lp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RIGHT_OF, measureViewGroup.getId());
        lp.addRule(RelativeLayout.BELOW, R.id.tie_view_layout);
        barView.setLayoutParams(lp);
        this.addView(barView);
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
                tieViewLayout.addView(tieView);
            }
        }
    }

    public void clearTieInfo() {
        this.tieInfo.clear();
    }

    private class BarView extends View {
        private int width;
        private int height;

        public BarView(Context context) {
            super(context);

            barStrokeWidth = ShowScoreActivity.NoteChildViewDimension.BAR_STROKE_WIDTH;
            width = barStrokeWidth * 3;
            height = ShowScoreActivity.noteHeight;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            barStrokeWidth = ShowScoreActivity.NoteChildViewDimension.BAR_STROKE_WIDTH;
            width = barStrokeWidth * 3;
            height = ShowScoreActivity.noteHeight;
            setMeasuredDimension(width, height);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(barStrokeWidth);
            canvas.drawLine(width/2, 0, width/2, height, mPaint);
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

    public TieViewGroup getTieViewLayout() {
        return tieViewLayout;
    }
}