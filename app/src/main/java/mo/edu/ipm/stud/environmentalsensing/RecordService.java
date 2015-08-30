package mo.edu.ipm.stud.environmentalsensing;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by xierch on 2015/8/29.
 */
public class RecordService extends Service {
    static private final String TAG = "RecordService";
    static private final int ONGOING_NOTIFICATION_ID = 1;
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
    private long recording_start;
    private long recording_stop;
    private PendingIntent pendingIntent;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        if (ACTION_NEW.equals(intent.getAction())) {
            // TODO: Check existed task.
            recording_start = intent.getLongExtra(EXTRA_RECORDING_START, 0);
            recording_stop = intent.getLongExtra(EXTRA_RECORDING_END, 0);
            if (System.currentTimeMillis() > recording_stop)
                return finishTask();

            Intent measureIntent = new Intent(this, RecordService.class);
            measureIntent.setAction(ACTION_MEASURE);
            pendingIntent = PendingIntent.getService(this, 0, measureIntent, 0);
            int intervalSeconds = Integer.parseInt(
                    preferences.getString("pref_recording_interval", "300"));
            alarmManager.cancel(pendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,
                    intervalSeconds * 1000, pendingIntent);
            return START_REDELIVER_INTENT;

        } else if (ACTION_MEASURE.equals(intent.getAction())) {
            if (System.currentTimeMillis() > recording_stop)
                return finishTask();
            if (System.currentTimeMillis() < recording_start)
                return START_NOT_STICKY;

            Log.d(TAG, "Measuring...");

            return START_NOT_STICKY;
        } else if (ACTION_STOP.equals(intent.getAction())) {
            return finishTask();
        } else {
            return START_NOT_STICKY;
        }
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