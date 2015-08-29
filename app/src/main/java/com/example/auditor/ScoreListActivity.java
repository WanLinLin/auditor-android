package com.example.auditor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.auditor.score.Score;
import com.example.auditor.score.ScoreAdapter;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ScoreListActivity extends ActionBarActivity {
    private String auditorDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/";
    private ScoreAdapter scoreAdapter;
    private ArrayList<Score> scoreList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_list);

        scoreList = new ArrayList<>();

        // prepare the score list
        getScoreList();

        final ListView scoreListView = (ListView) findViewById(R.id.score_list);
        scoreAdapter = new ScoreAdapter(this, scoreList);
        scoreListView.setAdapter(scoreAdapter);
        scoreListView.setTextFilterEnabled(true);
        scoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ShowScoreActivity.class);
                intent.putExtra("score name", scoreList.get(position).getTitle());
                startActivity(intent);
            }
        });
    }

    private void getScoreList() {
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".txt");
            }
        };
        File auditor = new File(auditorDir);
        File[] files = auditor.listFiles(filter);

        for (int i = 0; i < files.length; i++) {
            DateFormat sdf = DateFormat.getDateTimeInstance();

            File file = files[i];
            Date lastModDate = new Date(file.lastModified());
            String lmd = sdf.format(lastModDate);
            String songTitle = file.getName().substring(0, file.getName().length() - 4);
            Score score = new Score(i, songTitle, lmd);
            scoreList.add(score);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_sheet_music, menu);
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}