package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by wanlin on 2015/10/3.
 */
public class OctaveButton extends Button {
    private Paint mPaint;
    private int dotCount;
    private boolean position; // true = top, false = bottom

    public OctaveButton(Context context) {
        super(context);
        init();
    }

    public OctaveButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OctaveButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ShowScoreActivity.measureEditMode) {
                    switch (dotCount) {
                        case 1:
                            dotCount = 2;
                            break;
                        case 2:
                            dotCount = 3;
                            break;
                        case 3:
                            dotCount = 4;
                            break;
                        case 4:
                            dotCount = 0;
                            break;
                        case 0:
                            dotCount = 1;
                            break;
                    }
                    OctaveButton.this.invalidate();
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(dotCount == 0) return;

        int padding = Math.round(getMeasuredHeight() * 0.1f);
        int dotRadius = Math.round(getMeasuredHeight() * 0.083f);
        int center_x = getMeasuredWidth() / 2;
        int center_y = getMeasuredHeight();
        float tab = Math.round((getMeasuredHeight() - 2 * dotRadius - padding) / 3);

        if(position) {
            tab = -tab;
            center_y -= dotRadius;
        }
        else {
            center_y = padding + dotRadius;
        }

        for(int i = 0; i < dotCount; i++) {
            canvas.drawCircle(center_x, center_y, dotRadius, mPaint);
            center_y += tab;
        }
    }

    public int getDotCount() {
        return dotCount;
    }

    public void setDotCount(int dotCount) {
        this.dotCount = dotCount;
    }

    public boolean getPosition() {
        return position;
    }

    public void setPosition(boolean position) {
        this.position = position;
    }
}
