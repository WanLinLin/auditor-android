package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by wanlin on 15/9/10.
 */
public class WordView extends View{
    private int noteViewGroupWidth;
    private int width;
    private int height;
    private Paint mPaint;
    private String word;

    public WordView(Context context, int width) {
        super(context);
        this.noteViewGroupWidth = width;
        this.height = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_HEIGHT;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        height = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_HEIGHT;
        width = (int) (noteViewGroupWidth * ShowScoreActivity.mScaleFactor);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setTextSize((int) (height * 0.5));
        mPaint.setTextAlign(Paint.Align.CENTER);

        int x = width / 2;
        int y = (int) ((height / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));

        canvas.drawText(word, x, y, mPaint);
    }

    public void setWord(String word) {
        this.word = word;
    }
}
