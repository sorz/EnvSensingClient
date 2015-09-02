package mo.edu.ipm.stud.environmentalsensing;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sensorcon.sensordrone.android.Drone;

import mo.edu.ipm.stud.environmentalsensing.entites.Humidity;
import mo.edu.ipm.stud.environmentalsensing.entites.Measurement;
import mo.edu.ipm.stud.environmentalsensing.entites.Temperature;
import mo.edu.ipm.stud.environmentalsensing.tasks.SensorConnectAsyncTask;
import mo.edu.ipm.stud.environmentalsensing.tasks.SensorMeasureAsyncTask;

/**
 * Do measure periodically and stick a notification.
 */
public class RecordService extends Service {
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
    private Drone drone;

    private long recording_start;
    private long recording_stop;
    private PendingIntent pendingIntent;
    private boolean exactInterval;
    private long interval;
    private long nextMeasureTime;
    private PowerManager.WakeLock wakeLock;


    public class LocalBinder extends Binder {
        RecordService getService() {
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
                    preferences.getString("pref_recording_interval", "300")) * 1000;
            alarmManager.cancel(pendingIntent);

            exactInterval = preferences.getBoolean("pref_recording_exact_interval", false);
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
            }.execute(preferences.getString("pref_bluetooth_mac", ""));
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
        Measurement measurement = new Measurement();
        measurement.save();

        if (sensors[SensorMeasureAsyncTask.SENSOR_TEMPERATURE]) {
            Log.d(TAG, "Temperature: " + drone.temperature_Celsius);
            Temperature temperature = new Temperature(measurement);
            temperature.setValue(drone.temperature_Kelvin);
            temperature.save();
        }

        if (sensors[SensorMeasureAsyncTask.SENSOR_HUMIDITY]) {
            Log.d(TAG, "Humidity: " + drone.humidity_Percent);
            Humidity humidity = new Humidity(measurement);
            humidity.setValue(drone.humidity_Percent);
            humidity.save();
        }

        if (wakeLock.isHeld())
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

}