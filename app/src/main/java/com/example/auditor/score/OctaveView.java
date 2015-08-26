package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Wan Lin on 15/8/4.
 * An octave or perfect octave is the interval between one musical pitch and another with half or
 * double its frequency.
 */
public class OctaveView extends View {
    private static final String LOG_TAG = "OctaView";
    private Paint mPaint;
    private int octave;
    private float dotRadius;
    private float space;
    private float padding;
    private int dotCount;

    public OctaveView(Context context) {
        super(context);
    }

    public OctaveView(Context context, int noteViewGroupHeight) {
        super(context);
        float viewHeight = noteViewGroupHeight * 0.225f;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        padding = viewHeight * 0.1f;
        dotRadius = viewHeight * 0.083f;
        space = (viewHeight - 2 * dotRadius - padding) / 3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = Math.round(2 * dotRadius + (dotCount - 1) * space + padding);
        setMeasuredDimension(widthMeasureSpec, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int center_x = getWidth() / 2;
        int center_y = getHeight();
        float tab = space;

        if(octave > 4) {
            tab = -tab;
            center_y -= dotRadius;
        }
        else {
            center_y = (int)(padding + dotRadius);
        }

        for(int i = 0; i < dotCount; i++) {
            canvas.drawCircle(center_x, center_y, dotRadius, mPaint);
            center_y += tab;
        }
    }

    public void setOctave(int octave) {
        this.octave = octave;
        dotCount = Math.abs(octave - 4);
    }

    public int geth() {
        return Math.round(2 * dotRadius + (dotCount - 1) * space + padding);
    }
}