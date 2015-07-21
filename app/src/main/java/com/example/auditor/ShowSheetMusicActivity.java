package com.example.auditor;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.jfugue.MusicStringParser;
import org.jfugue.MusicXmlRenderer;
import org.jfugue.Pattern;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import nu.xom.Serializer;

public class ShowSheetMusicActivity extends ActionBarActivity {
    private static final String LOG_TAG = "AudioRecordActivity";
    private File auditorDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Auditor");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sheet_music);

        FileOutputStream file;

        try {
//            file = this.openFileOutput("music.xml", MODE_PRIVATE);
            file = new FileOutputStream(new File(auditorDir + "/music.xml"));
            MusicXmlRenderer renderer = new MusicXmlRenderer();
            MusicStringParser parser = new MusicStringParser();
            parser.addParserListener(renderer);

            Pattern pattern = new Pattern("C D E F G A B |");
            parser.parse(pattern);

            Serializer serializer = new Serializer(file, "UTF-8");
            serializer.setIndent(4);
            serializer.write(renderer.getMusicXMLDoc());

            file.flush();
            file.close();
        }
        catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "file not found");
        }
        catch (UnsupportedEncodingException e) {
            Log.e(LOG_TAG, "unsupported encoding exception");
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "io exception");
        }
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
