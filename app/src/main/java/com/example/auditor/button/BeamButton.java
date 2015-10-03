package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.example.auditor.ShowScoreActivity;

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
                if (ShowScoreActivity.measureEditMode) {
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
                            setDuration("");
                            break;
                        case "":
                            setDuration("i");
                            break;
                    }
                    BeamButton.this.invalidate();
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int beamStrokeWidth = Math.round(getMeasuredHeight() * 0.12f);
        int space = Math.round(getMeasuredHeight() - beamStrokeWidth) / 3;
        float y = beamStrokeWidth / 2;

        mPaint.setStrokeWidth(beamStrokeWidth);
        for(int i = 0; i < lineCount; i++) {
            canvas.drawLine(getMeasuredWidth() / 6, y, getMeasuredWidth() - getMeasuredWidth() / 6, y, mPaint);
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
            case "":
                lineCount = 0;
                break;
        }
    }
}
