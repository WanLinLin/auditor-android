package com.example.auditor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Wan Lin on 2015/10/8.
 * SlidingTabAdapter
 */
public class SlidingTabAdapter extends FragmentStatePagerAdapter {
    private static final int FRAGMENT_1 = 0;
    private static final int FRAGMENT_2 = 1;
    private static final int FRAGMENT_3 = 2;

    public SlidingTabAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case FRAGMENT_1 : return new PageFragment();
            case FRAGMENT_2 : return new PageFragment();
            case FRAGMENT_3 : return new PageFragment();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case FRAGMENT_1 : return "RECORD";
            case FRAGMENT_2 : return "AUDIO FILE";
            case FRAGMENT_3 : return "Fragment 3 Title";
        }
        return super.getPageTitle(position);
    }

    @Override
    public int getCount() {
        return 3;
    }
}
