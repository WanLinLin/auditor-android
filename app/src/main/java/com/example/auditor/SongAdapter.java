package com.example.auditor;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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
 * Created by Wan Lin on 15/7/2.
 * To adapt song list view
 */
public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInflater;
    private AudioFileListActivity audioFileListActivity;

    public SongAdapter(Context c, ArrayList<Song> theSongs){
        songs = theSongs;
        songInflater = LayoutInflater.from(c);
        this.audioFileListActivity = (AudioFileListActivity)c;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // map to song layout
        RelativeLayout songLayout = (RelativeLayout)songInflater.inflate(R.layout.file_item_view, parent, false);

        TextView songTitle = (TextView)songLayout.findViewById(R.id.file_title);
        TextView songModDate = (TextView)songLayout.findViewById(R.id.file_mod_date);
        final ImageButton collapse = (ImageButton)songLayout.findViewById(R.id.collapse);

        final Song currSong = songs.get(position);

        songTitle.setText(currSong.getTitle());
        songModDate.setText(currSong.getLastModDate());

        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(audioFileListActivity, collapse);
                popupMenu.getMenuInflater().inflate(R.menu.audio_file_popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getTitle().toString()) {
                            case "Convert":
                                new ConvertSongTask().execute(currSong);
                                break;

                            case "Rename":
                                audioFileListActivity.renameSong(currSong);
                                break;

                            case "Delete":
                                audioFileListActivity.deleteSong(currSong);
                                break;
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

    class ConvertSongTask extends AsyncTask<Song, Void, Boolean> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(audioFileListActivity, "Song converting...",
                    "Please wait...", true);
        }

        @Override
        protected Boolean doInBackground(Song... songs) {
            return audioFileListActivity.convertSong(songs[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.dismiss();
            if (aBoolean)
                Toast.makeText(audioFileListActivity, "Success!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(audioFileListActivity, "Convert failed!", Toast.LENGTH_SHORT).show();
        }
    }
}