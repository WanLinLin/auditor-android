package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
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

    private boolean hasAccidentalView;
    private boolean hasDottedView;

    private int width;
    private int height;

    public BeamView(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        beamStrokeWidth = Math.round(height * 0.12f);
        mPaint.setStrokeWidth(beamStrokeWidth);
        space = Math.round(height - beamStrokeWidth) / 3;
        height = Math.round(beamStrokeWidth + (beams - 1) * space);

        width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
        if(hasAccidentalView)
            width += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
        if(hasDottedView)
            width += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        height = ShowScoreActivity.NoteChildViewDimension.BEAM_VIEW_HEIGHT;

        beamStrokeWidth = Math.round(height * 0.12f);
        mPaint.setStrokeWidth(beamStrokeWidth);
        space = Math.round(height - beamStrokeWidth) / 3;
        height = Math.round(beamStrokeWidth + (beams - 1) * space);

        width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
        if(hasAccidentalView)
            width += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
        if(hasDottedView)
            width += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float y = beamStrokeWidth / 2;

        for(int i = 0; i < beams; i++) {
            canvas.drawLine(0, y, getWidth(), y, mPaint);
            y += space;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch(beams) {
                    case 1:
                        beams = 2;
                        break;
                    case 2:
                        beams = 3;
                        break;
                    case 3:
                        beams = 4;
                        break;
                    case 4:
                        beams = 1;
                        break;
                }
                break;
        }
        this.requestLayout();
        return super.onTouchEvent(event);
    }

    public void setBeams(int beams) {
        this.beams = beams;
    }

    public void setHasAccidentalView(boolean hasAccidentalView) {
        this.hasAccidentalView = hasAccidentalView;
    }

    public void setHasDottedView(boolean hasDottedView) {
        this.hasDottedView = hasDottedView;
    }

    public String getBeams() {
        switch (beams) {
            case 1:
                return "i";
            case 2:
                return "s";
            case 3:
                return "t";
            case 4:
                return "x";
        }
        return null;
    }
}