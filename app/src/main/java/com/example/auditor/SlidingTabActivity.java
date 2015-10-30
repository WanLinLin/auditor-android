package com.example.auditor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

public class SlidingTabActivity extends ActionBarActivity{
    private static final String LOG_TAG = "SlidingTabActivity";
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private SlidingTabAdapter adapter;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_tab);

        int initialPosition = getIntent().getIntExtra("initialPosition", SlidingTabAdapter.AUDIO_RECORD);

        // sliding tab view set up
        viewPager = (ViewPager)findViewById(R.id.pager);
        viewPager.setAdapter(new SlidingTabAdapter(getSupportFragmentManager(), this));
        slidingTabLayout = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);
        slidingTabLayout.setDistributeEvenly(true);
        adapter = (SlidingTabAdapter)viewPager.getAdapter();

        initToolbar();
        ActionBar actionbar = getSupportActionBar();
        actionbar.setTitle("Auditor");

        viewPager.setCurrentItem(initialPosition, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_sliding_tab, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AudioFileListPage page = (AudioFileListPage)adapter.getPage(SlidingTabAdapter.AUDIO_FILE_LIST);
        page.bindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AudioFileListPage page = (AudioFileListPage)adapter.getPage(SlidingTabAdapter.AUDIO_FILE_LIST);
        page.destroyMusicService();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onBackPressed() {
        // leave app
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public SlidingTabAdapter getAdapter() {
        return adapter;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }
}