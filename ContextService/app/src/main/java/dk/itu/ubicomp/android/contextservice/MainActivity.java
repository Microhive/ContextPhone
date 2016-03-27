package dk.itu.ubicomp.android.contextservice;

import android.location.Criteria;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static BeaconManager beaconManager;
    public static Region region;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Location currentLocation;

    private String android_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.floating_action_button, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = new Intent(this, ContextService.class);
        startService(intent);

        int startView = getIntent().getIntExtra("displayfragmentview", R.id.nav_privacy);
        DisplayFragmentView(startView);
        Log.d("ACTIVITY", "RESUMED!");

        locationListener = new MyLocationListener();
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }

        beaconManager = new BeaconManager(this);
        region = new Region("rid", null, null, null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {

                if (currentLocation != null) {
                    Log.d("LOCATION", currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                    for (Beacon beacon : list) {
                        if (!BeaconDb.getInstance().getmMapOfBeaconData().containsKey(beacon.getProximityUUID() + "," + beacon.getMajor() + "," + beacon.getMinor())) {
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
//                            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            BeaconDb.getInstance().AddItem(beacon.getProximityUUID() + "," + beacon.getMajor() + "," + beacon.getMinor(), beacon, currentLocation);
                        }
                    }
                }

                Log.d("Beacons that exist!", Integer.toString(BeaconDb.getInstance().getmMapOfBeaconData().size(), 0));
            }
        });

        android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
//                    currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                break;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("displayfragmentview")) {
            int startView = getIntent().getIntExtra("displayfragmentview", R.id.nav_privacy);
            DisplayFragmentView(startView);
            Log.d("ACTIVITY", "RESUMED!");
        }

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DisplayFragmentView(item.getItemId());
        return true;
    }

    public void DisplayFragmentView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (viewId) {

            case R.id.nav_my_location:
                fragment = new MyLocation();
                title = getString(R.string.nav_title_my_location);
                break;

            case R.id.nav_privacy:
                fragment = new Privacy();
                title = getString(R.string.nav_title_privacy);
                break;

            case R.id.nav_track_beacons:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.removeUpdates(locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                break;

            case R.id.nav_send_beacons:
                Toast.makeText(getApplicationContext(), "SENDING for " + android_id + "!", Toast.LENGTH_SHORT).show();
                try {
                    SendBeacons();
                } catch (IOException e) {
                    Log.d("ERROR!", e.getMessage());
                    e.printStackTrace();
                }
                break;

            case R.id.nav_send_sensor:
                Toast.makeText(getApplicationContext(), "SENDING for " + android_id + "!", Toast.LENGTH_SHORT).show();
                try {
                    SendSensor();
                } catch (IOException e) {
                    Log.d("ERROR!", e.getMessage());
                    e.printStackTrace();
                }
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location locFromGps) {
            currentLocation = locFromGps;
            String longitude = "Longitude: " + currentLocation.getLongitude();
            Log.v("POSITION X: ", longitude);
            String latitude = "Latitude: " + currentLocation.getLatitude();
            Log.v("POSITION Y: ", latitude);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private void SendBeacons() throws IOException {

        for(Map.Entry<String, BeaconDb.BeaconData> entry : BeaconDb.getInstance().getmMapOfBeaconData().entrySet()) {
            String key = entry.getKey();
            BeaconDb.BeaconData value = entry.getValue();

            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .authority("contextphone-1253.appspot.com")
                    .path("")
                    .appendQueryParameter("entype", "1")
                    .appendQueryParameter("id", "1")
                    .appendQueryParameter("lat", Double.toString(value.mLocation.getLatitude()))
                    .appendQueryParameter("long", Double.toString(value.mLocation.getLongitude()))
                    .appendQueryParameter("beacon", "1")
                    .appendQueryParameter("minor", Integer.toString(value.mBeacon.getMinor()))
                    .appendQueryParameter("major", Integer.toString(value.mBeacon.getMajor()))
                    .appendQueryParameter("androidID", android_id)
                    .appendQueryParameter("uuid", value.mBeacon.getProximityUUID().toString())
                    .build();

            Log.d("HTTP REQUEST", uri.toString());
            new SendRequestByURL().execute(uri.toString());
        }
    }

    private void SendSensor() throws IOException {

        Uri uri = new Uri.Builder()
                .scheme("http")
                .authority("contextphone-1253.appspot.com")
                .path("")
                .appendQueryParameter("entype", "2")
                .appendQueryParameter("id", "1")
                .appendQueryParameter("sensortype", "light")
                .appendQueryParameter("value", "123")
                .appendQueryParameter("timestamp", new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString())
                .appendQueryParameter("androidID", android_id)
                .build();

        Log.d("HTTP REQUEST", uri.toString());
        new SendRequestByURL().execute(uri.toString());
    }

    class SendRequestByURL extends AsyncTask<String, String, String> {

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
            Toast.makeText(getApplicationContext(), "SENSOR DATA SENT FOR " + android_id + "!", Toast.LENGTH_SHORT).show();
        }
    }
}
