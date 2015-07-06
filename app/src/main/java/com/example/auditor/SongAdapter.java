package com.example.auditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wanlin on 15/7/2.
 */
public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInflater;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs = theSongs;
        songInflater = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // map to song layout
        LinearLayout songLayout = (LinearLayout)songInflater.inflate(R.layout.song, parent, false);

        TextView songView = (TextView)songLayout.findViewById(R.id.song_title);

        Song currSong = songs.get(position);

        songView.setText(currSong.getTitle());
        songLayout.setTag(position);

        return songLayout;
    }

}