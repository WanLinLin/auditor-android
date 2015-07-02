package com.example.auditor;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.auditor.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;


public class AudioFileActivity extends ActionBarActivity {
    private static final String LOG_TAG = "AudioFileActivity";
    private File AuditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");

    private List<String> FileNamelist = Arrays.asList(AuditorDir.list(
            new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".3gp");
                }
            }
    ));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_file);

        ListView list = (ListView) findViewById(R.id.listView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2,android.R.id.text1, FileNamelist);
        list.setAdapter(adapter);

        list.setTextFilterEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_audio_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

// TODO play different songs