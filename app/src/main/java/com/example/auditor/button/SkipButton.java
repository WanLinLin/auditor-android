package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

import com.example.auditor.R;

/**
 * Created by Wan Lin on 2015/10/11.
 * SkipButton
 */
public class SkipButton extends View{
    private Paint mPaint;
    private Path path;
    private Point point1_draw;
    private Point point2_draw;
    private Point point3_draw;
    private float buttonSize;
    private Rect touchRegion;

    /**
     * true is next button, false is previous button
     */
    private boolean isNextButton;

    public SkipButton(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getHitRect(touchRegion);
        setMeasuredDimension((int) buttonSize, (int) buttonSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(isNextButton) { // is next button
            // draw triangle
            point1_draw.set((int) (buttonSize / 5 * 2), (int) (buttonSize / 3));
            point2_draw.set((int) (buttonSize / 5 * 2), (int) (buttonSize - buttonSize / 3));
            point3_draw.set((int) (buttonSize - buttonSize / 3), (int) (buttonSize / 2));

            path.moveTo(point1_draw.x, point1_draw.y);
            path.lineTo(point2_draw.x, point2_draw.y);
            path.lineTo(point3_draw.x, point3_draw.y);
            path.close();
            canvas.drawPath(path, mPaint);

            // draw line
            mPaint.setStrokeWidth(getResources().getDimension(R.dimen.pause_button_stroke_width));
            canvas.drawLine(point3_draw.x + mPaint.getStrokeWidth(), point1_draw.y, point3_draw.x + mPaint.getStrokeWidth(), point2_draw.y, mPaint);
        }
        else { // is previous button, rotate the next button 180 degrees
            // draw triangle
            point1_draw.set((int) (buttonSize - buttonSize / 5 * 2), (int) (buttonSize / 3));
            point2_draw.set((int) (buttonSize - buttonSize / 5 * 2), (int) (buttonSize - buttonSize / 3));
            point3_draw.set((int) (buttonSize / 3), (int) (buttonSize / 2));

            path.moveTo(point1_draw.x, point1_draw.y);
            path.lineTo(point2_draw.x, point2_draw.y);
            path.lineTo(point3_draw.x, point3_draw.y);
            path.close();
            canvas.drawPath(path, mPaint);

            // draw line
            mPaint.setStrokeWidth(getResources().getDimension(R.dimen.pause_button_stroke_width));
            canvas.drawLine(point3_draw.x - mPaint.getStrokeWidth(), point1_draw.y, point3_draw.x - mPaint.getStrokeWidth(), point2_draw.y, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                SkipButton.this.setAlpha(0.6f);
                break;
            case MotionEvent.ACTION_UP:
                SkipButton.this.setAlpha(1f);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void init() {
        buttonSize = getResources().getDimension(R.dimen.skip_button_size);

        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.AuditorColorAccent));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(getResources().getDimension(R.dimen.play_button_stroke_width));

        point1_draw = new Point();
        point2_draw = new Point();
        point3_draw = new Point();

        path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        touchRegion = new Rect();
    }

    public void setNextOrPrevious(boolean isNextButton) {
        this.isNextButton = isNextButton;
    }
}
