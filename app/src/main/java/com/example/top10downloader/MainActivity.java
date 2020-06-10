package com.example.top10downloader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ListView listApps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listApps = findViewById(R.id.xmlListView);

       downloadUrl("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        String feedUrl;

        switch(id) {
            case R.id.mnuFree:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml";
                break;
            case R.id.mnuPaid:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=10/xml";
                break;
            case R.id.mnuSongs:
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=10/xml";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(feedUrl);
        return true;
    }

    private void downloadUrl(String feedUrl){
        Log.d(TAG, "downloadUrl: AsyncTask is executing");
        DownloadData downloadData = new DownloadData();
        downloadData.execute(feedUrl); //This URL goes into doInBackground method of the AsyncTask.
        Log.d(TAG, "downloadUrl: AsyncTask is done");

    }

    // Methods of this AsyncTask are executed on seperate Thread.
    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

//            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplications.getApplications());
//            listApps.setAdapter(arrayAdapter);

            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String RSSfeed = DownloadXML(strings[0]);
            if (RSSfeed == null) {
                Log.e(TAG, "doInBackground: Error Downloading");
            }
            return RSSfeed;
        }

        private String DownloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();
            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // open an HTTP connection of the url
                int response = connection.getResponseCode(); // to get the response code of the HTTP connection
                Log.d(TAG, "DownloadXML: The response code is: " + response);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); // this line can be split into foloowing three lines
//                InputStream inputStream = connection.getInputStream();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader reader = new BufferedReader(inputStreamReader);

                int charsRead;
                char[] inputBuffer = new char[500]; //increase the inputBuffer array of characters size if you are expecting larger file download as it'll increase the performance.
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break; //if value in character buffer is less than 0 it'll break out of the loop.
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();
                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "DownloadXML: Invalid URL" + e.getMessage()); // MalformedURL exception for url path.
            } catch (IOException e) {
                Log.e(TAG, "DownloadXML: IO exception reading data" + e.getMessage()); // IO exception for http connection.
            } catch (SecurityException e) {
                Log.e(TAG, "DownloadXML: Security Exception. needs permission?" + e.getMessage());
//                e.printStackTrace();
            }
            return null; //if there is any error detected in catch block then we need to return null.
        }
    }
}
