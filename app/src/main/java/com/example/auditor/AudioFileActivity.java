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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

import com.example.auditor.MusicService.MusicBinder;

import java.io.File;
import java.util.ArrayList;


public class AudioFileActivity extends ActionBarActivity implements MediaPlayerControl{
    private static final String LOG_TAG = "AudioFileActivity";
    private File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");
    private MusicController controller;
    private ArrayList<Song> songList = new ArrayList<>();
    private MusicService musicService;
    private ListView songView;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false;
    private boolean playbackPaused = false;
    private BroadcastReceiver onPrepareReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_file);

        onPrepareReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                // When music player has been prepared, show controller
                controller.show(0);
            }
        };

        getSongList();

        songView = (ListView) findViewById(R.id.song_list);

        SongAdapter songAdt = new SongAdapter(this, songList);

        songView.setAdapter(songAdt);
        songView.setTextFilterEnabled(true);
        setController();

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

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicService = binder.getService();
            //pass list
            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);

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
    protected void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume(){
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(onPrepareReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));
        if(paused){
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
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
        if (controller == null) controller = new MusicController(this);
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
        if (isPlaying()) {
            controller.hide();
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
        if(playbackPaused){
            playbackPaused = false;
        }
    }
}

// TODO listview 小箭頭點開要有 轉檔 命名 刪除

// TODO http://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764
// TODO http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
// TODO http://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787