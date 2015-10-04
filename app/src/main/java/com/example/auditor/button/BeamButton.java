package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;
import com.example.auditor.score.BeamView;
import com.example.auditor.score.NoteViewGroup;

/**
 * Created by wanlin on 2015/10/2.
 */
public class BeamButton extends Button{
    private Paint mPaint;
    private String duration;
    private int lineCount = 0;

    public BeamButton(Context context) {
        super(context);
        init();
    }

    public BeamButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BeamButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShowScoreActivity.scoreEditMode) {
                    switch (duration) {
                        case "i":
                            setDuration("s");
                            break;
                        case "s":
                            setDuration("t");
                            break;
                        case "t":
                            setDuration("x");
                            break;
                        case "x":
                            setDuration("q");
                            break;
                        case "q":
                            setDuration("i");
                            break;
                    }
                    BeamButton.this.invalidate();

                    BeamView beamView = (BeamView) NoteViewGroup.curEditNote.findViewById(R.id.beam_view);
                    beamView.setDuration(duration);
                    beamView.requestLayout();
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int beamStrokeWidth = (int) (getMeasuredHeight() * 0.075f);
        int padding = (int) (getMeasuredHeight() * 0.25);
        int space = (getMeasuredHeight() - 2 * padding - beamStrokeWidth) / 3;
        int y = padding + beamStrokeWidth / 2;

        mPaint.setStrokeWidth(beamStrokeWidth);
        for(int i = 0; i < lineCount; i++) {
            canvas.drawLine(getMeasuredWidth() / 8, y, getMeasuredWidth() - getMeasuredWidth() / 8, y, mPaint);
            y += space;
        }
    }

    public void setDuration(String duration) {
        this.duration = duration;

        switch(duration) {
            case "w":
                lineCount = 0;
                break;
            case "h":
                lineCount = 0;
                break;
            case "q":
                lineCount = 0;
                break;
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
}
