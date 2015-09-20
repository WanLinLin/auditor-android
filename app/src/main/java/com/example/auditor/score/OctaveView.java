package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.auditor.R;
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!ShowScoreActivity.noteEditMode)
            return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (octave) {
                    case 0:
                        octave = 1;
                        break;
                    case 1:
                        octave = 2;
                        break;
                    case 2:
                        octave = 3;
                        break;
                    case 3:
                        octave = 5;
                        break;
//                    case 4:
//                        octave = 5;
//                        break;
                    case 5:
                        octave = 6;
                        break;
                    case 6:
                        octave = 7;
                        break;
                    case 7:
                        octave = 8;
                        break;
                    case 8:
                        octave = 0;
                        break;
                }
                break;
        }
        dotCount = Math.abs(octave - 4);

        RelativeLayout.LayoutParams olp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        if(octave > 4) {
            olp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        else if(octave < 4 && hasBeamView) {
            olp.addRule(RelativeLayout.BELOW, R.id.beam_view);
        }
        else if(octave < 4 && !hasBeamView){
            olp.addRule(RelativeLayout.BELOW, R.id.number_view);
        }

        if(hasAccidentalView) {
            olp.addRule(RelativeLayout.RIGHT_OF, R.id.blank_view);
        }
        else {
            olp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        }
        this.setLayoutParams(olp);

        this.invalidate();
        if(octave == 5 || octave == 0)
            this.getParent().requestLayout();

        return super.onTouchEvent(event);
    }

    public void setOctave(int octave) {
        this.octave = octave;
        dotCount = Math.abs(octave - 4);
    }

    public String getOctave() {
        return Integer.toString(octave);
    }
}