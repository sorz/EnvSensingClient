package mo.edu.ipm.stud.envsensing.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.sensorcon.sensordrone.android.Drone;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.tasks.SensorConnectAsyncTask;

/**
 * Manage sensor status.
 */
public class SensorService extends Service {
    static private final String TAG = "SensorService";
    static private final int ONGOING_NOTIFICATION_ID = 1;
    static public final String ACTION_CONNECT = SensorService.class.getName() + ".ACTION_CONNECT";
    static public final String ACTION_NEW_TASK = SensorService.class.getName() + ".ACTION_NEW_TASK";
    static public final String ACTION_STOP_TASK = SensorService.class.getName() + ".ACTION_STOP_TASK";
    static private final String ACTION_DO_MEASURING = SensorService.class.getName() + ".ACTION_DO_MEASURING";
    static private final String ACTION_GO_READY = SensorService.class.getName() + ".GO_READY";
    static public final String EXTRA_TASK_END = "extra-task-end";
    static public final String EXTRA_TASK_TAG = "extra-task-tag";

    static private final int HEATING_SECONDS = 10;  // TODO: change heating time.
    static private final int MEASURE_TIMEOUT = 12 * 1000; // 12 seconds

    static private boolean running = false;

    // System services
    private SharedPreferences preferences;
    private AlarmManager alarmManager;

    public enum SensorState {
        DISCONNECTED, CONNECTING, HEATING, READY, TASK_REST, TASK_MEASURING
    }

    private Drone drone;
    private SensorState serviceState = SensorState.DISCONNECTED;

    // Task related
    private Intent currentTaskIntent;
    private PendingIntent taskDoMeasuringPendingIntent;
    private long taskDoMeasuringInterval;
    private long taskDoMeasuringWindowLength;
    private long taskStartTime;
    private long taskAutoEndTime;
    private int taskTotalMeasuringCount;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static public boolean isRunning() {
        return running;
    }

    public SensorState getState() {
        return serviceState;
    }

    @Override
    public void onCreate() {
        running = true;
        drone = new Drone();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        updateServiceState(SensorState.DISCONNECTED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        //stopForeground(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        if (intent == null)
            return START_NOT_STICKY;
        if (ACTION_CONNECT.equals(intent.getAction())) {
            actionConnect();
        } else if (ACTION_GO_READY.equals(intent.getAction())) {
            actionGoReady();
        } else if (ACTION_NEW_TASK.equals(intent.getAction())) {
            actionNewTask(intent);
        } else if (ACTION_DO_MEASURING.equals(intent.getAction())) {
            actionDoMeasuring();
        }

        return START_NOT_STICKY;
    }


    private void actionConnect() {
        String mac = preferences.getString(getString(R.string.pref_bluetooth_mac), null);
        if (mac == null) {
            Toast.makeText(this, R.string.need_to_select_sensor, Toast.LENGTH_SHORT).show();
            // TODO: Show sensor selection fragment directly?
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, R.string.bluetooth_disabled, Toast.LENGTH_SHORT).show();
            return;
        }
        new SensorConnectAsyncTask() {
            @Override
            protected void onPreExecute () {
                updateServiceState(SensorState.CONNECTING);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    updateServiceState(SensorState.HEATING);
                    actionHeating();
                } else {
                    Toast.makeText(SensorService.this,
                            R.string.connect_fail, Toast.LENGTH_SHORT).show();
                    updateServiceState(SensorState.DISCONNECTED);
                }
            }
        }.execute(mac);
    }

    private void actionHeating() {
        drone.enableTemperature();
        drone.enableHumidity();
        drone.enablePressure();
        drone.enablePrecisionGas();
        drone.enableOxidizingGas();
        drone.enableReducingGas();

        Intent intent = new Intent(this, SensorService.class);
        intent.setAction(SensorService.ACTION_GO_READY);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        // TODO: AllowWhileIdle for Android 6.0+
        alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + HEATING_SECONDS * 1000,
                HEATING_SECONDS * 500, pendingIntent);
    }

    private void actionGoReady() {
        if (serviceState != SensorState.HEATING && serviceState != SensorState.TASK_REST) {
            Log.wtf(TAG, String.format("Wrong state. Cannot change from %s to %s.",
                    serviceState, SensorState.READY));
            return;
        }
        // TODO: Check task queue.
        updateServiceState(SensorState.READY);
    }

    private void actionNewTask(Intent intent) {
        if (serviceState == SensorState.HEATING) {
            // TODO: add task to queue.
            Log.e(TAG, "Pending task not yet implemented, ignored.");
        } else if (serviceState != SensorState.READY) {
            Log.wtf(TAG, String.format(
                    "Wrong state. Cannot start new task under %s.", serviceState));
            return;
        }
        currentTaskIntent = intent;
        taskStartTime = SystemClock.elapsedRealtime();
        taskAutoEndTime = intent.getLongExtra(EXTRA_TASK_END, 0);
        taskTotalMeasuringCount = 0;
        taskDoMeasuringInterval = Integer.parseInt(
                preferences.getString(
                        getString(R.string.pref_recording_interval), "300")) * 1000;
        taskDoMeasuringWindowLength = taskDoMeasuringInterval / 2;
        if (preferences.getBoolean(getString(R.string.pref_recording_exact_interval), false))
            taskDoMeasuringWindowLength = 1000;

        Intent measuringIntent = new Intent(this, SensorService.class);
        measuringIntent.setAction(ACTION_DO_MEASURING);
        taskDoMeasuringPendingIntent = PendingIntent.getService(this, 0, measuringIntent, 0);

        alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                taskStartTime, taskDoMeasuringWindowLength, taskDoMeasuringPendingIntent);
    }

    private void actionDoMeasuring() {
        if (serviceState != SensorState.TASK_REST) {
            Log.wtf(TAG, String.format("Wrong state. Cannot do measuring under %s.", serviceState));
            return;
        }
        // TODO: do measuring.
        Log.d(TAG, "Do measuring...");

        // Schedule next measuring or end task.
        taskTotalMeasuringCount ++;
        long nextMeasuringTime = taskStartTime + taskTotalMeasuringCount * taskDoMeasuringInterval;
        if (nextMeasuringTime > taskAutoEndTime) {
            updateServiceState(SensorState.READY);
        } else {
            alarmManager.setWindow(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    nextMeasuringTime, taskDoMeasuringWindowLength, taskDoMeasuringPendingIntent);
        }
    }

    /**
     * Set new `serviceState` and update notification.
     * This method doesn't change anything other than `serviceState` and notification.
     */
    private void updateServiceState(SensorState newState) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_av_hearing);
        switch (newState) {
            case DISCONNECTED:
                Intent connectIntent = new Intent(this, SensorService.class);
                connectIntent.setAction(SensorService.ACTION_CONNECT);
                PendingIntent pendingIntent = PendingIntent.getService(this, 0, connectIntent, 0);
                builder.setContentTitle(getText(R.string.notification_disconnected_title))
                        .setContentText(getText(R.string.notification_disconnected_text))
                        .setContentIntent(pendingIntent);
                break;
            case CONNECTING:
                builder.setContentTitle(getText(R.string.notification_connecting_title))
                        .setContentText(getText(R.string.notification_connecting_text));
                break;
            case HEATING:
                builder.setContentTitle(getText(R.string.notification_heating_title))
                        .setContentText(getText(R.string.notification_heating_text));
                break;
            case READY:
                builder.setContentTitle(getText(R.string.notification_ready_title))
                        .setContentText(getText(R.string.notification_ready_text));
                break;
            case TASK_REST:
                builder.setContentTitle(getText(R.string.notification_rest_title))
                        .setContentText(getText(R.string.notification_rest_text));
                break;
            case TASK_MEASURING:
                builder.setContentTitle(getText(R.string.notification_measuring_title))
                        .setContentText(getText(R.string.notification_measuring_text));
                break;
            default:
                Log.wtf(TAG, "Attempting change to an unknown state.");
                break;
        }
        startForeground(ONGOING_NOTIFICATION_ID, builder.build());
        serviceState = newState;
    }

}
