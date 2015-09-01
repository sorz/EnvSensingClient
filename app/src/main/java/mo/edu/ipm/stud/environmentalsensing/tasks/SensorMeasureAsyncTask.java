package mo.edu.ipm.stud.environmentalsensing.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;

import mo.edu.ipm.stud.environmentalsensing.SensorDrone;


/**
 * Enable sensors, wait seconds, and send measure requests.
 *
 * Result is a boolean array to indicate whether specific measure request is sent successfully.
 */
public class SensorMeasureAsyncTask
        extends AsyncTask<SensorMeasureAsyncTask.OnMeasureDone, Void, Void>
        implements DroneEventHandler {
    private static final String TAG = "SensorMeasureAsyncTask";
    private static final long TIMEOUT = 10 * 1000; // 10 seconds

    public static final int TOTAL_SENSOR = 2;
    public static final int SENSOR_TEMPERATURE = 0;
    public static final int SENSOR_HUMIDITY = 1;

    private Drone drone = SensorDrone.getInstance();
    private boolean[] measured = new boolean[TOTAL_SENSOR];
    private boolean[] failed = new boolean[TOTAL_SENSOR];
    private OnMeasureDone callback;
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;

    public interface OnMeasureDone {
        /**
         * Callback on measure done.
         * @param measured Indicate which sensors are measured successfully.
         */
        public void onMeasureDone(boolean[] measured);
    }

    @Override
    protected Void doInBackground(OnMeasureDone... callback) {
        this.callback = callback[0];
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Measure timeout, callback.");
                drone.unregisterDroneListener(SensorMeasureAsyncTask.this);
                SensorMeasureAsyncTask.this.callback.onMeasureDone(measured);
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT);
        drone.registerDroneListener(this);

        failed[SENSOR_TEMPERATURE] =
                drone.temperatureStatus ? drone.measureTemperature() : !drone.enableTemperature();
        failed[SENSOR_HUMIDITY] =
                drone.humidityStatus ? drone.measureHumidity() : !drone.enableHumidity();

        // TODO: Add more sensors here.
        // TODO: Handle all failed before timeout.

        return null;
    }


    @Override
    public void parseEvent(DroneEventObject event) {
        Log.d(TAG, "Event: " + event);
        if (event.matches(DroneEventObject.droneEventType.TEMPERATURE_ENABLED)
                & !measured[SENSOR_TEMPERATURE])
            drone.measureTemperature();
        else if (event.matches(DroneEventObject.droneEventType.HUMIDITY_ENABLED)
                & !measured[SENSOR_HUMIDITY])
            drone.measureHumidity();

        // TODO: Add more sensors here.


        else if (event.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED))
            measured[SENSOR_TEMPERATURE] = true;
        else if (event.matches(DroneEventObject.droneEventType.HUMIDITY_MEASURED))
            measured[SENSOR_HUMIDITY] = true;

        // TODO: Add more sensors here.

        if (hasFinished()) {
            Log.d(TAG, "Measure finish, callback.");
            drone.unregisterDroneListener(this);
            timeoutHandler.removeCallbacks(timeoutRunnable);
            callback.onMeasureDone(measured);
        }
    }

    /**
     * Check whether all sensors measure has finished.
     * @return true if all sensors are either finished or failed.
     */
    private boolean hasFinished() {
        for (int i=0; i<TOTAL_SENSOR; ++i)
            if (!(measured[i] || failed[i]))
                return false;
        return true;
    }
}
