package com.example.auditor;

import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

public class SlidingTabActivity extends ActionBarActivity{
    private static final String LOG_TAG = "SlidingTabActivity";
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private AudioFileListPage audioFileListPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_tab);

        int initialPosition = getIntent().getIntExtra("initialPosition", SlidingTabAdapter.AUDIO_RECORD);

        /* sliding tab view set up */
        viewPager = (ViewPager) findViewById(R.id.my_pager);
        viewPager.setAdapter(new SlidingTabAdapter(getSupportFragmentManager(), this));
        slidingTabLayout = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);

        viewPager.setCurrentItem(initialPosition);

        SlidingTabAdapter adapter = (SlidingTabAdapter)viewPager.getAdapter();
        audioFileListPage = (AudioFileListPage)adapter.getItem(SlidingTabAdapter.AUDIO_FILE_LIST);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        audioFileListPage.bindService();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioFileListPage.destroyMusicService();
    }
}