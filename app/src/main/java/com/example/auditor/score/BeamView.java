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
    private String duration;
    private int lineCount = 0;
    private int beamStrokeWidth;
    private int space;

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
        height = Math.round(beamStrokeWidth + (lineCount - 1) * space);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(duration.equals("") || "whq-".contains(duration)) setMeasuredDimension(0, 0);
        else {
            height = ShowScoreActivity.NoteChildViewDimension.BEAM_VIEW_HEIGHT;

            beamStrokeWidth = Math.round(height * 0.12f);
            mPaint.setStrokeWidth(beamStrokeWidth);
            space = Math.round(height - beamStrokeWidth) / 3;
            height = Math.round(beamStrokeWidth + (lineCount - 1) * space);

            width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;

            NoteViewGroup note = (NoteViewGroup)getParent();
            if (note.hasAccidentalView())
                width += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
            if (note.hasDottedView())
                width += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;

            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(duration.equals("") || "whq".contains(duration)) return;

        float y = beamStrokeWidth / 2;

        for(int i = 0; i < lineCount; i++) {
            canvas.drawLine(0, y, getWidth(), y, mPaint);
            y += space;
        }
    }

    public void setDuration(String duration) {
        this.duration = duration;

        switch(duration) {
            case "i":
                lineCount = 1;
                break;
            case "s":
                lineCount = 2;
                break;
            case "t":
                lineCount = 3;
                break;
            case "x":
                lineCount = 4;
                break;
        }
    }

    public String getDuration() {
        return duration;
    }
}