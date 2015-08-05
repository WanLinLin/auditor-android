package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by Wan Lin on 15/8/4.
 * Draw the sharp or flat sign
 */
public class AccidentalView extends View {
    private Paint mPaint;
    private Paint mPaint2;
    private String accidental;

    public AccidentalView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int incline = 5;
        float startX = getX();
        float startY = getY();
        Log.e(this.getClass().getName(), "x: " + startX + ", y: " + startY);

        switch(accidental) {
            case "#":
                float[] pos = {
                        0, getMeasuredHeight()/3 + incline, getMeasuredWidth(), getMeasuredHeight()/3 - incline,
                        0, getMeasuredHeight()*2/3 + incline, getMeasuredWidth(), getMeasuredHeight()*2/3 - incline,
                        getMeasuredWidth()/3, incline, getMeasuredWidth()/3, getMeasuredHeight(),
                        getMeasuredWidth()*2/3, 0, getMeasuredWidth()*2/3, getMeasuredHeight() - incline,
                };
                canvas.drawLines(pos, 0, 8, mPaint);
                canvas.drawLines(pos, 8, 8, mPaint2);
                break;
            case "b":
                // TODO draw the flat sign
                // TODO measure the padding of the view when onMeasure
                break;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0);

        setMeasuredDimension(w, h);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);

        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setColor(Color.BLACK);
        mPaint2.setStyle(Paint.Style.STROKE);
        mPaint2.setStrokeWidth(5);
    }

    public void setAccidental(String accidental) {
        this.accidental = accidental;
    }
}
