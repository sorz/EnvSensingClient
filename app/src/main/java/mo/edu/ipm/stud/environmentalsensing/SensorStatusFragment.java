package mo.edu.ipm.stud.environmentalsensing;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;


/**
 * A {@link Fragment} used to display status of sensor.
 */
public class SensorStatusFragment extends Fragment implements DroneEventHandler {
    private Drone drone;
    private SharedPreferences preferences;

    private View layoutConnected;
    private View layoutDisconnected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drone = SensorDrone.getInstance();
        drone.registerDroneListener(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        drone.unregisterDroneListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_status, container, false);
        layoutConnected = view.findViewById(R.id.layout_connected);
        layoutDisconnected = view.findViewById(R.id.layout_disconnected);
        final Button buttonConnect = (Button) view.findViewById(R.id.button_connect);
        Button buttonDisconnect = (Button) view.findViewById(R.id.button_disconnect);

        layoutConnected.setVisibility(drone.isConnected ? View.VISIBLE : View.GONE);
        layoutDisconnected.setVisibility(drone.isConnected ? View.GONE : View.VISIBLE);


        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mac = preferences.getString("pref_bluetooth_mac", null);
                if (mac == null)
                    return;  // TODO: pop a message.
                new SensorConnectAsyncTask() {
                    @Override
                    protected void onPostExecute (Boolean result) {
                        if (!result)
                            Toast.makeText(getActivity(),
                                    R.string.connect_fail, Toast.LENGTH_SHORT).show();
                        buttonConnect.setEnabled(true);
                    }
                }.execute(mac);
                buttonConnect.setEnabled(false);
            }
        });

        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drone.disconnect();
            }
        });


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        layoutConnected = null;
        layoutDisconnected = null;
    }

    @Override
    public void parseEvent(final DroneEventObject event) {
        // This may be invoke in thread other than UI thread. (e.g. CONNECTED may fired
        // on doInBackground() of AsyncTask.)
        // Since we have to touch views here, we check whether it's on UI thread or not,
        // if not, put it in it.

        // References:
        // https://stackoverflow.com/questions/11411022/how-to-check-if-current-thread-is
        // -not-main-thread
        // https://stackoverflow.com/questions/8183111/accessing-views-from-other-thread-android
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parseEvent(event);
                }
            });
            return;
        }

        if (event.matches(DroneEventObject.droneEventType.CONNECTED)) {
            if (layoutConnected != null) {
                layoutDisconnected.setVisibility(View.GONE);
                layoutConnected.setVisibility(View.VISIBLE);
            }
        } else if (event.matches(DroneEventObject.droneEventType.DISCONNECTED) ||
                event.matches(DroneEventObject.droneEventType.CONNECTION_LOST)) {
            if (layoutConnected != null) {
                layoutDisconnected.setVisibility(View.VISIBLE);
                layoutConnected.setVisibility(View.GONE);
            }
        }
    }
}
