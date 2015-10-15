package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;
import com.example.auditor.score.AccidentalView;
import com.example.auditor.score.MeasureViewGroup;
import com.example.auditor.score.NoteViewGroup;
import com.example.auditor.score.PartViewGroup;
import com.example.auditor.score.WordView;

/**
 * Created by Wan Lin on 2015/10/2.
 * AccidentalButton
 */
public class AccidentalButton extends Button{
    private static String LOG_TAG = AccidentalButton.class.getName();
    private Paint mPaint;
    private RectF rect;
    private String accidental = "";
    private int width;
    private int height;

    public AccidentalButton(Context context) {
        super(context);
        init();
    }

    public AccidentalButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AccidentalButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(accidental.equals("")) return;

        int incline = getMeasuredHeight() / 15;

        int left = 0;
        int top = getMeasuredHeight() / 8;
        int right = getMeasuredWidth() - getMeasuredWidth() / 6;
        int bottom = getMeasuredHeight() - getMeasuredHeight() / 8;
        int ratioHeight = bottom - top;

        switch (accidental) {
            case "#":
                left = getMeasuredWidth() / 5;
                right = getMeasuredWidth() - getMeasuredWidth() / 5;
                int ratioWidth = right - left;

                float[] pos = {
                        // horizontal line
                        left, top + ratioHeight / 3 + incline, right, top + ratioHeight / 3 - incline,
                        left, top + ratioHeight * 2 / 3 + incline, right, top + ratioHeight * 2 / 3 - incline,

                        // vertical line
                        left + ratioWidth / 3, top + incline, left + ratioWidth / 3, bottom,
                        left + ratioWidth * 2 / 3, top, left + ratioWidth * 2 / 3, bottom - incline,
                };

                mPaint.setStrokeWidth(getMeasuredHeight() / 14); // draw horizontal line
                canvas.drawLines(pos, 0, 8, mPaint);
                mPaint.setStrokeWidth(getMeasuredHeight() / 20); // draw vertical line
                canvas.drawLines(pos, 8, 8, mPaint);
                break;
            case "b":
                float sweepAngle = 180f;
                float arcStrokeRatioWidth = getMeasuredHeight() / 15;
                float verticalLineWidth = getMeasuredHeight() / 18;

                // slightly move up
                bottom = bottom - ratioHeight / 10;

                // draw the variable width arc
                rect.set(left, top + ratioHeight / 1.8f, right - arcStrokeRatioWidth / 2, bottom);
                mPaint.setStrokeWidth(arcStrokeRatioWidth);
                canvas.drawArc(rect, 270, sweepAngle, false, mPaint);

                // draw the vertical line
                mPaint.setStrokeWidth(verticalLineWidth);
                canvas.drawLine((left + right) / 2, top, (left + right) / 2, bottom + arcStrokeRatioWidth / 2, mPaint);
                break;
        }
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);

        width = (int) (getResources().getDimension(R.dimen.default_note_edit_width) * 0.25);
        height = (int) (getResources().getDimension(R.dimen.default_note_edit_height) * 0.4);

        rect = new RectF();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShowScoreActivity.scoreEditMode) {
                    switch (accidental) {
                        case "#":
                            accidental = "b";
                            break;
                        case "b":
                            accidental = "";
                            break;
                        case "":
                            accidental = "#";
                            break;
                    }
                    AccidentalButton.this.invalidate();

                    AccidentalView accidentalView = (AccidentalView) NoteViewGroup.curEditNote.findViewById(R.id.accidental_view);
                    if (accidentalView != null) {
                        accidentalView.setAccidental(accidental);
                        NoteViewGroup.curEditNote.setHasAccidentalView(!accidental.equals(""));
                        WordView wordView = (WordView) MeasureViewGroup.curEditMeasure.findViewById(NoteViewGroup.curEditNote.getId() - ShowScoreActivity.noteStartId + ShowScoreActivity.wordStartId);
                        if (wordView != null) wordView.setHasAccidentalView(!accidental.equals(""));
                        MeasureViewGroup.curEditMeasure.requestLayout();
                        updateTieView();
                    }
                }
            }
        });
    }

    public void setAccidental(String accidental) {
        this.accidental = accidental;
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
