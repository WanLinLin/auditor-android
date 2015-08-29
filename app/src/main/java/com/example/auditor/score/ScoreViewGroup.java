package com.example.auditor.score;

import android.content.Context;
import android.widget.RelativeLayout;

/**
 * Created by Wan Lin on 15/8/4.
 * Keep Part
 */
public class ScoreViewGroup extends RelativeLayout {
    private static final int partStartId = 10001;

    public ScoreViewGroup(Context context) {
        super(context);

        RelativeLayout.LayoutParams slp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        this.setLayoutParams(slp);
    }

    public void printPart(PartViewGroup partViewGroup, int i) {
        int partViewGroupId = i + partStartId;
        RelativeLayout.LayoutParams plp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        partViewGroup.setId(partViewGroupId);

        if(partViewGroupId == partStartId) {
            plp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        else if(partViewGroupId > partStartId) {
            plp.addRule(RelativeLayout.BELOW, partViewGroup.getId() - 1);
        }
        partViewGroup.setLayoutParams(plp);
        this.addView(partViewGroup);
    }
}