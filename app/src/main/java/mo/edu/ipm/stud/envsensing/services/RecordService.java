package mo.edu.ipm.stud.envsensing.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sensorcon.sensordrone.android.Drone;

import java.util.Date;

import mo.edu.ipm.stud.envsensing.MainActivity;
import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.SensorDrone;
import mo.edu.ipm.stud.envsensing.entities.Humidity;
import mo.edu.ipm.stud.envsensing.entities.InvalidValueException;
import mo.edu.ipm.stud.envsensing.entities.LocationInfo;
import mo.edu.ipm.stud.envsensing.entities.Measurement;
import mo.edu.ipm.stud.envsensing.entities.Monoxide;
import mo.edu.ipm.stud.envsensing.entities.OxidizingGas;
import mo.edu.ipm.stud.envsensing.entities.Pressure;
import mo.edu.ipm.stud.envsensing.entities.ReducingGas;
import mo.edu.ipm.stud.envsensing.entities.Temperature;
import mo.edu.ipm.stud.envsensing.tasks.SensorConnectAsyncTask;
import mo.edu.ipm.stud.envsensing.tasks.SensorMeasureAsyncTask;

/**
 * Do measure periodically and stick a notification.
 */
public class RecordService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static private final String TAG = "RecordService";
    static private final int ONGOING_NOTIFICATION_ID = 1;
    static private final int MEASURE_TIMEOUT = 30 * 1000; // 30 seconds
    static public final String ACTION_NEW = RecordService.class.getName() + ".ACTION_NEW";
    static public final String ACTION_MEASURE = RecordService.class.getName() + ".ACTION_MEASURE";
    static public final String ACTION_STOP = RecordService.class.getName() + ".ACTION_STOP";
    static public final String EXTRA_RECORDING_START = "extra-recording-start";
    static public final String EXTRA_RECORDING_END = "extra-recording-end";

    // Reference:
    // https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    static private boolean running = false;
    private final IBinder binder = new LocalBinder();
    private SharedPreferences preferences;
    private AlarmManager alarmManager;
    private GoogleApiClient apiClient;
    private Drone drone;

    private long recording_start;
    private long recording_stop;
    private PendingIntent pendingIntent;
    private boolean exactInterval;
    private long interval;
    private long nextMeasureTime;
    private int measureSuccessCounter;
    private int measureFailCounter;

    private PowerManager.WakeLock wakeLock;
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private Measurement thisMeasurement;
    private boolean thisMeasureSuccess;
    private boolean thisMeasureFail;
    private boolean thisLocationDone;

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google API suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Google API connection failed.");
    }

    public class LocalBinder extends Binder {
        public RecordService getService() {
            return RecordService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        running = true;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        drone = SensorDrone.getInstance();
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MainActivity.ACTION_SHOW_RECORD_STATUS);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setTicker(getText(R.string.record_service_running))
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.record_service_running))
                .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
        timeoutHandler = new Handler();
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                finishThisMeasure();
            }
        };
    }

    @Override
    public void onDestroy() {
        apiClient.disconnect();
        running = false;
        stopForeground(true);
        if (drone.isConnected)
            // TODO: Use a connection counter to avoid disturbing who is using it?
            drone.disconnect();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        if (ACTION_NEW.equals(intent.getAction())) {
            // TODO: Check existed task.
            recording_start = intent.getLongExtra(EXTRA_RECORDING_START, 0);
            recording_stop = intent.getLongExtra(EXTRA_RECORDING_END, 0);
            if (SystemClock.elapsedRealtime() > recording_stop)
                return finishTask();

            Intent measureIntent = new Intent(this, RecordService.class);
            measureIntent.setAction(ACTION_MEASURE);
            pendingIntent = PendingIntent.getService(this, 0, measureIntent, 0);
            interval = Integer.parseInt(
                    preferences.getString(
                            getString(R.string.pref_recording_interval), "300")) * 1000;
            alarmManager.cancel(pendingIntent);

            exactInterval = preferences.getBoolean(
                    getString(R.string.pref_recording_exact_interval), false);
            if (exactInterval) {
                nextMeasureTime = recording_start;
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextMeasureTime, pendingIntent);
            } else {
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        recording_start, interval, pendingIntent);
            }
            return START_REDELIVER_INTENT;

        } else if (ACTION_MEASURE.equals(intent.getAction())) {
            if (SystemClock.elapsedRealtime() > recording_stop)
                return finishTask();
            if (SystemClock.elapsedRealtime() < recording_start)
                return START_NOT_STICKY;
            if (exactInterval) {
                nextMeasureTime += interval;
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        nextMeasureTime, pendingIntent);
            }
            doMeasure();
            return START_NOT_STICKY;
        } else if (ACTION_STOP.equals(intent.getAction())) {
            return finishTask();
        } else {
            return START_NOT_STICKY;
        }
    }

    private void doMeasure() {
        Log.d(TAG, "Measuring...");
        wakeLock.acquire(MEASURE_TIMEOUT + 3000);
        // Timeout of the handler must less than WakeLock's and large than the sum of
        // SensorConnectAsyncTask's + SensorMeasureAsyncTask's.
        // Then only goal of this handler is calling finishThisMeasure().
        timeoutHandler.postDelayed(timeoutRunnable, MEASURE_TIMEOUT);

        thisMeasureSuccess = thisMeasureFail = thisLocationDone = false;
        thisMeasurement = new Measurement();
        thisMeasurement.save();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);

        // TODO: Active check connection status?
        if (drone.isConnected) {
            sendMeasureRequest();
        } else {
            Log.d(TAG, "Connecting to sensor.");
            new SensorConnectAsyncTask() {
                @Override
                public void onPostExecute(Boolean result) {
                    if (result) {
                        sendMeasureRequest();
                    } else {
                        Log.w(TAG, "Measure failed: cannot connect to sensor.");
                        setMeasureDone(false);
                    }
                }
            }.execute(preferences.getString(getString(R.string.pref_bluetooth_mac), ""));
        }
    }

    private void sendMeasureRequest() {
        Log.d(TAG, "Sending measure requests.");

        new SensorMeasureAsyncTask().execute(new SensorMeasureAsyncTask.OnMeasureDone() {
            @Override
            public void onMeasureDone(boolean[] measured) {
                Log.d(TAG, "Measured.");
                storeMeasureResult(measured);
            }
        });
    }

    private void storeMeasureResult(boolean[] sensors) {
        try {
            if (sensors[SensorMeasureAsyncTask.SENSOR_TEMPERATURE]) {
                Log.d(TAG, "Temperature: " + drone.temperature_Celsius);
                new Temperature(thisMeasurement, drone.temperature_Kelvin).save();
            }
            if (sensors[SensorMeasureAsyncTask.SENSOR_HUMIDITY]) {
                Log.d(TAG, "Humidity: " + drone.humidity_Percent);
                new Humidity(thisMeasurement, drone.humidity_Percent).save();
            }
            if (sensors[SensorMeasureAsyncTask.SENSOR_MONOXIDE]) {
                Log.d(TAG, "Monoxide: " + drone.precisionGas_ppmCarbonMonoxide);
                new Monoxide(thisMeasurement, drone.precisionGas_ppmCarbonMonoxide).save();
            }
            if (sensors[SensorMeasureAsyncTask.SENSOR_PRESSURE]) {
                Log.d(TAG, "Pressure: " + drone.pressure_Pascals);
                if (drone.pressure_Pascals > 0)
                    new Pressure(thisMeasurement, drone.pressure_Pascals).save();
            }
            if (sensors[SensorMeasureAsyncTask.SENSOR_OXIDIZING]) {
                Log.d(TAG, "Oxidizing gas: " + drone.oxidizingGas_Ohm);
                new OxidizingGas(thisMeasurement, drone.oxidizingGas_Ohm).save();
            }
            if (sensors[SensorMeasureAsyncTask.SENSOR_REDUCING]) {
                Log.d(TAG, "Reducing gas: " + drone.reducingGas_Ohm);
                new ReducingGas(thisMeasurement, drone.reducingGas_Ohm).save();
            }
        } catch (InvalidValueException e) {
            Log.w(TAG, e.getMessage());
        }
        // Only fail this measure if every sensors all are failed.
        boolean success = false;
        for (boolean ok : sensors) {
            if (ok) {
                success = true;
                break;
            }
        }
        setMeasureDone(success);
    }

    private void setMeasureDone(boolean success) {
        thisMeasureFail = ! (thisMeasureSuccess = success);
        if (thisLocationDone)
            finishThisMeasure();
    }

    private void setLocationDone() {
        thisLocationDone = true;
        if (thisMeasureSuccess || thisMeasureFail)
            finishThisMeasure();
    }

    /**
     * Called when
     *   1. This measure has been success & location has been got.
     *      (thisMeasureSuccess == true && thisMeasureFail == false && locationDone == true)
     *   2. This measure has failed & location has been got.
     *      (thisMeasureSuccess == false && thisMeasureFail == true && locationDone == true)
     *   3. timeoutHandler call timeoutRunnable when above 2 case not occur after a certain time.
     */
    private void finishThisMeasure() {
        timeoutHandler.removeCallbacks(timeoutRunnable);
        if (thisMeasureSuccess && thisLocationDone)
            measureSuccessCounter ++;
        else
            measureFailCounter ++;
        if (wakeLock.isHeld())
            wakeLock.release();
    }

    /**
     * Stop this service while cancel the alarm if necessary.
     * @return START_NOT_STICKY, for convenience.
     */
    private int finishTask() {
        if (pendingIntent != null)
            alarmManager.cancel(pendingIntent);
        stopSelf();
        return START_NOT_STICKY;
    }

    static public boolean isRunning() {
        return running;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed: " + location);
        Log.d(TAG, "Provider: " + location.getProvider());
        Log.d(TAG, "Accuracy: " + location.getAccuracy() + "m");
        Log.d(TAG, "Elapsed time: " +
                (SystemClock.elapsedRealtimeNanos()
                        - location.getElapsedRealtimeNanos()) / 1000000000);
        new LocationInfo(thisMeasurement, location).save();
        setLocationDone();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Location related, seems never be invoked.
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                Log.d(TAG, provider + " out of service");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d(TAG, provider + " temporarily unavailable");
                break;
            case LocationProvider.AVAILABLE:
                Log.d(TAG, provider + " available");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Location related, seems never be invoked.
        Log.d(TAG, "Provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Location related, seems never be invoked.
        Log.d(TAG, "Provider disabled: " + provider);
    }

    public Date getStartTime() {
        return new Date(System.currentTimeMillis() - SystemClock.elapsedRealtime()
                + recording_start);
    }

    public Date getStopTime() {
        return new Date(System.currentTimeMillis() - SystemClock.elapsedRealtime()
                + recording_stop);
    }

    public long getInterval() {
        return interval;
    }

    public int getMeasureSuccessCount() {
        return measureSuccessCounter;
    }

    public int getMeasureFailCount() {
        return measureFailCounter;
    }

}