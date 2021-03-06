package mo.edu.ipm.stud.envsensing.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;


/**
 * Enable sensors, wait seconds, and send measure requests.
 *
 * Result is a boolean array to indicate whether specific measure request is sent successfully.
 */
public class SensorMeasureAsyncTask
        extends AsyncTask<SensorMeasureAsyncTask.OnMeasureDone, Void, Void>
        implements DroneEventHandler {
    private static final String TAG = "SensorMeasureAsyncTask";
    private static final long TIMEOUT = 20 * 1000; // 20 seconds

    public static final int TOTAL_SENSOR = 6;
    public static final int SENSOR_TEMPERATURE = 0;
    public static final int SENSOR_HUMIDITY = 1;
    public static final int SENSOR_MONOXIDE = 2;
    public static final int SENSOR_PRESSURE = 3;
    public static final int SENSOR_OXIDIZING = 4;
    public static final int SENSOR_REDUCING = 5;

    private Drone drone;
    private boolean[] measured = new boolean[TOTAL_SENSOR];
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

    public SensorMeasureAsyncTask(Drone drone) {
        this.drone = drone;
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
        Log.d(TAG, "Sending measure requests.");
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT);
        drone.registerDroneListener(this);

        boolean success;
        success = drone.measureTemperature();
        success &= drone.measureHumidity();
        success &= drone.measurePressure();
        success &= drone.measurePrecisionGas();
        success &= drone.measureOxidizingGas();
        success &= drone.measureReducingGas();
        if (!success) {
            Log.w(TAG, "Sent measure requests failed.");
        } else {
            Log.d(TAG, "Sent successfully.");
        }
        return null;
    }


    @Override
    public void parseEvent(DroneEventObject event) {
        Log.d(TAG, "Event: " + event);

        if (event.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED))
            measured[SENSOR_TEMPERATURE] = true;
        else if (event.matches(DroneEventObject.droneEventType.HUMIDITY_MEASURED))
            measured[SENSOR_HUMIDITY] = true;
        else if (event.matches(DroneEventObject.droneEventType.PRECISION_GAS_MEASURED))
            measured[SENSOR_MONOXIDE] = true;
        else if (event.matches(DroneEventObject.droneEventType.PRESSURE_MEASURED))
            measured[SENSOR_PRESSURE] = true;
        else if (event.matches(DroneEventObject.droneEventType.OXIDIZING_GAS_MEASURED))
            measured[SENSOR_OXIDIZING] = true;
        else if (event.matches(DroneEventObject.droneEventType.REDUCING_GAS_MEASURED))
            measured[SENSOR_REDUCING] = true;

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
//        for (int i=0; i<TOTAL_SENSOR; ++i)
//            if (!(measured[i]))
//                return false;
//        return true;

        // Sensordrone has been broken...
        // Only three sensors works, others all not response.
        int count = 0;
        for (boolean received : measured)
            if (received)
                count ++;
        return count >= 3;
    }
}
