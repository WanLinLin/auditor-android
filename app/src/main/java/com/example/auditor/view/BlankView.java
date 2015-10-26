package com.example.auditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.auditor.R;

/**
 * Created by Wan Lin on 2015/10/3.
 * BlankView
 */
public class BlankView extends View{
    private Paint mPaint;
    private int width;
    private int height;

    public BlankView(Context context) {
        super(context);
        init();
    }

    public BlankView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlankView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.TRANSPARENT);

        width = (int) (getResources().getDimension(R.dimen.default_edit_note_group_width) * 0.25);
        height = (int) (getResources().getDimension(R.dimen.default_edit_note_group_height) * 0.225);
    }
}
