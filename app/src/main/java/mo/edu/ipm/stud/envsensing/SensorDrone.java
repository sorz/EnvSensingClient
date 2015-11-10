package mo.edu.ipm.stud.envsensing;

import android.util.Log;

import com.sensorcon.sensordrone.android.Drone;

/**
 * Keep a singleton of Drone.
 * Must use this class to get the instance of Drone.
 */
public class SensorDrone {
    private static final String TAG = "SensorDrone";
    private static Drone drone;
    private static int userCounter;

    public static synchronized Drone getInstance() {
        if (drone == null)
            drone = new Drone();
        userCounter += 1;
        Log.d(TAG, "Instance required, current usage: " + userCounter);
        return drone;
    }

    public static synchronized void release() {
        userCounter -= 1;
        Log.d(TAG, "Instance released, current usage: " + userCounter);
        if (userCounter <= 0) {
            if (drone.isConnected) {
                Log.d(TAG, "Disconnect.");
                drone.disconnect();
            }
            userCounter = 0;
        }
    }
}
