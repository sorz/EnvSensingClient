package mo.edu.ipm.stud.envsensing.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;

import mo.edu.ipm.stud.envsensing.SensorDrone;


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

    public static final int TOTAL_SENSOR = 6;
    public static final int SENSOR_TEMPERATURE = 0;
    public static final int SENSOR_HUMIDITY = 1;
    public static final int SENSOR_MONOXIDE = 2;
    public static final int SENSOR_PRESSURE = 3;
    public static final int SENSOR_OXIDIZING = 4;
    public static final int SENSOR_REDUCING = 5;

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
                disableHighPowerSensor();
                SensorMeasureAsyncTask.this.callback.onMeasureDone(measured);
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT);
        drone.registerDroneListener(this);

        failed[SENSOR_TEMPERATURE] = drone.temperatureStatus ?
                !drone.measureTemperature() : !drone.enableTemperature();
        failed[SENSOR_HUMIDITY] = drone.humidityStatus ?
                !drone.measureHumidity() : !drone.enableHumidity();
        failed[SENSOR_MONOXIDE] = drone.precisionGasStatus ?
                !drone.measurePrecisionGas() : !drone.enablePrecisionGas();
        failed[SENSOR_PRESSURE] = drone.pressureStatus ?
                !drone.measurePressure() : !drone.enablePressure();
        failed[SENSOR_OXIDIZING] = drone.oxidizingGasStatus ?
                !drone.measureOxidizingGas() : !drone.enableOxidizingGas();
        failed[SENSOR_REDUCING] = drone.reducingGasStatus ?
                !drone.measureReducingGas() : !drone.enableReducingGas();

        // TODO: Add more sensors here?
        // TODO: Handle all failed case before timeout.

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
        else if (event.matches(DroneEventObject.droneEventType.PRECISION_GAS_ENABLED)
                & !measured[SENSOR_MONOXIDE])
            drone.measurePrecisionGas();
        else if (event.matches(DroneEventObject.droneEventType.PRESSURE_ENABLED)
                & !measured[SENSOR_PRESSURE])
            drone.measurePressure();
        else if (event.matches(DroneEventObject.droneEventType.OXIDIZING_GAS_ENABLED)
                & !measured[SENSOR_OXIDIZING])
            drone.measureOxidizingGas();
        else if (event.matches(DroneEventObject.droneEventType.REDUCING_GAS_ENABLED)
                & !measured[SENSOR_REDUCING])
            drone.measureReducingGas();

        // TODO: Add more sensors here?

        else if (event.matches(DroneEventObject.droneEventType.TEMPERATURE_MEASURED))
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

        // TODO: Add more sensors here?

        if (hasFinished()) {
            Log.d(TAG, "Measure finish, callback.");
            drone.unregisterDroneListener(this);
            timeoutHandler.removeCallbacks(timeoutRunnable);
            disableHighPowerSensor();
            callback.onMeasureDone(measured);
        }
    }

    private void disableHighPowerSensor() {
        if (drone.statusOfOxidizingGas())
            drone.disableOxidizingGas();
        if (drone.statusOfReducingGas())
            drone.disableReducingGas();
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
