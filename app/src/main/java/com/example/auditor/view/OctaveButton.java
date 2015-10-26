package com.example.auditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.example.auditor.R;
import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 2015/10/3.
 * OctaveButton
 */
public class OctaveButton extends Button {
    private Paint mPaint;
    private int dotCount;
    private boolean position; // true = top, false = bottom
    private static String octave = "";
    private int width;
    private int height;

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

        width = (int) (getResources().getDimension(R.dimen.default_edit_note_group_width) * 0.5);
        height = (int) (getResources().getDimension(R.dimen.default_edit_note_group_height) * 0.225);

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ShowScoreActivity.scoreEditMode) { // is score edit mode
                    if (position) { // is top octave button
                        if (Integer.parseInt(octave) < 4) octave = "5";
                        else {
                            switch (octave) {
                                case "4":
                                    octave = "5";
                                    break;
                                case "5":
                                    octave = "6";
                                    break;
                                case "6":
                                    octave = "7";
                                    break;
                                case "7":
                                    octave = "8";
                                    break;
                                case "8":
                                    octave = "4";
                                    break;
                            }
                        }
                    }
                    else { // is bottom octave button
                        if (Integer.parseInt(octave) > 4) octave = "3";
                        else {
                            switch (octave) {
                                case "4":
                                    octave = "3";
                                    break;
                                case "3":
                                    octave = "2";
                                    break;
                                case "2":
                                    octave = "1";
                                    break;
                                case "1":
                                    octave = "0";
                                    break;
                                case "0":
                                    octave = "4";
                                    break;
                            }
                        }
                    }

                    ShowScoreActivity.topOctaveButton.invalidate();
                    ShowScoreActivity.bottomOctaveButton.invalidate();

                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.addRule(RelativeLayout.RIGHT_OF, R.id.accidental_view);

                    if (!octave.equals("")) {
                        if (Integer.parseInt(octave) > 4)
                            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        else if (Integer.parseInt(octave) < 4)
                            lp.addRule(RelativeLayout.BELOW, R.id.beam_view);
                    }

                    OctaveView octaveView = (OctaveView) NoteViewGroup.curEditNote.findViewById(R.id.octave_view);
                    octaveView.setLayoutParams(lp);
                    octaveView.setOctave(octave);
                    octaveView.requestLayout();
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
        if(octave.equals("")) return;
        else dotCount = Math.abs(Integer.parseInt(octave) - 4);
        if(dotCount == 0) return;

        if(position && Integer.parseInt(octave) < 4) return;
        if(!position && Integer.parseInt(octave) > 4) return;

        int dotRadius = (int) (getMeasuredHeight() * 0.06f);
        int padding = (int) (getMeasuredHeight() * 0.23f);
        int center_x = getMeasuredWidth() / 2;
        int center_y;
        int tab = (getMeasuredHeight() - 2 * padding - dotRadius) / 3;

        if(position) {
            center_y = getMeasuredHeight() - padding - dotRadius;
            tab = -tab;
        }
        else {
            center_y = padding + dotRadius;
        }

        for(int i = 0; i < dotCount; i++) {
            canvas.drawCircle(center_x, center_y, dotRadius, mPaint);
            center_y += tab;
        }
    }

    public static String getOctave() {
        return octave;
    }

    public static void setOctave(String octave) {
        OctaveButton.octave = octave;
    }

    public boolean getPosition() {
        return position;
    }

    public void setPosition(boolean position) {
        this.position = position;
    }
}
