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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by wanlin on 2015/10/8.
 */
public class ScoreFileListPage extends Fragment{
    private static String LOG_TAG = ScoreFileListPage.class.getName();
    private static final String txtDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor/txt/";
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scoreList = new ArrayList<>();
        getScoreList();

        scoreListView = new ListView(getActivity());
        scoreAdapter = new ScoreAdapter(slidingTabActivity, scoreList, this);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout rootView = (RelativeLayout)inflater.inflate(R.layout.score_file_list_page, container, false);
        rootView.addView(scoreListView);

        return rootView;
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
            DateFormat sdf = DateFormat.getDateTimeInstance();

            File file = files[i];
            Date lastModDate = new Date(file.lastModified());
            String lmd = sdf.format(lastModDate);
            String songTitle = file.getName().substring(0, file.getName().length() - 4);
            Score score = new Score(i, songTitle, lmd);
            scoreList.add(score);
        }
    }

    public void renameScore(final Score score) {
        // get audio_record_popup_rename.xml view
        LayoutInflater li = LayoutInflater.from(slidingTabActivity);
        View promptsView = li.inflate(R.layout.audio_record_popup_rename, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(slidingTabActivity);
        alertDialogBuilder.setView(promptsView);
        userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        userInput.setText(score.getTitle());

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File from = new File(txtDir + score.getTitle() + ".txt");
                                File to = new File(
                                        txtDir + userInput.getText() + ".txt");
                                if (!from.renameTo(to)) {
                                    Toast.makeText(slidingTabActivity,
                                            getString(R.string.failed),
                                            Toast.LENGTH_SHORT).show();
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

    public void deleteScore(final Score score) {
        // get audio_record_popup_delete.xml view
        LayoutInflater li = LayoutInflater.from(slidingTabActivity);
        View promptsView = li.inflate(R.layout.audio_file_popup_delete, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(slidingTabActivity);

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
                                            slidingTabActivity,
                                            "Delete successfully!",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                else
                                    Toast.makeText(
                                            slidingTabActivity,
                                            "Delete failed!",
                                            Toast.LENGTH_SHORT
                                    ).show();
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