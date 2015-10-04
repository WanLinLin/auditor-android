package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/4.
 * Draw the number note
 */
public class NumberView extends View {
    private static final String LOG_TAG = NumberView.class.getName();
    private String note;
    private Paint mPaint;

    private int width;
    private int height;

    public NumberView(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);

        width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
        height = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_HEIGHT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(note.equals("")) setMeasuredDimension(0, 0);
        else {
            width = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
            height = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_HEIGHT;

            setMeasuredDimension(width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(note.equals("")) return;

        mPaint.setTextSize(height);
        mPaint.setTextAlign(Paint.Align.CENTER);
        int x = width / 2;
        int y = (int) ((height / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));

        switch(note) {
            case "C":
                canvas.drawText("1", x, y, mPaint);
                break;
            case "D":
                canvas.drawText("2", x, y, mPaint);
                break;
            case "E":
                canvas.drawText("3", x, y, mPaint);
                break;
            case "F":
                canvas.drawText("4", x, y, mPaint);
                break;
            case "G":
                canvas.drawText("5", x, y, mPaint);
                break;
            case "A":
                canvas.drawText("6", x, y, mPaint);
                break;
            case "B":
                canvas.drawText("7", x, y, mPaint);
                break;
            case "R":
                canvas.drawText("0", x, y, mPaint);
                break;
            case "-":
                canvas.drawText("-", x, y, mPaint);
                break;
        }
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }
}