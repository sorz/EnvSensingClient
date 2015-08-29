package mo.edu.ipm.stud.environmentalsensing;

import android.os.AsyncTask;

import com.sensorcon.sensordrone.android.Drone;

/**
 * Connect to specify Sensordrone device via Bluetooth.
 */
public class SensorConnectAsyncTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... mac) {
        Drone drone = SensorDrone.getInstance();
        return drone.btConnect(mac[0]);
    }

}
