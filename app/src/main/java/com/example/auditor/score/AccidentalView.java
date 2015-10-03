package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/4.
 * An accidental is a note whose pitch (or pitch class) is not a member of the scale or mode
 * indicated by the most recently applied key signature.
 *
 * 5 dots at most(we support no shorter than sixty-forth note).
 */
public class AccidentalView extends View {
    private static final String LOG_TAG = AccidentalView.class.getName();
    private Paint mPaint;
    private Paint mPaint2;
    private String accidental;
    private RectF rect;
    private int width;
    private int height;

    public AccidentalView(Context context) {
        super(context);
        init();
        rect = new RectF();

        width = ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_HEIGHT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_HEIGHT;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int incline = height / 15;

        int left = 0;
        int top = height / 8;
        int right = width;
        int bottom = height - height / 8;
        int ratioHeight = bottom - top;

        switch (accidental) {
            case "#":
                left = width / 6;
                right = width;
                int ratioWidth = right - left;

                mPaint.setStrokeWidth(height / 11); // draw horizontal line
                mPaint2.setStrokeWidth(height / 17); // draw vertical line
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
                float arcStrokeRatioWidth = height / 11;

                // slightly move up
                bottom = bottom - ratioHeight / 10;

                // draw the variable width arc
                rect.set(left, top + ratioHeight / 1.8f, right - arcStrokeRatioWidth / 2, bottom);
                mPaint.setStrokeWidth(arcStrokeRatioWidth);
                canvas.drawArc(rect, 270, sweepAngle, false, mPaint);

                // draw the vertical line
                mPaint.setStrokeWidth(height / 15);
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

    public String getAccidental() {
        return accidental;
    }
}
