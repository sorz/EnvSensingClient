package mo.edu.ipm.stud.environmentalsensing;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensorcon.sensordrone.android.Drone;

import mo.edu.ipm.stud.environmentalsensing.entities.Humidity;
import mo.edu.ipm.stud.environmentalsensing.entities.LocationInfo;
import mo.edu.ipm.stud.environmentalsensing.entities.Measurement;
import mo.edu.ipm.stud.environmentalsensing.entities.Monoxide;
import mo.edu.ipm.stud.environmentalsensing.entities.OxidzingGas;
import mo.edu.ipm.stud.environmentalsensing.entities.Pressure;
import mo.edu.ipm.stud.environmentalsensing.entities.ReducingGas;
import mo.edu.ipm.stud.environmentalsensing.entities.Temperature;
import mo.edu.ipm.stud.environmentalsensing.tasks.SensorConnectAsyncTask;
import mo.edu.ipm.stud.environmentalsensing.tasks.SensorMeasureAsyncTask;

/**
 * Do measure periodically and stick a notification.
 */
public class RecordService extends Service implements LocationListener {
    static private final String TAG = "RecordService";
    static private final int ONGOING_NOTIFICATION_ID = 1;
    static private final int MEASURE_TIMEOUT = 60 * 1000; // 60 seconds
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
    private LocationManager locationManager;
    private Drone drone;

    private long recording_start;
    private long recording_stop;
    private PendingIntent pendingIntent;
    private boolean exactInterval;
    private long interval;
    private long nextMeasureTime;

    private PowerManager.WakeLock wakeLock;
    private Measurement measurement;
    private boolean measureDone;
    private boolean locationDone;

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
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        drone = SensorDrone.getInstance();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        stopForeground(true);
        if (drone.isConnected)
            // TODO: Use a connection counter to avoid disturbing who is using it?
            drone.disconnect();
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
        wakeLock.acquire(MEASURE_TIMEOUT);
        measureDone = locationDone = false;
        measurement = new Measurement();
        measurement.save();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setBearingRequired(false);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        // TODO: check permission?
        locationManager.requestSingleUpdate(criteria, this, getMainLooper());

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
                        if (wakeLock.isHeld())
                            wakeLock.release();
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
        if (sensors[SensorMeasureAsyncTask.SENSOR_TEMPERATURE]) {
            Log.d(TAG, "Temperature: " + drone.temperature_Celsius);
            new Temperature(measurement, drone.temperature_Kelvin).save();
        }
        if (sensors[SensorMeasureAsyncTask.SENSOR_HUMIDITY]) {
            Log.d(TAG, "Humidity: " + drone.humidity_Percent);
            new Humidity(measurement, drone.humidity_Percent).save();
        }
        if (sensors[SensorMeasureAsyncTask.SENSOR_MONOXIDE]) {
            Log.d(TAG, "Monoxide: " + drone.precisionGas_ppmCarbonMonoxide);
            new Monoxide(measurement, drone.precisionGas_ppmCarbonMonoxide).save();
        }
        if (sensors[SensorMeasureAsyncTask.SENSOR_PRESSURE]) {
            Log.d(TAG, "Pressure: " + drone.pressure_Pascals);
            new Pressure(measurement, drone.pressure_Pascals).save();
        }
        if (sensors[SensorMeasureAsyncTask.SENSOR_OXIDIZING]) {
            Log.d(TAG, "Oxidizing gas: " + drone.oxidizingGas_Ohm);
            new OxidzingGas(measurement, drone.oxidizingGas_Ohm).save();
        }
        if (sensors[SensorMeasureAsyncTask.SENSOR_REDUCING]) {
            Log.d(TAG, "Reducing gas: " + drone.reducingGas_Ohm);
            new ReducingGas(measurement, drone.reducingGas_Ohm).save();
        }
        measureDone = true;

        if (locationDone && wakeLock.isHeld())
            wakeLock.release();
    }

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
        new LocationInfo(measurement, location).save();
        locationDone = true;

        // TODO: Handle getting location timeout?
        if (measureDone && wakeLock.isHeld())
            wakeLock.release();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
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
        Log.d(TAG, "Provider enabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider disabled: " + provider);
    }

}