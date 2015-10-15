package com.example.auditor;

import android.app.ProgressDialog;
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

import java.text.DateFormat;
import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/7/2.
 * To adapt song list view
 */
public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInflater;
    private AudioFileListPage audioFileListPage;
    private SlidingTabActivity slidingTabActivity;

    public SongAdapter(SlidingTabActivity activity, ArrayList<Song> songs, AudioFileListPage audioFileListPage){
        this.songs = songs;
        this.audioFileListPage = audioFileListPage;
        songInflater = LayoutInflater.from(activity);
        slidingTabActivity = activity;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return songs.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return songs.get(arg0).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // map to song layout
        RelativeLayout songLayout = (RelativeLayout)songInflater.inflate(R.layout.file_item_view, parent, false);

        TextView songTitle = (TextView)songLayout.findViewById(R.id.file_title);
        TextView songModDate = (TextView)songLayout.findViewById(R.id.file_mod_date);
        final ImageButton collapse = (ImageButton)songLayout.findViewById(R.id.collapse);

        final Song currSong = songs.get(position);

        // not showing the filename extension
        songTitle.setText(currSong.getTitle().substring(0, currSong.getTitle().length() - 4));
        DateFormat sdf = DateFormat.getDateTimeInstance();
        songModDate.setText(sdf.format(currSong.getLastModDate()));

        collapse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(slidingTabActivity, collapse);
                popupMenu.getMenuInflater().inflate(R.menu.audio_file_popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getTitle().toString().equals(audioFileListPage.getString(R.string.convert)))
                            new ConvertSongTask().execute(currSong);
                        else if(item.getTitle().toString().equals(audioFileListPage.getString(R.string.rename)))
                            audioFileListPage.renameSong(currSong);
                        else if(item.getTitle().toString().equals(audioFileListPage.getString(R.string.delete)))
                            audioFileListPage.deleteSong(currSong);
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
            progress = ProgressDialog.show(slidingTabActivity, "Song converting...",
                    "Please wait...", true);
        }

        @Override
        protected Boolean doInBackground(Song... songs) {
            return audioFileListPage.convertSong(songs[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.dismiss();

            SlidingTabAdapter adapter = slidingTabActivity.getAdapter();
            ScoreFileListPage page = (ScoreFileListPage)adapter.getPage(SlidingTabAdapter.SCORE_FILE_LIST);
            page.refreshList();

            if (aBoolean)
                Toast.makeText(slidingTabActivity, slidingTabActivity.getString(R.string.success), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(slidingTabActivity, slidingTabActivity.getString(R.string.failed), Toast.LENGTH_SHORT).show();
        }
    }
}