package com.example.auditor;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;
import android.widget.Toast;

import com.example.auditor.convert.SongConverter;
import com.example.auditor.song.MusicService;
import com.example.auditor.song.Song;
import com.example.auditor.song.SongAdapter;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AudioFileListActivity extends ActionBarActivity implements MediaPlayerControl{
    private static final String LOG_TAG = "AudioFileActivity";
    private File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");
    private MediaController controller;
    private MusicService musicService;
    private ArrayList<Song> songList;
    private SongAdapter songAdt;
    private Intent playIntent;
    private ServiceConnection musicConnection;
    private BroadcastReceiver notificationReceiver;
    private boolean musicBound = false;
    private boolean paused = false;
    private boolean playbackPaused = false;
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_file);

        songList = new ArrayList<>();
        musicConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
                    musicService = binder.getService();
                    musicService.setList(songList);
                    musicBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    musicBound = false;
                }
            };

        // set notification receiver to handle message from music service
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                // When music player has been prepared, show controller
                Log.e(LOG_TAG, "onReceive: " + i.getStringExtra("action"));
                switch (i.getStringExtra("action")) {
                    case "prepared":
                        controller.show(0);
                        controller.onFinishInflate();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter("Player");
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver, intentFilter);

        setController();

        // prepare the song list
        getSongList();

        final ListView songListView = (ListView) findViewById(R.id.song_list);
        songAdt = new SongAdapter(this, songList);
        songListView.setAdapter(songAdt);
        songListView.setTextFilterEnabled(true);
        songListView.setOnScrollListener(
                new AbsListView.OnScrollListener() {
                    private int mLastFirstVisibleItem;

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        // do nothing
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                        if (mLastFirstVisibleItem < firstVisibleItem) {
                            controller.hide();
                        } else if (mLastFirstVisibleItem > firstVisibleItem) {
                            controller.show(0);
                        }
                        mLastFirstVisibleItem = firstVisibleItem;
                    }
                }
        );
        songListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                songPicked(view);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(paused){
            paused = false;
            controller.show(0);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        paused = true;
        if (controller.isShowing())
            controller.hide();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (controller.isShowing())
            controller.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (controller.isShowing())
            controller.hide();

        stopService(playIntent);
        unbindService(musicConnection);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
        notificationReceiver = null;
        controller = null;
        musicService = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (controller.isShowing())
            controller.hide();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicService.setShuffle();
                if (musicService.isShuffle())
                    item.getIcon().setAlpha(70);
                else
                    item.getIcon().setAlpha(255);

                break;
            case R.id.action_end:
                stopService(playIntent);
                musicService = null;
                System.exit(0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService != null && musicBound && musicService.isPlaying())
            return musicService.getPosition();
        else return 0;
    }

    @Override
    public int getDuration() {
        if(musicService !=null && musicBound && musicService.isPlaying())
            return musicService.getDur();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        if(musicService != null && musicBound)
            return musicService.isPlaying();

        return false;
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public void start() {
        musicService.go();
    }

    // do nothing
    @Override
    public int getBufferPercentage() {
        return 0;
    }

    // do nothing
    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController(){
        //set the controller up
        if (controller == null) {
            controller = new MediaController(this);
        }

        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    public void getSongList() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".wav");
            }
        };
        File[] files = auditorDir.listFiles(filter);

        for (int i = 0; i < files.length; i++) {
            DateFormat sdf = DateFormat.getDateTimeInstance();

            File file = files[i];
            Date lastModDate = new Date(file.lastModified());
            String lmd = sdf.format(lastModDate);
            String songTitle = file.getName().substring(0, file.getName().length() - 4);
            Song song = new Song(i, songTitle, lmd);
            songList.add(song);
        }
    }

    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();

        if (playbackPaused) {
            playbackPaused = false;
        }
    }

    private void playNext() {
        musicService.playNext();
        if(playbackPaused){
            playbackPaused = false;
        }
    }

    private void playPrev() {
        musicService.playPrev();
        if (playbackPaused) {
            playbackPaused = false;
        }
    }

    public void renameSong(final Song song) {
        // get audio_record_popup_rename.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.audio_record_popup_rename, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File from = new File(auditorDir + "/" + song.getTitle() + ".wav");
                                File to = new File(
                                        auditorDir + "/" + userInput.getText() + ".wav");
                                from.renameTo(to);

                                // update song list view and reset songList of musicService
                                songList.clear();
                                getSongList();
                                musicService.setList(songList);
                                songAdt.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void deleteSong(final Song song) {
        // get audio_record_popup_delete.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.audio_file_popup_delete, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set audio_record_popup_delete.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView dialogTitle = (TextView) promptsView
                .findViewById(R.id.popupWindowTitle);
        dialogTitle.setText(dialogTitle.getText() + song.getTitle() + "?");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File fileToDelete = new File(auditorDir + "/" + song.getTitle());
                                if (fileToDelete.delete())
                                    Toast.makeText(
                                            AudioFileListActivity.this,
                                            "Delete successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                else
                                    Toast.makeText(
                                            AudioFileListActivity.this,
                                            "Delete failed!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                // update song list view and reset songList of musicService
                                songList.clear();
                                getSongList();
                                musicService.setList(songList);
                                songAdt.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public boolean convertSong(final Song song) {
        SongConverter songConverter = new SongConverter(song.getTitle());

        if(!songConverter.setUp())
            return false;

        songConverter.convert();
        return true;
    }
}

// TODO http://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764,
// http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778,
// http://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787