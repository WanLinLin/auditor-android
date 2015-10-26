package com.example.auditor.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.auditor.R;


/**
 * Created by Wan Lin on 2015/10/25.
 * RecordButton
 */
public class RecordButton extends View{
    private Paint mPaint;
    private Bitmap recordImg;
    private float radius;
    private float size;

    public RecordButton(Context context) {
        super(context);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RecordButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        radius = getResources().getDimension(R.dimen.record_button_size) / 2 - getResources().getDimension(R.dimen.record_button_border_stroke_width);
        size = radius / 2;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(getResources().getDimension(R.dimen.record_button_border_stroke_width));
        recordImg = BitmapFactory.decodeResource(getResources(), R.drawable.mic);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(getResources().getColor(R.color.White));
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, radius, mPaint);

        ColorFilter filter = new LightingColorFilter(getResources().getColor(R.color.White), 1);
        mPaint.setColorFilter(filter);

        canvas.drawBitmap(recordImg,
                null,
                new Rect(
                    (int) (getMeasuredWidth()/2 - size/2),
                    (int) (getMeasuredHeight()/2 - size/2),
                    (int) (getMeasuredWidth()/2 + size/2),
                    (int) (getMeasuredHeight()/2 + size/2)),
                mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                RecordButton.this.setAlpha(0.6f);
                break;
            case MotionEvent.ACTION_UP:
                RecordButton.this.setAlpha(1f);
                break;
            case MotionEvent.ACTION_CANCEL:
                RecordButton.this.setAlpha(1f);
                break;
        }
        return super.onTouchEvent(event);
    }
}
