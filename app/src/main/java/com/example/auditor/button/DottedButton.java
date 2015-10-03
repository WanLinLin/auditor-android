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
 * Created by wanlin on 2015/10/3.
 */
public class DottedButton extends Button{
    private Paint mPaint;
    private String dot;

    public DottedButton(Context context) {
        super(context);
        init();
    }

    public DottedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DottedButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShowScoreActivity.measureEditMode) {
                    if (dot.equals(".")) dot = "";
                    else dot = ".";
                }
                DottedButton.this.invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(dot.equals(".")) {
            float dotRadius = Math.round(getMeasuredWidth() / 8);
            canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, dotRadius, mPaint);
        }
    }

    public void setDot(String dot) {
        this.dot = dot;
    }

    public String getDot() {
        return dot;
    }
}