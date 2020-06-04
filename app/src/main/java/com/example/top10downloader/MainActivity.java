package com.example.top10downloader;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: AsyncTask is executing");
        DownloadData downloadData = new DownloadData();
        downloadData.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml"); //This URL goes into doInBackground method of the AsyncTask.
        Log.d(TAG, "onCreate: AsyncTask is done");
    }


    // Methods of this AsyncTask are executed on seperate Thread.
    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);
            Log.d(TAG, "onPostExecute: parameter is " + s);
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
