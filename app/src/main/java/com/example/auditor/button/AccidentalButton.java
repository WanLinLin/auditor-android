package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by wanlin on 2015/10/2.
 */
public class AccidentalButton extends Button{
    private Paint mPaint;
    private RectF rect;
    private String accidental;

    public AccidentalButton(Context context) {
        super(context);
        init();
    }

    public AccidentalButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AccidentalButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);

        rect = new RectF();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShowScoreActivity.measureEditMode) {
                    switch (accidental) {
                        case "#":
                            accidental = "b";
                            break;
                        case "b":
                            accidental = "";
                            break;
                        case "":
                            accidental = "#";
                            break;
                    }
                    AccidentalButton.this.invalidate();
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(accidental.equals("")) return;

        int incline = getMeasuredHeight() / 15;

        int left = 0;
        int top = getMeasuredHeight() / 8;
        int right = getMeasuredWidth() - getMeasuredWidth() / 6;
        int bottom = getMeasuredHeight() - getMeasuredHeight() / 8;
        int ratioHeight = bottom - top;

        switch (accidental) {
            case "#":
                left = getMeasuredWidth() / 6;
                right = getMeasuredWidth() - getMeasuredWidth() / 6;
                int ratioWidth = right - left;

                float[] pos = {
                        // horizontal line
                        left, top + ratioHeight / 3 + incline, right, top + ratioHeight / 3 - incline,
                        left, top + ratioHeight * 2 / 3 + incline, right, top + ratioHeight * 2 / 3 - incline,

                        // vertical line
                        left + ratioWidth / 3, top + incline, left + ratioWidth / 3, bottom,
                        left + ratioWidth * 2 / 3, top, left + ratioWidth * 2 / 3, bottom - incline,
                };

                mPaint.setStrokeWidth(getMeasuredHeight() / 11); // draw horizontal line
                canvas.drawLines(pos, 0, 8, mPaint);
                mPaint.setStrokeWidth(getMeasuredHeight() / 17); // draw vertical line
                canvas.drawLines(pos, 8, 8, mPaint);
                break;
            case "b":
                float sweepAngle = 180f;
                float arcStrokeRatioWidth = getMeasuredHeight() / 11;

                // slightly move up
                bottom = bottom - ratioHeight / 10;

                // draw the variable width arc
                rect.set(left, top + ratioHeight / 1.8f, right - arcStrokeRatioWidth / 2, bottom);
                mPaint.setStrokeWidth(arcStrokeRatioWidth);
                canvas.drawArc(rect, 270, sweepAngle, false, mPaint);

                // draw the vertical line
                mPaint.setStrokeWidth(getMeasuredHeight() / 15);
                canvas.drawLine((left + right) / 2, top, (left + right) / 2, bottom + arcStrokeRatioWidth / 2, mPaint);
                break;
        }
    }

    public void setAccidental(String accidental) {
        this.accidental = accidental;
    }
}
