package com.example.auditor;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by wanlin on 15/9/23.
 */
public class WordAdapter extends ArrayAdapter<String>{
    private ArrayList<String> words = new ArrayList<>();

    public WordAdapter(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
        words = objects;
    }

    @Override
    public int getCount() {
        return words.size();
    }

    @Override
    public String getItem(int arg0) {
        return words.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }
}
