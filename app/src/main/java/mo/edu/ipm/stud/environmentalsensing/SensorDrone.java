package mo.edu.ipm.stud.environmentalsensing;

import com.sensorcon.sensordrone.android.Drone;

/**
 * Keep a singleton of Drone.
 */
public class SensorDrone {
    private static Drone drone;

    public static Drone getInstance() {
        if (drone == null)
            drone = new Drone();
        return drone;
    }
}
