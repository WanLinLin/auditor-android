package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.auditor.ShowScoreActivity;

import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/8/20.
 * A part contains numbers of measures.
 */
public class PartViewGroup extends RelativeLayout{
    private static final boolean SHOW_TIE_VIEW_COLOR = false;
    private static int tieStrokeWidth;
    private Context context;
    private Paint mPaint;
    private ArrayList<Pair<Integer, String>> tieInfo;

    public static final int measureStartId = 101;
    public static int barStrokeWidth;
    public static int tieViewHeight;

    public PartViewGroup(Context context) {
        super(context);
        this.context = context;
        tieInfo = new ArrayList<>();

        tieViewHeight = Math.round(ShowScoreActivity.noteHeight * 0.225f);
        tieStrokeWidth = Math.round(ShowScoreActivity.noteHeight * 0.022f);
        barStrokeWidth = Math.round(ShowScoreActivity.noteHeight * 0.03f);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void printMeasure(MeasureViewGroup measureViewGroup, int i) {
        int measureViewGroupId = i + measureStartId;

        // add first bar
        if(measureViewGroupId == measureStartId) {
            BarView barView = new BarView(context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = tieViewHeight;
            lp.leftMargin = barStrokeWidth;
            barView.setLayoutParams(lp);
            this.addView(barView);
        }

        // add this measure
        RelativeLayout.LayoutParams rlp = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = tieViewHeight;
        rlp.leftMargin = Math.round(3 * barStrokeWidth);
        if(measureViewGroupId > measureStartId)
            rlp.addRule(RIGHT_OF, measureViewGroupId - 1); // previous measure id
        else
            rlp.addRule(ALIGN_PARENT_LEFT);
        measureViewGroup.setLayoutParams(rlp);
        measureViewGroup.setId(measureViewGroupId);
        this.addView(measureViewGroup);

        // add a new bar right of this measure
        BarView barView = new BarView(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RIGHT_OF, measureViewGroup.getId());
        lp.topMargin = tieViewHeight;
        lp.leftMargin = barStrokeWidth;
        lp.rightMargin = barStrokeWidth;
        barView.setLayoutParams(lp);
        this.addView(barView);
    }

    public void addTieInfo(Pair<Integer, String> tieInfo) {
        this.tieInfo.add(tieInfo);
    }

    public void addTieView() {
        int rectStartX = 0;

        for(Pair<Integer, String> p: tieInfo) {
            int x = p.first;
            String tie = p.second;


            if(tie.equals("start")) {
                rectStartX = x;
            }
            else if(tie.equals("end")) {
                int rectWidth = x - rectStartX;

                RectF rectF = new RectF(tieStrokeWidth / 2, tieViewHeight / 2, rectWidth + tieStrokeWidth / 2, tieViewHeight * 3 / 2);
                TieView tieView = new TieView(context, rectF);
                if(SHOW_TIE_VIEW_COLOR)
                    tieView.setBackgroundColor(Color.RED);

                RelativeLayout.LayoutParams rlp =
                        new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                rlp.leftMargin = rectStartX - tieStrokeWidth / 2;

                tieView.setLayoutParams(rlp);
                this.addView(tieView);
            }
        }
    }

    private class BarView extends View {
        public BarView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(Math.round(barStrokeWidth), ShowScoreActivity.noteHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(barStrokeWidth);
            canvas.drawLine(barStrokeWidth/2, 0, barStrokeWidth/2, ShowScoreActivity.noteHeight, mPaint);
        }
    }

    private class TieView extends View {
        private RectF rectF;

        public TieView(Context context, RectF rectF) {
            super(context);
            this.rectF = rectF;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
}