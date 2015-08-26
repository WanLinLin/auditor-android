package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Wan Lin on 15/8/5.
 * Draw note length
 */
public class DottedView extends View{
    private Paint mPaint;
    private String dot;

    public DottedView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int bottom = getHeight() - getPaddingBottom();
        int widthWithoutPadding = right - left;
        int heightWithoutPadding = bottom - top;

        // only draw one dot because Jfugue only support one dot duration
        if(dot.equals(".")) {
            float dotRadius = widthWithoutPadding/8;
            canvas.drawCircle(left + dotRadius, getHeight()/2, dotRadius, mPaint);
        }
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
    }

    public void setLength(String dot) {
        this.dot = dot;
    }
}
