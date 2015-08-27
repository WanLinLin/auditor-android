package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created by Wan Lin on 15/8/4.
 * Draw the number note
 */
public class NumberView extends View {
    private String note;
    private Paint mPaint;

    public NumberView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // env init
        mPaint.setTextSize(getMeasuredHeight());
        mPaint.setTextAlign(Paint.Align.CENTER);
        int x = canvas.getWidth() / 2;
        int y = (int) ((canvas.getHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));

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
                Log.e(getClass().getName(), "Rest");
                canvas.drawText("0", x, y, mPaint);
                break;
        }
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
    }

    public void setNote(String note) {
        this.note = note;
    }
}
