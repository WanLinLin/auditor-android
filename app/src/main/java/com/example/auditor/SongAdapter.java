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
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by wanlin on 15/7/2.
 */
public class SongAdapter extends BaseAdapter {
    private static final String LOG_TAG = "SongAdapter";
    private ArrayList<Song> songs;
    private LayoutInflater songInflater;
    private AudioFileActivity audioFileActivity;

    public SongAdapter(Context c, ArrayList<Song> theSongs, AudioFileActivity a){
        songs = theSongs;
        songInflater = LayoutInflater.from(c);
        audioFileActivity = a;
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
        RelativeLayout songLayout = (RelativeLayout)songInflater.inflate(R.layout.song, parent, false);

        TextView songTitle = (TextView)songLayout.findViewById(R.id.song_title);
        TextView songModDate = (TextView)songLayout.findViewById(R.id.song_mod_date);
        final ImageButton collapse = (ImageButton)songLayout.findViewById(R.id.collapse);

        final Song currSong = songs.get(position);

        songTitle.setText(currSong.getTitle());
        songModDate.setText(currSong.getLastModDate().toString());

        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(audioFileActivity, collapse);
                popupMenu.getMenuInflater().inflate(R.menu.audio_file_popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Convert":
                                if (!audioFileActivity.convertSong(currSong))
                                    Toast.makeText(audioFileActivity, "Convert failed!", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(audioFileActivity, "Success!", Toast.LENGTH_SHORT).show();
                                break;

                            case "Rename":
                                audioFileActivity.renameSong(currSong);
                                break;

                            case "Delete":

                                audioFileActivity.deleteSong(currSong);
                        }

                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        songLayout.setTag(position);

        return songLayout;
    }

    public void setSongs(ArrayList<Song> newSongs) {
        songs = newSongs;
    }
}
// TODO set collapse button onClickListener