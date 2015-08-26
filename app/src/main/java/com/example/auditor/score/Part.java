package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Pair;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/8/20.
 * A part contains numbers of measures.
 */
public class Part extends RelativeLayout{
    private static final String LOG_TAG = "PartViewGroup";
    public static final int measureStartId = 101;
    private int noteViewGroupHeight;
    private float tieStrokeWidth;
    private Context context;
    private Paint mPaint;
    private ArrayList<Pair<Integer, String>> tieInfo;

    public static float barStrokeWidth;
    public static int tieViewHeight;

    public Part(Context context) {
        super(context);
    }

    public Part(Context context, int noteViewGroupHeight) {
        super(context);
        this.context = context;
        this.noteViewGroupHeight = noteViewGroupHeight;
        tieInfo = new ArrayList<>();

        tieViewHeight = (int)(noteViewGroupHeight * 0.225f);
        tieStrokeWidth = (int)(noteViewGroupHeight * 0.022f);
        barStrokeWidth = noteViewGroupHeight * 0.03f;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, noteViewGroupHeight + tieViewHeight);
    }

    public void printMeasure(Measure measure, int i) {
        int measureViewGroupId = i + measureStartId;

        // add first bar
        if(measureViewGroupId == measureStartId) {
            BarView barView = new BarView(context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = tieViewHeight;
            lp.leftMargin = (int) barStrokeWidth;
            barView.setLayoutParams(lp);
            this.addView(barView);
        }

        // add this measure
        RelativeLayout.LayoutParams rlp = new LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.topMargin = tieViewHeight;
        rlp.leftMargin = (int)(3 * barStrokeWidth);
        if(measureViewGroupId > measureStartId)
            rlp.addRule(RIGHT_OF, measureViewGroupId - 1); // previous measure id
        else
            rlp.addRule(ALIGN_PARENT_LEFT);
        measure.setLayoutParams(rlp);
        measure.setId(measureViewGroupId);
        this.addView(measure);

        // add a new bar right of this measure
        BarView barView = new BarView(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RIGHT_OF, measure.getId());
        lp.topMargin = tieViewHeight;
        lp.leftMargin = (int) barStrokeWidth;
        lp.rightMargin = (int) barStrokeWidth;
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

                RectF rectF = new RectF(0 + tieStrokeWidth / 2, tieViewHeight / 2, rectWidth + tieStrokeWidth / 2, tieViewHeight * 3 / 2);
                TieView tieView = new TieView(context, rectF);

                RelativeLayout.LayoutParams rlp =
                        new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                rlp.leftMargin = rectStartX - (int)(tieStrokeWidth / 2);

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
            setMeasuredDimension(Math.round(barStrokeWidth), noteViewGroupHeight);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            mPaint.setColor(Color.BLACK);
            mPaint.setStrokeWidth(barStrokeWidth);
            canvas.drawLine(barStrokeWidth/2, 0, barStrokeWidth/2, noteViewGroupHeight, mPaint);
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