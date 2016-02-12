package mo.edu.ipm.stud.envsensing.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.tasks.UploadAsyncTask;

/**
 * Upload measurement while display progress.
 */
public class UploadService extends Service {
    static private final String TAG = "UploadService";
    static private final int NOTIFICATION_ID = 1;
    static public final String ACTION_START = UploadService.class.getName() + ".ACTION_START";
    static public final String ACTION_STOP = UploadService.class.getName() + ".ACTION_STOP";

    static private boolean running = false;

    private static PendingIntent stopServiceIntent;
    private static UploadAsyncTask uploadAsyncTask;

    public static boolean isRunning() {
        return running;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        running = true;

        Intent stopIntent = new Intent(this, UploadService.class);
        stopIntent.setAction(ACTION_STOP);
        stopServiceIntent = PendingIntent.getService(this, 0, stopIntent, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);

        if (ACTION_START.equals(intent.getAction())) {
            if (uploadAsyncTask != null) {
                stopSelf();
                return START_NOT_STICKY;
            }
            updateNotification(-1);
            startUploadTask();
            return START_REDELIVER_INTENT;
        } else if (ACTION_STOP.equals(intent.getAction())) {
            if (uploadAsyncTask != null && !uploadAsyncTask.isCancelled())
                uploadAsyncTask.cancel(true);
            return START_NOT_STICKY;
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    private void startUploadTask() {
        uploadAsyncTask = new UploadAsyncTask(this) {
            @Override
            protected void onProgressUpdate(Float... progress) {
                updateNotification(progress[0]);
            }

            @Override
            protected void onPostExecute(Pair<Boolean, Long> result) {
                Log.d(TAG, "AsyncTask done.");
                uploadAsyncTask = null;
                // TODO: add a finish notification.
                stopForeground(true);
                stopSelf();
            }

            @Override
            protected void onCancelled(Pair<Boolean, Long> result) {
                Log.d(TAG, "AsyncTask canceled.");
                uploadAsyncTask = null;
                // TODO: add a cancel notification.
                stopForeground(true);
                stopSelf();
            }

        };
        uploadAsyncTask.execute();
    }

    private void updateNotification(float progress) {
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_file_cloud_upload)
                .setContentIntent(stopServiceIntent)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.uploading))
                .setContentText(getText(R.string.touch_for_cancel_upload))
                .setTicker(getText(R.string.touch_for_cancel_upload))
                .setProgress(100, progress < 0 ? 0 : (int) (progress * 100), progress < 0)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

}
