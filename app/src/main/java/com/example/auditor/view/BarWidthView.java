package com.example.auditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/9/3.
 * A bar width view adds at the begin of a measure.
 */
public class BarWidthView extends View {
    private Paint mPaint;
    private int width;
    private int height;

    public BarWidthView(Context context) {
        super(context);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.TRANSPARENT);

        width = ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_HEIGHT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_HEIGHT;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, width, height, mPaint);
    }
}
