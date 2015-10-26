package com.example.auditor.view;

import android.content.Context;
import android.widget.RelativeLayout;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/9/8.
 * A tie view group aligns a part view group top and stores numbers of ties.
 */
public class TieViewGroup extends RelativeLayout{

    public TieViewGroup(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(widthMeasureSpec, ShowScoreActivity.NoteChildViewDimension.TIE_VIEW_HEIGHT);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        for(int i = 0; i < getChildCount(); i++) {
            getChildAt(i).requestLayout();
        }
    }
}
