package dk.itu.ubicomp.android.contextservice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.*;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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

    public static final long NOTIFY_INTERVAL = 1000; // 1 seconds
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        setupSensors();
        showNotification();
        startTrackingSensorInfo();
    }

    private void startTrackingSensorInfo()
    {
        if(mTimer != null) {
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
//        Log.d("SENSOR VALUES", mLocationValue != null ? mLocationValue : "NULL");
        Log.d("SENSOR VALUES", mPressureValue != null ? mPressureValue : "NULL");
        Log.d("SENSOR VALUES", mRotationValue != null ? mRotationValue : "NULL");
//        Toast.makeText(getApplicationContext(), "Tracking", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

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

    class RotationListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mRotationValue = Float.toString(event.values[0]);
        }
    }

    class PressureListener implements SensorEventListener {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mPressureValue = Float.toString(event.values[0]);
        }
    }
}