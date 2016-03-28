package dk.itu.ubicomp.android.contextservice;

import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import dk.itu.ubicomp.android.contextservice.Data.DummyContent.DummyItem;
import dk.itu.ubicomp.android.contextservice.Data.SensorData;

public class CRUDSensorActivity extends AppCompatActivity {

    private DummyItem mItem;
    private TextView mTextViewValue;
    private Button buttonDelete;
    private Button buttonSave;
    private String android_id = null;

    public SensorData sensorData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_crud);

        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.nav_title_sensor_list_edit);
        }

        // To retrieve object in second Activity
        mItem = (DummyItem)getIntent().getSerializableExtra("ITEM");
        mTextViewValue = (TextView) findViewById(R.id.editTextValue);
        mTextViewValue.setText(mItem.id);

        sensorData = new SensorData("1", "1","1","1","1","1");

        SetupButtons();
    }

    private void SetupButtons() {
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateData();

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .authority("contextphone-1253.appspot.com")
                        .path("")
                        .appendQueryParameter("entype", sensorData.entype)
                        .appendQueryParameter("id", sensorData.id)
                        .appendQueryParameter("sensortype", sensorData.sensortype)
                        .appendQueryParameter("value", sensorData.value)
                        .appendQueryParameter("timestamp", sensorData.timestamp)
                        .appendQueryParameter("androidID", sensorData.androidID)
                        .build();

                Log.d("HTTP REQUEST", uri.toString());
                new SendDeleteRequestAndCloseOnOK().execute(uri.toString());
            }
        });

        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateData();

                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .authority("contextphone-1253.appspot.com")
                        .path("")
                        .appendQueryParameter("entype", sensorData.entype)
                        .appendQueryParameter("id", sensorData.id)
                        .appendQueryParameter("sensortype", sensorData.sensortype)
                        .appendQueryParameter("value", sensorData.value)
                        .appendQueryParameter("timestamp", sensorData.timestamp)
                        .appendQueryParameter("androidID", sensorData.androidID)
                        .build();

                Log.d("HTTP REQUEST", uri.toString());
                new SendUpdateRequest().execute(uri.toString());
            }
        });
    }

    private void UpdateData()
    {
        sensorData.value = mTextViewValue.getText().toString();
    }

    class SendDeleteRequestAndCloseOnOK extends AsyncTask<String, String, String> {

        private Boolean resultOK = false;

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
                    resultOK = true;
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
            if (resultOK)
            {
                Toast.makeText(getBaseContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
                CRUDSensorActivity.this.finish();
            }
            else
            {
                Toast.makeText(getBaseContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class SendUpdateRequest extends AsyncTask<String, String, String> {

        private Boolean resultOK = false;

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
                    resultOK = true;
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
            if (resultOK)
            {
                Toast.makeText(getBaseContext(), "SUCCESS", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getBaseContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    }
}