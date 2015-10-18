package com.example.auditor.button;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;
import com.example.auditor.score.NoteViewGroup;
import com.example.auditor.score.NumberView;
import com.example.auditor.score.OctaveView;

/**
 * Created by Wan Lin on 2015/10/2.
 * NumberButton
 */
public class NumberButton extends Button{
    private Paint mPaint;
    private String note;
    private int width;
    private int height;

    public NumberButton(Context context) {
        super(context);
        init();
    }

    public NumberButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLACK);

        width = (int) (getResources().getDimension(R.dimen.default_edit_note_group_width) * 0.5);
        height = (int) (getResources().getDimension(R.dimen.default_edit_note_group_height) * 0.4);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShowScoreActivity.scoreEditMode) {
                    switch (note) {
                        case "C":
                            note = "D";
                            break;
                        case "D":
                            note = "E";
                            break;
                        case "E":
                            note = "F";
                            break;
                        case "F":
                            note = "G";
                            break;
                        case "G":
                            note = "A";
                            break;
                        case "A":
                            note = "B";
                            break;
                        case "B":
                            note = "R";
                            break;
                        case "R":
                            note = "C";
                            break;
                    }
                    NumberButton.this.invalidate();

                    // prevent edit octave of rest note
                    if (note.equals("R")) { // it's rest note
                        OctaveButton.setOctave("");
                        ShowScoreActivity.topOctaveButton.setEnabled(false);
                        ShowScoreActivity.bottomOctaveButton.setEnabled(false);

                        // set current note octave to be empty string
                        OctaveView octaveView = (OctaveView) NoteViewGroup.curEditNote.findViewById(R.id.octave_view);
                        if (octaveView != null) {
                            octaveView.setOctave("");
                            octaveView.invalidate();
                        }
                    } else { // if it's a normal note, set octave to be 4
                        OctaveButton.setOctave("4");
                        ShowScoreActivity.topOctaveButton.setEnabled(true);
                        ShowScoreActivity.bottomOctaveButton.setEnabled(true);
                    }

                    NumberView numberView = (NumberView) NoteViewGroup.curEditNote.findViewById(R.id.number_view);
                    if (numberView != null) {
                        numberView.setNote(note);
                        numberView.invalidate();
                    }
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setTextSize(getMeasuredHeight());
        mPaint.setTextAlign(Paint.Align.CENTER);
        int x = getMeasuredWidth() / 2;
        int y = (int) ((getMeasuredHeight() / 2) - ((mPaint.descent() + mPaint.ascent()) / 2));

        switch(note) {
            case "C":
                canvas.drawText("1", x, y, mPaint);
                break;
            case "D":
                canvas.drawText("2", x, y, mPaint);
                break;
            case "E":
                canvas.drawText("3", x, y, mPaint);
                break;
            case "F":
                canvas.drawText("4", x, y, mPaint);
                break;
            case "G":
                canvas.drawText("5", x, y, mPaint);
                break;
            case "A":
                canvas.drawText("6", x, y, mPaint);
                break;
            case "B":
                canvas.drawText("7", x, y, mPaint);
                break;
            case "R":
                canvas.drawText("0", x, y, mPaint);
                break;
            case "-":
                canvas.drawText("-", x, y, mPaint);
                break;
        }
    }

    public void setNote(String note) {
        this.note = note;
    }
}