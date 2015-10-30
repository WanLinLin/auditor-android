package com.example.auditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by wanlin on 2015/10/8.
 */
public class ScoreFileListPage extends Fragment{
    private static String LOG_TAG = "ScoreFileListPage";
    private static final String txtDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/txt/";
    private static final String midiDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/midi/";
    private File txtDirFiles = new File(txtDir);
    private ScoreAdapter scoreAdapter;
    private ArrayList<Score> scoreList;
    private EditText userInput;
    private ListView scoreListView;
    private SlidingTabActivity slidingTabActivity;

    public ScoreFileListPage() {
        super();
    }

    public ScoreFileListPage(SlidingTabActivity slidingTabActivity) {
        this.slidingTabActivity = slidingTabActivity;
        scoreList = new ArrayList<>();
        getScoreList();
        scoreAdapter = new ScoreAdapter(slidingTabActivity, scoreList, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout)inflater.inflate(R.layout.score_file_list_page, container, false);

        if(scoreList == null) {
            scoreList = new ArrayList<>();
            getScoreList();
            scoreAdapter = new ScoreAdapter(slidingTabActivity, scoreList, this);
        }

        scoreListView = (ListView) rootView.findViewById(R.id.score_file_list_view);
        scoreListView.setAdapter(scoreAdapter);
        scoreListView.setTextFilterEnabled(true);
        scoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(slidingTabActivity, ShowScoreActivity.class);
                intent.putExtra("score name", scoreList.get(position).getTitle());
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(scoreList == null) {
            scoreList = new ArrayList<>();
            getScoreList();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        slidingTabActivity = (SlidingTabActivity)activity;
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
            String songTitle = file.getName();
            Score score = new Score(i, songTitle, lastModDate);
            scoreList.add(score);
        }

        //Sorting
        Collections.sort(scoreList, new Comparator<Score>() {
            @Override
            public int compare(Score score1, Score score2) {
                return score2.getLastModDate().compareTo(score1.getLastModDate());
            }
        });
    }

    public void renameScore(final Score score) {
        // get audio_record_popup_rename.xml view
        LayoutInflater li = LayoutInflater.from(slidingTabActivity);
        View promptsView = li.inflate(R.layout.audio_record_popup_rename, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(slidingTabActivity);
        alertDialogBuilder.setView(promptsView);
        userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        String fileName = score.getTitle().substring(0, score.getTitle().length() - 4);
        userInput.setText(fileName);
        userInput.setSelection(fileName.length());

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File from = new File(txtDir + score.getTitle());
                                File to = new File(
                                        txtDir + userInput.getText() + ".txt");
                                if (!from.renameTo(to)) {
                                    Toast.makeText(slidingTabActivity,
                                            getString(R.string.failed),
                                            Toast.LENGTH_SHORT).show();
                                }
                                refreshList();

                                File midiFrom =
                                        new File(midiDir + score.getTitle().substring(0, score.getTitle().length() - 4) + ".mid");
                                File midiTo = new File(
                                        midiDir + userInput.getText() + ".mid");
                                if (!midiFrom.renameTo(midiTo)) {
                                    Toast.makeText(slidingTabActivity,
                                            getString(R.string.failed),
                                            Toast.LENGTH_SHORT).show();
                                }
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
        LayoutInflater li = LayoutInflater.from(slidingTabActivity);
        View promptsView = li.inflate(R.layout.audio_file_popup_delete, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(slidingTabActivity);

        // set audio_record_popup_delete.xml to alert dialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView dialogTitle = (TextView) promptsView
                .findViewById(R.id.popupWindowTitle);
        String fileName = score.getTitle().substring(0, score.getTitle().length() - 4);
        String title = getString(R.string.delete_confirm) + " " + fileName + " ?";
        dialogTitle.setText(title);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File scoreFile = new File(txtDir + score.getTitle());
                                File midiFile = new File(midiDir + score.getTitle().substring(0, score.getTitle().length()-4) + ".mid");
                                if (!scoreFile.delete() || !midiFile.delete()) {
                                    Toast.makeText(
                                            slidingTabActivity,
                                            getString(R.string.delete_failed),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                                refreshList();
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

    public void refreshList() {
        scoreList.clear();
        getScoreList();
        scoreAdapter.notifyDataSetChanged();
    }
}