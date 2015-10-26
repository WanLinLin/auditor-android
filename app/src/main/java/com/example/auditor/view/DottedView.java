package com.example.auditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/5.
 * Draw note length
 */
public class DottedView extends View{
    private Paint mPaint;
    private String dot;

    private int width;
    private int height;

    public DottedView(Context context) {
        super(context);
        init();

        width = ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_HEIGHT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(dot.equals("")) setMeasuredDimension(0, 0);
        else {
            width = ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;
            height = ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_HEIGHT;
            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(dot.equals("")) return;

        // only draw one dot because Jfugue only support one dot duration
        if(dot.equals(".")) {
            float dotRadius = Math.round(width / 8);
            canvas.drawCircle(0 + dotRadius, height/2, dotRadius, mPaint);
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

    public String getDot() {
        return dot;
    }
}
