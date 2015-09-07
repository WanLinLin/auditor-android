package com.example.auditor.score;

import android.content.Context;
import android.widget.RelativeLayout;

import com.example.auditor.ShowScoreActivity;

/**
 * Created by Wan Lin on 15/8/4.
 * Keep Part
 */
public class ScoreViewGroup extends RelativeLayout {
    private ShowScoreActivity showScoreActivity;

    public ScoreViewGroup(Context context) {
        super(context);
        this.showScoreActivity = (ShowScoreActivity)context;

        LayoutParams slp = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                );

        this.setLayoutParams(slp);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();

        for(int i = 0; i < getChildCount(); i++) {
            getChildAt(i).requestLayout();
        }
    }

    public void printPart(PartViewGroup partViewGroup, int i) {
        int partViewGroupId = i + ShowScoreActivity.partStartId;
        RelativeLayout.LayoutParams plp =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

        partViewGroup.setId(partViewGroupId);

        if(partViewGroupId == ShowScoreActivity.partStartId) {
            plp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        else if(partViewGroupId > ShowScoreActivity.partStartId) {
            plp.addRule(RelativeLayout.BELOW, partViewGroup.getId() - 1);
        }
        partViewGroup.setLayoutParams(plp);
        this.addView(partViewGroup);
    }

    public void printInfo() {
        String songName = "大海";
        String singer = "張雨生";
        String keySignature = "1 = C";
        String author = "陳大力" + " 詞";
        String composer = "陳秀男" + " 曲";

//        TextView songNameView = (TextView)showScoreActivity.findViewById(R.id.info_song_name);
//        songNameView.setText(songName);
//
//        TextView singerView = (TextView)showScoreActivity.findViewById(R.id.info_singer);
//        singerView.setText(singer);
//
//        TextView keySignatureView = (TextView)showScoreActivity.findViewById(R.id.info_key_signature);
//        keySignatureView.setText(keySignature);
//
//        TextView authorView = (TextView)showScoreActivity.findViewById(R.id.info_author);
//        authorView.setText(author);
//
//        TextView composerView = (TextView)showScoreActivity.findViewById(R.id.info_composer);
//        composerView.setText(composer);
    }
}