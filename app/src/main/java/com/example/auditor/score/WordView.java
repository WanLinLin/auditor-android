package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/9/10.
 * WordView
 */
public class WordView extends View {
    private int width;
    private int height;
    private Paint textPaint;
    private String word;
    private boolean hasAccidentalView;
    private boolean hasDottedView;

    public WordView(Context context) {
        super(context);
        init();
    }

    public WordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WordView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WordView(Context context, boolean hasAccidentalView, boolean hasDottedView) {
        super(context);
        this.height = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_HEIGHT;
        this.hasAccidentalView = hasAccidentalView;
        this.hasDottedView = hasDottedView;

        init();
    }

    private void init() {
        height = ShowScoreActivity.NoteChildViewDimension.WORD_VIEW_HEIGHT;
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize((int) (height * 0.6));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.WORD_VIEW_HEIGHT;
        if(hasAccidentalView)
            width += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
        if(hasDottedView)
            width += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

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

    public boolean hasAccidentalView() {
        return hasAccidentalView;
    }

    public void setHasAccidentalView(boolean hasAccidentalView) {
        this.hasAccidentalView = hasAccidentalView;
    }

    public boolean hasDottedView() {
        return hasDottedView;
    }

    public void setHasDottedView(boolean hasDottedView) {
        this.hasDottedView = hasDottedView;
    }
}
