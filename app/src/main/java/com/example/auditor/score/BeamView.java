package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/10.
 * A beam is a horizontal or diagonal line used to connect multiple consecutive notes
 * (and occasionally rests) in order to indicate rhythmic grouping.
 */
public class BeamView extends View {
    private Paint mPaint;
    private int beams;
    private int beamStrokeWidth;
    private int space;

    public BeamView(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        int viewHeight = Math.round(ShowScoreActivity.noteHeight * 0.15f);
        beamStrokeWidth = Math.round(viewHeight * 0.12f);
        mPaint.setStrokeWidth(beamStrokeWidth);
        space = Math.round(viewHeight - beamStrokeWidth) / 3;
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
        int right = getWidth() - getPaddingRight();

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