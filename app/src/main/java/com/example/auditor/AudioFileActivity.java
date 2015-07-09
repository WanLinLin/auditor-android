package com.example.auditor;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

import java.io.File;
import java.util.ArrayList;


public class AudioFileActivity extends ActionBarActivity implements MediaPlayerControl{
    private static final String LOG_TAG = "AudioFileActivity";
    private File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");
    private MediaController controller;
    private ArrayList<Song> songList = new ArrayList<>();
    private MusicService musicService;
    private ListView songView;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false;
    private boolean playbackPaused = false;
    private BroadcastReceiver notificationReceiver;
    private ServiceConnection musicConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_file);
//        Log.e(LOG_TAG, "onCreate");

        musicConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.e(LOG_TAG, "onServiceConnected");
                MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
                //get service
                musicService = binder.getService();
                //pass list
                musicService.setList(songList);
                musicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(LOG_TAG, "onServiceDisconnected");
                musicBound = false;
            }
        };
        setController();

        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
            // When music player has been prepared, show controller
            Log.e(LOG_TAG, "onReceive: " + i.getAction());
                switch (i.getAction()) {
                    case "MEDIA_PLAYER_PREPARED":
                        try {
                            controller.show(0);
                        }
                        catch (Exception e) {
                            controller = null;
                            setController();
                        }
                        break;
                }
            }
        };

        // prepare the song list
        getSongList();
        songView = (ListView) findViewById(R.id.song_list);
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
        songView.setTextFilterEnabled(true);

        songView.setOnScrollListener(
            new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    // do nothing
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    controller.hide();
                }
            }
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Log.e(LOG_TAG, "onStart");
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onResume(){
//        Log.e(LOG_TAG, "onResume");
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));

        if(paused){
            paused = false;
            controller.show(0);
        }
    }

    @Override
    protected void onPause(){
//        Log.e(LOG_TAG, "onPause");
        if (controller.isShowing())
            controller.hide();
        paused = true;
        super.onPause();
    }

    @Override
    protected void onStop() {
//        Log.e(LOG_TAG, "onStop");
        if (controller.isShowing())
            controller.hide();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        Log.e(LOG_TAG, "onDestroy");
        stopService(playIntent);
        unbindService(musicConnection);
        controller = null;

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
//        Log.e(LOG_TAG, "on back pressed");
        musicService = null;
        notificationReceiver = null;
        controller.hide();

        super.onBackPressed();
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
        if (controller == null)
            controller = new MediaController(this);

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
        File[] files = auditorDir.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String songTitle = file.getName();
            Song song = new Song(i, songTitle);
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

    private void playNext(){
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

//    get childAt of ListView
//    public View getViewByPosition(int pos, ListView listView) {
//        final int firstListItemPosition = listView.getFirstVisiblePosition();
//        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
//
//        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
//            return listView.getAdapter().getView(pos, null, listView);
//        } else {
//            final int childIndex = pos - firstListItemPosition;
//            return listView.getChildAt(childIndex);
//        }
//    }
}

// TODO list view 小箭頭點開要有 轉檔 命名 刪除
// TODO UI: change the alpha of LinearLayout of songView when the song is playing, or shows what song is currently playing

// TODO http://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764,
// http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778,
// http://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787