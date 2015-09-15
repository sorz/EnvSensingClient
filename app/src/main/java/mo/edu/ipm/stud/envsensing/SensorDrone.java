package mo.edu.ipm.stud.envsensing;

import com.sensorcon.sensordrone.android.Drone;

/**
 * Keep a singleton of Drone.
 * Must use this class to get the instance of Drone.
 */
public class SensorDrone {
    private static Drone drone;

    public static Drone getInstance() {
        if (drone == null)
            drone = new Drone();
        return drone;
    }
}
