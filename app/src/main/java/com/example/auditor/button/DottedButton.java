package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;
import com.example.auditor.score.DottedView;
import com.example.auditor.score.MeasureViewGroup;
import com.example.auditor.score.NoteViewGroup;
import com.example.auditor.score.PartViewGroup;
import com.example.auditor.score.WordView;

/**
 * Created by wanlin on 2015/10/3.
 */
public class DottedButton extends Button{
    private static String LOG_TAG = DottedButton.class.getName();
    private Paint mPaint;
    private String dot;

    public DottedButton(Context context) {
        super(context);
        init();
    }

    public DottedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DottedButton(Context context, AttributeSet attrs, int defStyleAttr) {
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
                if (ShowScoreActivity.scoreEditMode) {
                    if (dot.equals(".")) dot = "";
                    else dot = ".";

                    DottedButton.this.invalidate();

                    DottedView dottedView = (DottedView) NoteViewGroup.curEditNote.findViewById(R.id.dotted_view);
                    if (dottedView != null) {
                        dottedView.setLength(dot);
                        NoteViewGroup.curEditNote.setHasDottedView(!dot.equals(""));
                        WordView wordView = (WordView) MeasureViewGroup.curEditMeasure.findViewById(NoteViewGroup.curEditNote.getId() - ShowScoreActivity.noteStartId + ShowScoreActivity.wordStartId);
                        if (wordView != null) wordView.setHasDottedView(!dot.equals(""));
                        MeasureViewGroup.curEditMeasure.requestLayout();
                        updateTieView();
                    }
                }
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(dot.equals(".")) {
            float dotRadius = (int) (getMeasuredHeight() * 0.04f);
            canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, dotRadius, mPaint);
        }
    }

    public void setDot(String dot) {
        this.dot = dot;
    }

    public String getDot() {
        return dot;
    }

    private void updateTieView() {
        for (int i = 0; i < ShowScoreActivity.partMaxNumber; i++) {
            PartViewGroup part = (PartViewGroup)ShowScoreActivity.score.findViewById(i + ShowScoreActivity.partStartId);
            if (part == null) break;

            int curX = 0;
            part.clearTieInfo();
            part.getTieViewGroup().removeAllViews();

            for(int j = 0; j < ShowScoreActivity.partStartId - ShowScoreActivity.measureStartId; j++) {
                MeasureViewGroup measure = (MeasureViewGroup)part.findViewById(j + ShowScoreActivity.measureStartId);
                if (measure == null) break;

                curX += ShowScoreActivity.NoteChildViewDimension.BAR_VIEW_WIDTH;

                for(int k = 0; k < ShowScoreActivity.measureStartId - ShowScoreActivity.noteStartId; k++) {
                    NoteViewGroup note = (NoteViewGroup)measure.findViewById(k + ShowScoreActivity.noteStartId);
                    if (note == null) break;

                    int noteViewWidth = ShowScoreActivity.NoteChildViewDimension.NUMBER_VIEW_WIDTH;
                    if(note.hasAccidentalView())
                        noteViewWidth += ShowScoreActivity.NoteChildViewDimension.ACCIDENTAL_VIEW_WIDTH;
                    if(note.hasDottedView())
                        noteViewWidth += ShowScoreActivity.NoteChildViewDimension.DOTTED_VIEW_WIDTH;

                    if(note.isTieEnd()) {
                        part.addTieInfo(new Pair<>((curX + noteViewWidth/2), "end"));
                    }
                    if(note.isTieStart()) {
                        part.addTieInfo(new Pair<>((curX + noteViewWidth/2), "start"));
                    }
                    curX += noteViewWidth;
                }
            }
            part.printTieView();
            part.getTieViewGroup().requestLayout();
        }
    }
}