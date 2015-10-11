package com.example.auditor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Wan Lin on 2015/10/8.
 * SlidingTabAdapter
 */
public class SlidingTabAdapter extends FragmentStatePagerAdapter {
    public static final int AUDIO_RECORD = 0;
    public static final int AUDIO_FILE_LIST = 1;
    public static final int SCORE_FILE_LIST = 2;
    SlidingTabActivity slidingTabActivity;

    public SlidingTabAdapter(FragmentManager fm, SlidingTabActivity slidingTabActivity) {
        super(fm);
        this.slidingTabActivity = slidingTabActivity;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case AUDIO_RECORD: return new AudioRecordPage();
            case AUDIO_FILE_LIST : return new AudioFileListPage(slidingTabActivity);
            case SCORE_FILE_LIST : return new ScoreFileListPage(slidingTabActivity);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case AUDIO_RECORD: return "AudioRecord";
            case AUDIO_FILE_LIST : return "AudioFileList";
            case SCORE_FILE_LIST : return "ScoreFileList";
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        return 3;
    }
}
