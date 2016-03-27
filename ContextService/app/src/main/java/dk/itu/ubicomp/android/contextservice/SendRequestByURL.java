package dk.itu.ubicomp.android.contextservice;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Eiler on 27/03/2016.
 */
public class SendRequestByURL extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... params) {
        try {
            // http client
            URL url = new URL(params[0]);
            HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
            httpClient.setRequestMethod("POST");
            httpClient.setUseCaches(false);
            httpClient.setDoInput(true);
            httpClient.setDoOutput(true);
            httpClient.setRequestProperty("Connection", "Keep-Alive");

            OutputStream os = httpClient.getOutputStream();
            os.close();
            httpClient.connect();

            if (httpClient.getResponseCode() == HttpURLConnection.HTTP_OK) {

            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation
    }
}