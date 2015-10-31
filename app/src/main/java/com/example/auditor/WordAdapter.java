package com.example.auditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Wan Lin on 15/9/23.
 * WordAdapter
 */
public class WordAdapter extends ArrayAdapter<Pair<String, Integer>>{
    private static final String LOG_TAG = "WordAdapter";
    private ArrayList<Pair<String, Integer>> words = new ArrayList<>();

    public WordAdapter(Context context, int resource, ArrayList<Pair<String, Integer>> words) {
        super(context, resource, words);
        this.words = words;
    }

    @Override
    public int getCount() {
        return words.size();
    }

    @Override
    public Pair<String, Integer> getItem(int arg0) {
        return words.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecommendLyricViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.recommend_lyric_item, parent, false);
            holder = new RecommendLyricViewHolder();
            holder.word = (TextView) convertView.findViewById(R.id.title);
            holder.authorCount = (TextView) convertView.findViewById(R.id.author_count);
            holder.info = (ImageButton) convertView.findViewById(R.id.info);
            convertView.setTag(holder);
        }
        else {
            holder = (RecommendLyricViewHolder) convertView.getTag();
        }

        final String word = words.get(position).first;
        final int count = words.get(position).second;

        holder.word.setText(word);
        holder.authorCount.setText(count + " 位歌手使用過");
        holder.info.setColorFilter(getContext().getResources().getColor(R.color.AuditorColorPrimary));
        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetAuthorAndLyricListTask().execute(word);
            }
        });

        return convertView;
    }

    class GetAuthorAndLyricListTask extends AsyncTask<String, Void, ArrayList<Pair<String, String>>> {
        private String word;

        @Override
        protected ArrayList<Pair<String, String>> doInBackground(String... words) {
            word = words[0];
            ArrayList<Pair<String, String>> authorAndLyricList = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            String url = "http://140.117.71.221/auditor/stest/authorics.php";
            String webRequestResult = ""; // web request result

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("text", word));
            InputStream is = null;

            //http post
            try{
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
            }
            catch(Exception e){
                Log.e(LOG_TAG, "Error in http connection " + e.toString());
            }

            //convert response to string
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();

                webRequestResult = sb.toString();
                Log.e(LOG_TAG, "web result: " + webRequestResult);
            }
            catch(Exception e){
                Log.e(LOG_TAG, "Error converting result " + e.toString());
            }

            //parse json data
            try{
                JSONArray jArray = new JSONArray(webRequestResult);

                for(int i = 0; i < jArray.length(); i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    authorAndLyricList.add(new Pair<>(json_data.getString("author"), json_data.getString("lyrics")));
                    Log.i(LOG_TAG, "author: " + json_data.getString("author") + ", lyric: " + json_data.getString("lyrics"));
                }
            }
            catch(JSONException e){
                Log.e(LOG_TAG, "Error parsing data " + e.toString());
            }

            long finishTime = System.currentTimeMillis();
            double duration = (finishTime - startTime) / 1000d;
            Log.e(LOG_TAG, "total fetch time: " + duration + " seconds.");

            return authorAndLyricList;
        }

        @Override
        protected void onPostExecute(ArrayList<Pair<String, String>> authorAndLyricList) {
            super.onPostExecute(authorAndLyricList);

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
            builderSingle.setTitle("歌詞使用狀況");

            final AuthorAndLyricAdapter adapter =
                    new AuthorAndLyricAdapter(
                            getContext(),
                            android.R.layout.simple_list_item_1,
                            authorAndLyricList, word);

            builderSingle.setNegativeButton(
                    getContext().getResources().getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String newLyric = ShowScoreActivity.lyricInputACTextView.getText() + word;
                            ShowScoreActivity.lyricInputACTextView.setText(newLyric);
                            ShowScoreActivity.lyricInputACTextView.setSelection(newLyric.length());
                            dialog.dismiss();
                        }
                    });

            builderSingle.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newLyric = ShowScoreActivity.lyricInputACTextView.getText() + word;
                    ShowScoreActivity.lyricInputACTextView.setText(newLyric);
                    ShowScoreActivity.lyricInputACTextView.setSelection(newLyric.length());
                }
            });

            builderSingle.show();
        }
    }

    class AuthorAndLyricAdapter extends ArrayAdapter<Pair<String, String>> {
        private ArrayList<Pair<String, String>> authorAndLyricList = new ArrayList<>();
        private String word;

        public AuthorAndLyricAdapter(Context context, int resource, ArrayList<Pair<String, String>> authorAndLyricList, String word) {
            super(context, resource, authorAndLyricList);
            this.authorAndLyricList = authorAndLyricList;
            this.word = word;
        }

        @Override
        public int getCount() {
            return authorAndLyricList.size();
        }

        @Override
        public Pair<String, String> getItem(int position) {
            return super.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AuthorAndLyricViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.author_and_lyric_item, parent, false);
                holder = new AuthorAndLyricViewHolder();
                holder.author = (TextView) convertView.findViewById(R.id.author);
                holder.lyric = (TextView) convertView.findViewById(R.id.lyric);
                convertView.setTag(holder);
            }
            else {
                holder = (AuthorAndLyricViewHolder) convertView.getTag();
            }

            final String author = authorAndLyricList.get(position).first;
            final String lyric = authorAndLyricList.get(position).second;
            String prevLyric; // prev lyric, split by word
            String nextLyric; // next lyric, split by word
            int wordPos = 0;

            // find all occurrences forward
            for (int i = -1; (i = lyric.indexOf(word, i + 1)) != -1; ) {
                wordPos = i;
            }

            prevLyric = lyric.substring(0, wordPos);
            nextLyric = lyric.substring(wordPos + word.length(), lyric.length());
            String colorCode = getContext().getResources().getString(R.color.lyric_highlight).substring(3);

            String coloredLyric =
                    prevLyric +
                    "<font color='#" + colorCode + "'>" + word + "</font>" +
                    nextLyric;

            holder.author.setText(author);
            holder.lyric.setText(Html.fromHtml(coloredLyric));

            return convertView;
        }
    }

    static class AuthorAndLyricViewHolder {
        private TextView author;
        private TextView lyric;
    }

    static class RecommendLyricViewHolder {
        private TextView word;
        private TextView authorCount;
        private ImageButton info;
    }
}
