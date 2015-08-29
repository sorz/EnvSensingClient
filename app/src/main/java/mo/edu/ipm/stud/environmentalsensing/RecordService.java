package mo.edu.ipm.stud.environmentalsensing;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by xierch on 2015/8/29.
 */
public class RecordService extends Service {
    static private final String TAG = "RecordService";
    static private final int NOTIFICATION = R.string.record_service_running;
    // Reference:
    // https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    static private boolean running = false;

    private final IBinder binder = new LocalBinder();
    private NotificationManager notificationManager;

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
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        System.out.println("created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION);
        running = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    static public boolean isRunning() {
        return running;
    }

    private void showNotification() {
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setTicker(getText(R.string.record_service_running))
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.record_service_running))
                .build();
        notificationManager.notify(NOTIFICATION, notification);
    }
}