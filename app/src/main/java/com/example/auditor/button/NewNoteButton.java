package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.auditor.R;

/**
 * Created by Wan Lin on 2015/10/15.
 * NewNoteButton
 */
public class NewNoteButton extends View {
    private static final String LOG_TAG = "NewNoteButton";
    private Paint mPaint;

    public NewNoteButton(Context context) {
        super(context);
        init();
    }

    public NewNoteButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NewNoteButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) getResources().getDimension(R.dimen.new_note_button_size), (int) getResources().getDimension(R.dimen.new_note_button_size));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText("1", getMeasuredWidth()/2, getMeasuredHeight()/2, mPaint);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(getResources().getDimension(R.dimen.new_note_button_size));
        this.setBackgroundColor(Color.YELLOW);
    }
}
