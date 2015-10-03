package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/4.
 * An octave or perfect octave is the interval between one musical pitch and another with half or
 * double its frequency.
 */
public class OctaveView extends View {
    private Paint mPaint;
    private int octave;
    private int dotCount;
    private int dotRadius;
    private int space;
    private int padding;
    private boolean hasBeamView;
    private boolean hasAccidentalView;

    private int width;
    private int height;

    public OctaveView(Context context) {
        super(context);
    }

    public OctaveView(Context context, boolean hasBeamView, boolean hasAccidentalView) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        width = ShowScoreActivity.NoteChildViewDimension.OCTAVE_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.OCTAVE_VIEW_HEIGHT;

        padding = Math.round(height * 0.1f);
        dotRadius = Math.round(height * 0.083f);
        space = Math.round((height - 2 * dotRadius - padding) / 3);

        this.hasBeamView = hasBeamView;
        this.hasAccidentalView = hasAccidentalView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = ShowScoreActivity.NoteChildViewDimension.OCTAVE_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.OCTAVE_VIEW_HEIGHT;

        padding = Math.round(height * 0.1f);
        dotRadius = Math.round(height * 0.083f);
        space = Math.round((height - 2 * dotRadius - padding) / 3);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(octave == 4)
            return;

        super.onDraw(canvas);

        int center_x = getWidth() / 2;
        int center_y = getHeight();
        float tab = space;

        if(octave > 4 && octave <= 8) {
            tab = -tab;
            center_y -= dotRadius;
        }
        else if(octave < 4 && octave >= 0){
            center_y = padding + dotRadius;
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

    public String getOctave() {
        return Integer.toString(octave);
    }
}