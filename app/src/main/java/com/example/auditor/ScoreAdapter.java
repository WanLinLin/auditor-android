package com.example.auditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/8/29.
 * To adapt score list view
 */
public class ScoreAdapter extends BaseAdapter {
    private ArrayList<Score> scores;
    private LayoutInflater scoreInflater;
    private ScoreFileListActivity scoreFileListActivity;

    public ScoreAdapter(Context c, ArrayList<Score> scores) {
        this.scores = scores;
        scoreInflater = LayoutInflater.from(c);
        this.scoreFileListActivity = (ScoreFileListActivity)c;
    }

    @Override
    public int getCount() {
        return scores.size();
    }

    @Override
    public Object getItem(int position) {
        return scores.get(position);
    }

    @Override
    public long getItemId(int position) {
        return scores.get(position).getID();
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        // map to song layout
        final RelativeLayout songLayout = (RelativeLayout)scoreInflater.inflate(R.layout.file_item_view, parent, false);
        final TextView scoreTitle = (TextView)songLayout.findViewById(R.id.file_title);
        final ImageButton collapse = (ImageButton)songLayout.findViewById(R.id.collapse);
        final Score score = scores.get(position);
        final TextView scoreModDate = (TextView)songLayout.findViewById(R.id.file_mod_date);

        scoreTitle.setText(score.getTitle());
        scoreModDate.setText(score.getLastModDate());
        songLayout.setTag(position);

        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(scoreInflater.getContext(), collapse);
                popupMenu.getMenuInflater().inflate(R.menu.score_file_popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Rename":
                                scoreFileListActivity.renameScore(score);
                                break;

                            case "Delete":
                                scoreFileListActivity.deleteScore(score);
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        return songLayout;
    }
}
