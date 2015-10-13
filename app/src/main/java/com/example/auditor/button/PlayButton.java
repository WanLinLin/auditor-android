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
 * Created by Wan Lin on 2015/10/10.
 * PlayButton
 */
public class PlayButton extends View{
    private Paint mPaint;
    private Paint trianglePaint;
    private float buttonSize;
    private Path path;
    private Point point1_draw;
    private Point point2_draw;
    private Point point3_draw;
    private Rect touchRegion;

    /**
     * true is play button, false is pause button
     */
    private boolean play;

    public PlayButton(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        getHitRect(touchRegion);
        setMeasuredDimension((int) buttonSize, (int) buttonSize);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                PlayButton.this.setAlpha(0.6f);
                break;
            case MotionEvent.ACTION_UP:
                PlayButton.this.setAlpha(1f);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(play) {
            // draw circle
            mPaint.setStrokeWidth(getResources().getDimension(R.dimen.play_button_stroke_width));
            canvas.drawCircle(buttonSize / 2, buttonSize / 2, (buttonSize - 2 * mPaint.getStrokeWidth()) / 2, mPaint);

            // draw triangle
            point1_draw.set((int) (buttonSize / 5 * 2), (int) (buttonSize / 3));
            point2_draw.set((int) (buttonSize / 5 * 2), (int) (buttonSize - buttonSize / 3));
            point3_draw.set((int) (buttonSize - buttonSize / 3), (int) (buttonSize / 2));

            path.moveTo(point1_draw.x, point1_draw.y);
            path.lineTo(point2_draw.x, point2_draw.y);
            path.lineTo(point3_draw.x, point3_draw.y);
            path.close();
            canvas.drawPath(path, trianglePaint);
        }
        else {
            // draw circle
            mPaint.setStrokeWidth(getResources().getDimension(R.dimen.play_button_stroke_width));
            canvas.drawCircle(buttonSize / 2, buttonSize / 2, (buttonSize - 2 * mPaint.getStrokeWidth()) / 2, mPaint);

            mPaint.setStrokeWidth(getResources().getDimension(R.dimen.pause_button_stroke_width));
            canvas.drawLine((int) (buttonSize / 5 * 2), (int) (buttonSize / 3), (int) (buttonSize / 5 * 2), (int) (buttonSize - buttonSize / 3), mPaint);
            canvas.drawLine((int) (buttonSize - (buttonSize / 5 * 2)), (int) (buttonSize / 3), (int) (buttonSize - (buttonSize / 5 * 2)), (int) (buttonSize - buttonSize / 3), mPaint);
        }
    }

    private void init() {
        play = true;

        buttonSize = getResources().getDimension(R.dimen.play_button_size);

        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.AuditorColorAccent));
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);

        trianglePaint = new Paint();
        trianglePaint.setColor(getResources().getColor(R.color.AuditorColorAccent));
        trianglePaint.setAntiAlias(true);
        trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePaint.setStrokeWidth(getResources().getDimension(R.dimen.play_button_stroke_width));

        point1_draw = new Point();
        point2_draw = new Point();
        point3_draw = new Point();

        path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);

        touchRegion = new Rect();
    }

    public void setPlay(boolean play) {
        this.play = play;
    }
}
