package com.example.auditor.score;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by Wan Lin on 15/8/4.
 * Keep Part
 */
public class Score extends RelativeLayout {
    private static final int partStartId = 10001;

    public Score(Context context) {
        super(context);

//        RelativeLayout.LayoutParams slp =
//                new RelativeLayout.LayoutParams(
//                        RelativeLayout.LayoutParams.WRAP_CONTENT,
//                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        FrameLayout.LayoutParams slp =
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);

        this.setLayoutParams(slp);
    }

    public void printPart(Part part, int i) {
        int partViewGroupId = i + partStartId;
        RelativeLayout.LayoutParams plp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        part.setId(partViewGroupId);

        if(partViewGroupId == partStartId) {
            plp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        else if(partViewGroupId > partStartId) {
            plp.addRule(RelativeLayout.BELOW, part.getId() - 1);
        }
        part.setLayoutParams(plp);
        this.addView(part);
    }
}