package com.example.auditor;

import android.app.Activity;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.auditor.button.PlayButton;
import com.example.auditor.convert.SongConverter;
import com.example.auditor.song.MusicService;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by wanlin on 2015/10/8.
 */
public class AudioFileListPage extends Fragment implements MediaController.MediaPlayerControl {
    private static final String LOG_TAG = AudioFileListPage.class.getName();
    private static final String wavDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/wav/";
    private static MusicService musicService;
    private int screenHeight;

    private SongAdapter songAdt;
    private SlidingTabActivity slidingTabActivity;
    private EditText userInput;
    private ListView songListView;
    private ArrayList<Song> songList = new ArrayList<>();;

    private boolean playbackPaused = false;
    private boolean musicBound = false;

    private Intent playIntent;
    private ServiceConnection musicConnection;
    private BroadcastReceiver notificationReceiver;
    private RelativeLayout rootView;
    private RelativeLayout controllerContainer;
    private PlayButton playButton;


    public AudioFileListPage() {
        super();
    }

    public AudioFileListPage(SlidingTabActivity slidingTabActivity) {
        super();
        this.slidingTabActivity = slidingTabActivity;
        getSongList();
        playButton = new PlayButton(slidingTabActivity);

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
                Log.i(LOG_TAG, "onReceive: " + i.getStringExtra("action"));
                switch (i.getStringExtra("action")) {
                    case "prepared":
                        break;
                    case "play":
                        playButton.setPlay(false);
                        playButton.invalidate();
                        break;
                    case "complete":
                        playButton.setPlay(true);
                        playButton.invalidate();
                        break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter("Player");
        LocalBroadcastManager.getInstance(slidingTabActivity).registerReceiver(notificationReceiver, intentFilter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        songAdt = new SongAdapter(slidingTabActivity, songList, this);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        slidingTabActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenHeight = displaymetrics.heightPixels;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (RelativeLayout)inflater.inflate(R.layout.audio_file_list_page, container, false);
        controllerContainer = (RelativeLayout)rootView.findViewById(R.id.controller_container);

        int padding = (int)getResources().getDimension(R.dimen.controller_container_padding);
        controllerContainer.setPadding(padding, padding, padding, padding);

        songListView = (ListView)rootView.findViewById(R.id.audio_file_list_view);
        songListView.setAdapter(songAdt);
        songListView.setTextFilterEnabled(true);
        songListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                songPicked(view);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicService.isPlaying()) {
                    musicService.pausePlayer();
                    playButton.setPlay(true);
                    playButton.invalidate();
                    playbackPaused = true;
                } else {
                    musicService.go();
                    playButton.setPlay(false);
                    playButton.invalidate();
                    playbackPaused = false;
                }
            }
        });
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        playButton.setLayoutParams(lp);

        controllerContainer.addView(playButton);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        slidingTabActivity = (SlidingTabActivity)activity;
    }

    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicService != null && musicBound && musicService.isPlaying())
            return musicService.getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService != null && musicBound && musicService.isPlaying())
            return musicService.getPosition();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService != null && musicBound)
            return musicService.isPlaying();

        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
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
    public int getAudioSessionId() {
        return 0;
    }

    public void renameSong(final Song song) {
        // get audio_record_popup_rename.xml view
        LayoutInflater li = LayoutInflater.from(slidingTabActivity);
        View promptsView = li.inflate(R.layout.audio_record_popup_rename, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(slidingTabActivity);
        alertDialogBuilder.setView(promptsView);
        userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File from = new File(wavDir + song.getTitle() + ".wav");
                                File to = new File(
                                        wavDir + userInput.getText() + ".wav");
                                from.renameTo(to);

                                // update song list view and reset songList of musicService
                                songList.clear();
                                getSongList();
                                musicService.setList(songList);
                                songAdt.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(R.string.cancel,
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
        LayoutInflater li = LayoutInflater.from(slidingTabActivity);
        View promptsView = li.inflate(R.layout.audio_file_popup_delete, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(slidingTabActivity);

        // set audio_record_popup_delete.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView dialogTitle = (TextView) promptsView
                .findViewById(R.id.popupWindowTitle);
        dialogTitle.setText(dialogTitle.getText() + song.getTitle() + "?");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File fileToDelete = new File(wavDir + song.getTitle() + ".wav");
                                if (fileToDelete.delete())
                                    Toast.makeText(
                                            slidingTabActivity,
                                            "Delete successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                else
                                    Toast.makeText(
                                            slidingTabActivity,
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
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public boolean convertSong(final Song song) {
        SongConverter songConverter = new SongConverter(slidingTabActivity);

        if(!songConverter.setUp(song.getTitle()))
            return false;

        songConverter.convert();
        return true;
    }

    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();

        if (playbackPaused) {
            playbackPaused = false;
        }
    }

    public void playNext() {
        musicService.playNext();
        if(playbackPaused){
            playbackPaused = false;
        }
    }

    public void playPrev() {
        musicService.playPrev();
        if (playbackPaused) {
            playbackPaused = false;
        }
    }

    public void getSongList() {
        File wavDirFiles = new File(wavDir);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".wav");
            }
        };
        File[] files = wavDirFiles.listFiles(filter);

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

    public void bindService() {
        if(playIntent == null){
            playIntent = new Intent(slidingTabActivity, MusicService.class);
            slidingTabActivity.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            slidingTabActivity.startService(playIntent);
        }
    }

    public void destroyMusicService() {
        slidingTabActivity.stopService(playIntent);
        slidingTabActivity.unbindService(musicConnection);
        LocalBroadcastManager.getInstance(slidingTabActivity).unregisterReceiver(notificationReceiver);
        notificationReceiver = null;
        musicService = null;
    }
}