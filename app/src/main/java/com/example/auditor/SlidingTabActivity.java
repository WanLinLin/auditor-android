package com.example.auditor;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

public class SlidingTabActivity extends ActionBarActivity {
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sliding_tab);

        viewPager = (ViewPager) findViewById(R.id.my_pager);
        viewPager.setAdapter(new SlidingTabAdapter(getSupportFragmentManager()));

        slidingTabLayout = (SlidingTabLayout)findViewById(R.id.sliding_tabs);
        slidingTabLayout.setViewPager(viewPager);
    }
}
