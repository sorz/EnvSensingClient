package mo.edu.ipm.stud.envsensing.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
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
    static private final String ACTION_DO_MEARUEING = SensorService.class.getName() + ".ACTION_DO_MEASURING";

    static private boolean running = false;

    // System services
    private NotificationManager notificationManager;
    private SharedPreferences preferences;

    private enum SensorState {
        DISCONNECTED, CONNECTING, HEATING, READY, TASK_REST, TASK_MEASURING
    }

    private Drone drone;
    private SensorState serviceState = SensorState.DISCONNECTED;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static public boolean isRunning() {
        return running;
    }

    @Override
    public void onCreate() {
        running = true;
        drone = new Drone();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        if (ACTION_CONNECT.equals(intent.getAction())) {
            actionConnect();
            return START_STICKY;
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
                } else {
                    Toast.makeText(SensorService.this,
                            R.string.connect_fail, Toast.LENGTH_SHORT).show();
                    updateServiceState(SensorState.DISCONNECTED);
                }
            }
        }.execute(mac);
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
                builder.setContentTitle(getText(R.string.notification_disconnected_title))
                        .setContentText(getText(R.string.notification_disconnected_text));
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
