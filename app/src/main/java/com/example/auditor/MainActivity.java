package com.example.auditor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.File;


public class MainActivity extends Activity {
    private static final String LOG_TAG = "MainActivity";
    private static final String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    public static String androidId; // android phone id

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView auditorIcon = (ImageView)findViewById(R.id.auditor_icon);
        Animation iconAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom);
        iconAnimation.setDuration(1000);
        iconAnimation.setFillAfter(true);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                auditorIcon.clearAnimation();

                Intent i = new Intent(MainActivity.this, SlidingTabActivity.class);
                MainActivity.this.startActivity(i);
            }
        }, iconAnimation.getDuration());
        auditorIcon.startAnimation(iconAnimation);

        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.e(LOG_TAG, "android id: " + androidId);

        File auditor = new File(auditorDir);
        auditor.mkdir();

        File score = new File(auditorDir + "score/");
        score.mkdir();

        File wav = new File(auditorDir + "wav/");
        wav.mkdir();

        File midi = new File(auditorDir + "midi/");
        midi.mkdir();

        File txt = new File(auditorDir + "txt/");
        txt.mkdir();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageView auditorIcon = (ImageView)findViewById(R.id.auditor_icon);
        Animation iconAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom);
        auditorIcon.startAnimation(iconAnimation);
    }

    public void goToSlidingTabActivity(View view){
        Intent intent = new Intent(this, SlidingTabActivity.class);
        startActivity(intent);
    }
}
