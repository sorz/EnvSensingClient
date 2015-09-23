package mo.edu.ipm.stud.envsensing.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.tasks.UploadAsyncTask;

/**
 * Upload measurement while display progress.
 */
public class UploadService extends Service {
    static private final String TAG = "UploadService";
    private static final int NOTIFICATION_ID = 0;

    static private boolean running = false;

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);

        updateNotification(0);
        new UploadAsyncTask(this) {
            @Override
            protected void onProgressUpdate(Float... progress) {
                updateNotification(progress[0]);
            }

            @Override
            protected void onPostExecute(Void result) {
                Log.d(TAG, "AsyncTask done.");
                // Add a finish notification.
                stopForeground(true);
                stopSelf();
            }

        }.execute();

        return START_REDELIVER_INTENT;
    }

    private void updateNotification(float progress) {
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.uploading))
                .setProgress(100, (int) progress * 100, false)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

}
