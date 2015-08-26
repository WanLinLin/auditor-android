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
    private static final String LOG_TAG = "BeamView";
    private Paint mPaint;
    private int beams;
    private float beamStrokeWidth;
    private float space;

    public BeamView(Context context) {
        super(context);
    }

    public BeamView(Context context, float viewHeight) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        beamStrokeWidth = viewHeight * 0.12f;
        mPaint.setStrokeWidth(beamStrokeWidth);
        space = (viewHeight - beamStrokeWidth) / 3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = Math.round(beamStrokeWidth + (beams - 1) * space);
        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int bottom = getHeight() - getPaddingBottom();

        float y = beamStrokeWidth / 2;

        for(int i = 0; i < beams; i++) {
            canvas.drawLine(left, y, right, y, mPaint);
            y += space;
        }
    }

    public void setBeams(int beams) {
        this.beams = beams;
    }
}