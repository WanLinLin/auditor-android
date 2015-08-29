package com.example.auditor.score;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.auditor.R;

import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/8/29.
 * To adapt score list view
 */
public class ScoreAdapter extends BaseAdapter {
    private ArrayList<Score> scores;
    private LayoutInflater scoreInflater;

    public ScoreAdapter(Context c, ArrayList<Score> scores) {
        this.scores = scores;
        scoreInflater = LayoutInflater.from(c);
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
    public View getView(int position, View convertView, ViewGroup parent) {
        // map to song layout
        final RelativeLayout songLayout = (RelativeLayout)scoreInflater.inflate(R.layout.file_item_view, parent, false);

        final TextView scoreTitle = (TextView)songLayout.findViewById(R.id.file_title);
        TextView scoreModDate = (TextView)songLayout.findViewById(R.id.file_mod_date);

        final Score score = scores.get(position);

        scoreTitle.setText(score.getTitle());
        scoreModDate.setText(score.getLastModDate());
        songLayout.setTag(position);

        return songLayout;
    }
}
