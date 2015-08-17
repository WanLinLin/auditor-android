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

    public OctaveView(Context context) {
        super(context);
        init();
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

    @Override
    protected void onDraw(Canvas canvas) {
        // env init
        int dotCount = Math.abs(octave - 4);
        int width = getMeasuredWidth();
        float height = getHeight();
        int center_x = width / 2;
        float heightTab = height / (dotCount+1);
        float dotRadius = height * 0.08f;

        if(octave > 4) {
            heightTab = -heightTab;
            height -= dotRadius;
        }
        else {
            height = dotRadius;
        }

        for(int i = 0; i < dotCount; i++) {
            float center_y = height + i * heightTab;
            canvas.drawCircle(center_x, center_y, dotRadius, mPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setOctave(int octave) {
        this.octave = octave;
    }
}