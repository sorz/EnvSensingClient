package mo.edu.ipm.stud.environmentalsensing;

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
