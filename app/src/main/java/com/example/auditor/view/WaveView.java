package com.example.auditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.auditor.AudioRecordPage;
import com.example.auditor.R;

/**
 * Created by Wan Lin on 2015/10/25.
 * WaveView, show the voice pressure
 */
public class WaveView extends View{
    private Paint mPaint;
    private int viewSize;
    private int recordButtonSize;
    private double pressure;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        viewSize = (int)getResources().getDimension(R.dimen.wave_view_size);
        recordButtonSize = (int)getResources().getDimension(R.dimen.record_button_size);
        pressure = AudioRecordPage.minLevel;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.White));
        mPaint.setAlpha(80);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(recordButtonSize, recordButtonSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        double radius = Math.abs(pressure - AudioRecordPage.minLevel) * (recordButtonSize/2) / (AudioRecordPage.maxLevel - AudioRecordPage.minLevel);

        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, (float) radius, mPaint);
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }
}