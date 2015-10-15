package com.example.auditor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by Wan Lin on 2015/10/8.
 * SlidingTabAdapter
 */
public class SlidingTabAdapter extends FragmentStatePagerAdapter {
    private static final String LOG_TAG = "SlidingTabAdapter";
    public static final int AUDIO_RECORD = 0;
    public static final int AUDIO_FILE_LIST = 1;
    public static final int SCORE_FILE_LIST = 2;

    private SlidingTabActivity slidingTabActivity;
    private ArrayList<Fragment> pages;


    public SlidingTabAdapter(FragmentManager fm, SlidingTabActivity slidingTabActivity) {
        super(fm);
        this.slidingTabActivity = slidingTabActivity;
        pages = new ArrayList<>();
        pages.add(new AudioRecordPage());
        pages.add(new AudioFileListPage(slidingTabActivity));
        pages.add(new ScoreFileListPage(slidingTabActivity));
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case AUDIO_RECORD:
                return pages.get(AUDIO_RECORD);

            case AUDIO_FILE_LIST:
                return pages.get(AUDIO_FILE_LIST);

            case SCORE_FILE_LIST:
                return pages.get(SCORE_FILE_LIST);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case AUDIO_RECORD:
                return slidingTabActivity.getString(R.string.audio_record_page_title);
            case AUDIO_FILE_LIST:
                return slidingTabActivity.getString(R.string.audio_file_list_page_title);
            case SCORE_FILE_LIST:
                return slidingTabActivity.getString(R.string.score_file_list_page_title);
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    public Fragment getPage(int position) {
        return pages.get(position);
    }
}