package com.example.auditor.score;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.auditor.ShowSheetMusicActivity;

/**
 * Created by Wan Lin on 15/8/20.
 * Store measures.
 */
public class PartViewGroup extends RelativeLayout{
    private static final String LOG_TAG = "PartViewGroup";
    private Context context;
    private Paint mPaint;
    private float barWidth;

    public PartViewGroup(Context context) {
        super(context);
        this.context = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        barWidth = ShowSheetMusicActivity.noteHeight * 0.05f;
    }

    public void addMeasure(View measure, int measureIndex) {
        // first bar
        if(measureIndex == 0) {
            BarView barView = new BarView(context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = (int)barWidth;
            lp.rightMargin = (int)barWidth;
            barView.setLayoutParams(lp);
            this.addView(barView);
        }

        // add this measure
        RelativeLayout.LayoutParams rlp = new LayoutParams(getLayoutParams());
        rlp.leftMargin = (int)(3 * barWidth);
        if(measureIndex > 0)
            rlp.addRule(RIGHT_OF, measureIndex); // previous measure id
        else
            rlp.addRule(ALIGN_PARENT_LEFT);
        measure.setLayoutParams(rlp);
        this.addView(measure);

        // add a new bar right of this measure
        BarView barView = new BarView(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RIGHT_OF, measure.getId());
        lp.leftMargin = (int)barWidth;
        lp.rightMargin = (int)barWidth;
        barView.setLayoutParams(lp);
        this.addView(barView);
    }

    private class BarView extends View{
        public BarView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            mPaint.setStrokeWidth(barWidth);
            canvas.drawLine(getPaddingLeft(), getPaddingTop(), getPaddingLeft(), ShowSheetMusicActivity.noteHeight, mPaint);
        }
    }
}
