package com.example.auditor.song;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.auditor.AudioFileListActivity;
import com.example.auditor.R;
import com.example.auditor.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Wan Lin on 15/7/2.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = "MusicService";
    private File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");
    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int songPosition; // current song position
    private final IBinder musicBind = new MusicBinder();
    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;
    private Intent notification;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public void onCreate(){
        super.onCreate();

        notification = new Intent("Player");
        songPosition = 0;
        player = new MediaPlayer();
        rand = new Random();

        initMusicPlayer();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setVolume(1, 1);
    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        if (player.isPlaying()) {
            player.stop();
            player.release();
        }

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();

        Intent notIntent = new Intent(this, AudioFileListActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.getNotification();

        // send notification to AudioFileActivity
        notification.putExtra("action", "prepared");
        LocalBroadcastManager.getInstance(this).sendBroadcast(notification);

        Log.e(LOG_TAG, "song time: " + player.getDuration());

        startForeground(NOTIFY_ID, not);
    }

    @Override
    public void onDestroy() {
        Log.e(LOG_TAG, "Music service on destroy!");
        player = null;

        stopForeground(true);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mp.getCurrentPosition() > 0){
            mp.stop();
            mp.reset();
        }
    }

    public void playSong(){
        player.reset();
        Song playSong = songs.get(songPosition);

        songTitle = playSong.getTitle();

        //set uri
        String songPath = auditorDir + "/" + playSong.getTitle() + ".wav";

        try{
            player.setDataSource(songPath);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public void setSong(int songIndex){
        songPosition = songIndex;
    }

    public int getPosition(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPlaying(){
        boolean isPlaying = false;

        try {
            isPlaying = player.isPlaying();
        }
        catch (IllegalStateException e) {
            player = null;
            player = new MediaPlayer();
        }

        return isPlaying;
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int position){
        player.seekTo(position);
    }

    public void go(){
        player.start();
    }

    public void setShuffle() {
        if(shuffle) shuffle = false;
        else shuffle = true;
    }

    // skipping to the next and previous tracks
    public void playPrev(){
        songPosition--;
        if(songPosition < 0) songPosition = songs.size() - 1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = songPosition;
            while(newSong == songPosition){
                newSong = rand.nextInt(songs.size());
            }
            songPosition = newSong;
        }
        else{
            songPosition++;
            if(songPosition >= songs.size()) songPosition = 0;
        }
        playSong();
    }

    public boolean isShuffle(){
        return shuffle;
    }
}
