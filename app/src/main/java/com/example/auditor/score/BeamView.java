package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Wan Lin on 15/8/10.
 * A beam is a horizontal or diagonal line used to connect multiple consecutive notes
 * (and occasionally rests) in order to indicate rhythmic grouping.
 */
public class BeamView extends View {
    private Paint mPaint;
    private int beams;

    public BeamView(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int bottom = getHeight() - getPaddingBottom();
        int widthWithoutPadding = right - left;
        int heightWithoutPadding = bottom - top;

        float strokeWidth = heightWithoutPadding * 0.1f;
        int space = heightWithoutPadding / (beams + 1);
        float y = strokeWidth / 2;

        mPaint.setStrokeWidth(strokeWidth);

        for(int i = 0; i < beams; i++) {
            canvas.drawLine(left, y, right, y, mPaint);
            y += space;
        }
    }

    public void setBeams(int beams) {
        this.beams = beams;
    }
}

// TODO
