package com.example.auditor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;

public class ScoreFileListActivity extends ActionBarActivity {
    private static final String txtDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/txt/";
    private File txtDirFiles = new File(txtDir);
    private ScoreAdapter scoreAdapter;
    private ArrayList<Score> scoreList;
    private EditText userInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_file_list);

        scoreList = new ArrayList<>();

        // prepare the score list
        getScoreList();

        final ListView scoreListView = (ListView) findViewById(R.id.score_list);
//        scoreAdapter = new ScoreAdapter(this, scoreList);
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
        File[] files = txtDirFiles.listFiles(filter);

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            Date lastModDate = new Date(file.lastModified());
            String songTitle = file.getName().substring(0, file.getName().length() - 4);
            Score score = new Score(i, songTitle, lastModDate);
            scoreList.add(score);
        }
    }

    public void renameScore(final Score score) {
        // get audio_record_popup_rename.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.audio_record_popup_rename, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);
        userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File from = new File(txtDir + score.getTitle() + ".txt");
                                File to = new File(
                                        txtDir + userInput.getText() + ".txt");
                                from.renameTo(to);
                                scoreList.clear();
                                getScoreList();
                                scoreAdapter.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void deleteScore(final Score score) {
        // get audio_record_popup_delete.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.audio_file_popup_delete, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set audio_record_popup_delete.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView dialogTitle = (TextView) promptsView
                .findViewById(R.id.popupWindowTitle);
        dialogTitle.setText(dialogTitle.getText() + score.getTitle() + "?");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File fileToDelete = new File(txtDir + score.getTitle() + ".txt");
                                if (fileToDelete.delete())
                                    Toast.makeText(
                                            ScoreFileListActivity.this,
                                            "Delete successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                else
                                    Toast.makeText(
                                            ScoreFileListActivity.this,
                                            "Delete failed!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                scoreList.clear();
                                getScoreList();
                                scoreAdapter.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}