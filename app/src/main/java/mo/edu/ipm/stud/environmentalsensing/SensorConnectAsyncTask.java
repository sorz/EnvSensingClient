package mo.edu.ipm.stud.environmentalsensing;

import android.os.AsyncTask;

import com.sensorcon.sensordrone.android.Drone;

/**
 * Connect to specify Sensordrone device via Bluetooth.
 *
 * The Drone.btConnect() is a block method although it trigger CONNECTED event.
 * So we have to use AsyncTask to wrap it.
 *
 * Note that CONNECTED event will be sent in background thread.
 */
public class SensorConnectAsyncTask extends AsyncTask<String, Void, Boolean> {

    @Override
    protected Boolean doInBackground(String... mac) {
        Drone drone = SensorDrone.getInstance();
        return drone.btConnect(mac[0]);
    }

}
