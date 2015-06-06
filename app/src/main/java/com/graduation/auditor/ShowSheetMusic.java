package com.example.project.gp;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.jfugue.Player;
import org.jfugue.Pattern;

import jp.kshoji.javax.sound.midi.MetaEventListener;
import jp.kshoji.javax.sound.midi.MetaMessage;
import jp.kshoji.javax.sound.midi.UsbMidiSystem;

public class ShowSheetMusic extends ActionBarActivity {
    private static final String LOG_TAG = "AudioRecordActivity";
    UsbMidiSystem usbMidiSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sheet_music);

        usbMidiSystem = new UsbMidiSystem(this);
        usbMidiSystem.initialize();

        Player player = new Player();
        Pattern pattern = new Pattern("C D E F G A B");
        player.play(pattern);   // Fucking no sound!

        player.getSequencer().addMetaEventListener(new MetaEventListener() {
            @Override
            public void meta(MetaMessage metaMessage) {
                if (MetaMessage.TYPE_END_OF_TRACK == metaMessage.getType()) {
                    Log.e(LOG_TAG, "player finished");
                }
            }
        });


/*        File musicXml = new File("main/musicXml/BeetAnGeSample.xml");

        MusicXmlParser parser = new MusicXmlParser();
        MusicXmlRenderer renderer = new MusicXmlRenderer();
        parser.addParserListener(renderer);
        parser.parse(musicXml);*/

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

        if (usbMidiSystem != null) {
            usbMidiSystem.terminate();
        }
    }
}
