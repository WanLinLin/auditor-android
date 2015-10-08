package com.example.auditor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Wan Lin on 2015/10/8.
 * PageFragment
 */
public class PageFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.page_root_view, container, false);
            Bundle args = getArguments();
            ((TextView) rootView.findViewById(R.id.page_text)).setText(ARG_OBJECT);
            return rootView;
    }
}