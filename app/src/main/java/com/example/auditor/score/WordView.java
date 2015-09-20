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
public class WordView extends View {
    private int noteViewGroupWidth;
    private int width;
    private int height;
    private Paint textPaint;
    private String word;

    public WordView(Context context, int width) {
        super(context);
        this.noteViewGroupWidth = width;
        this.height = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_HEIGHT;

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
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

        textPaint.setTextSize((int) (height * 0.5));
        textPaint.setTextAlign(Paint.Align.CENTER);

        int x = width / 2;
        int y = (int) ((height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));

        canvas.drawText(word, x, y, textPaint);
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }
}
