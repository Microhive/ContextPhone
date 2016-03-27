package dk.itu.ubicomp.android.contextservice;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Eiler on 10/03/2016.
 */
public class ContextService extends Service {

    private SensorManager mSensorManager;

    private Sensor mRotation;
    private Sensor mBarometer;
    private String mRotationValue;
    private String mPressureValue;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private Location mLocationValue;

    public static final long NOTIFY_INTERVAL = 60000; // 60 seconds
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    private String android_id = null;

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        setupSensors();
        SetupLocation();
        showNotification();
        startTrackingSensorInfo();

        android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void startTrackingSensorInfo() {
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            mTimer = new Timer();
        }
        mTimer.scheduleAtFixedRate(new RunTaskPeriodically(), 0, NOTIFY_INTERVAL);
    }

    class RunTaskPeriodically extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ContextService.this.PeriodicTask();
                }
            });
        }
    }

    protected void PeriodicTask() {
        // If permission has yet to be granted, try setup again
        if (mLocationValue == null) SetupLocation();

        Log.d("SENSOR VALUES", mLocationValue != null ? mLocationValue.getLatitude() + ", " + mLocationValue.getLongitude() : "NULL");
        Log.d("SENSOR VALUES", mPressureValue != null ? mPressureValue : "NULL");
        Log.d("SENSOR VALUES", mRotationValue != null ? mRotationValue : "NULL");

        if (mLocationValue != null && mPressureValue != null && mRotationValue != null)
        {
            SendSensor("LOCATION", mLocationValue != null ? mLocationValue.getLatitude() + ", " + mLocationValue.getLongitude() : "NOT_PROVIDED");
            SendSensor("PRESSURE", mPressureValue != null ? mPressureValue : "NOT_PROVIDED");
            SendSensor("ROTATION", mRotationValue != null ? mRotationValue : "NOT_PROVIDED");
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void showNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_text))
                        .setOngoing(true);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra("displayfragmentview", R.id.nav_privacy);
        resultIntent.setAction(Intent.ACTION_MAIN);
        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        Notification notification = mBuilder.build();
        NotificationManager notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManger.notify(01, notification);
    }

    private void setupSensors()
    {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSensorManager.registerListener(new RotationListener(), mRotation, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(new PressureListener(), mBarometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void SetupLocation()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    class RotationListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mRotationValue = Float.toString(event.values[0]);
        }
    }

    class PressureListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mPressureValue = Float.toString(event.values[0]);
        }
    }

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            mLocationValue = mLastLocation;
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void SendSensor(String sensortype, String Value) {

        Uri uri = new Uri.Builder()
                .scheme("http")
                .authority("contextphone-1253.appspot.com")
                .path("")
                .appendQueryParameter("entype", "2")
                .appendQueryParameter("id", "1")
                .appendQueryParameter("sensortype", sensortype)
                .appendQueryParameter("value", Value)
                .appendQueryParameter("timestamp", new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()).toString())
                .appendQueryParameter("androidID", android_id)
                .build();

        Log.d("HTTP REQUEST", uri.toString());
        new SendRequestByURL().execute(uri.toString());
    }
}