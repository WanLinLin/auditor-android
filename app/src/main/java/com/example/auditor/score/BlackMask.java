package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/9/24.
 * Add a black mask on root view to highlight the part user want to edit.
 */
public class BlackMask extends View{
    private int top;
    private int left;
    private int right;
    private int bottom;

    private int width;
    private int height;

    private Paint mPaint;

    public BlackMask(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAlpha(100);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, width, top, mPaint);
        canvas.drawRect(0, top, left, bottom, mPaint);
        canvas.drawRect(right + ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH, top, width, bottom, mPaint);
        canvas.drawRect(0, bottom, width, height, mPaint);
    }

    public void setDimension(int left, int top, int right, int bottom, int width, int height){
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;

        this.width = width;
        this.height = height;
    }
}
