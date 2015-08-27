package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * Created by Wan Lin on 15/8/4.
 * An accidental is a note whose pitch (or pitch class) is not a member of the scale or mode
 * indicated by the most recently applied key signature.
 *
 * 5 dots at most(we support no shorter than sixty-forth note).
 */
public class AccidentalView extends View {
    private Paint mPaint;
    private Paint mPaint2;
    private String accidental;
    private RectF rect;

    public AccidentalView(Context context) {
        super(context);
        init();
        rect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int incline = 5;

        int left = 0;
        int top = getHeight() / 8;
        int right = getWidth();
        int bottom = getHeight() - getHeight() / 8;
        int ratioHeight = bottom - top;

        switch (accidental) {
            case "#":
                left = getWidth() / 6;
                right = getWidth();
                int ratioWidth = right - left;

                mPaint.setStrokeWidth(getHeight() / 11); // draw horizontal line
                mPaint2.setStrokeWidth(getHeight() / 17); // draw vertical line
                float[] pos = {
                        // horizontal line
                        left, top + ratioHeight / 3 + incline, right, top + ratioHeight / 3 - incline,
                        left, top + ratioHeight * 2 / 3 + incline, right, top + ratioHeight * 2 / 3 - incline,

                        // vertical line
                        left + ratioWidth / 3, top + incline, left + ratioWidth / 3, bottom,
                        left + ratioWidth * 2 / 3, top, left + ratioWidth * 2 / 3, bottom - incline,
                };
                canvas.drawLines(pos, 0, 8, mPaint);
                canvas.drawLines(pos, 8, 8, mPaint2);
                break;
            case "b":
                float sweepAngle = 180f;
                float arcStrokeRatioWidth = getHeight() / 11;

                // slightly move up
                bottom = bottom - ratioHeight / 10;

                // draw the variable width arc
                rect.set(left, top + ratioHeight / 1.8f, right - arcStrokeRatioWidth / 2, bottom);
                mPaint.setStrokeWidth(arcStrokeRatioWidth);
                canvas.drawArc(rect, 270, sweepAngle, false, mPaint);

                // draw the vertical line
                mPaint.setStrokeWidth(getHeight() / 15);
                canvas.drawLine((left + right) / 2, top, (left + right) / 2, bottom + arcStrokeRatioWidth / 2, mPaint);
                break;
        }
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);

        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setColor(Color.BLACK);
        mPaint2.setStyle(Paint.Style.STROKE);
    }

    public void setAccidental(String accidental) {
        this.accidental = accidental;
    }
}