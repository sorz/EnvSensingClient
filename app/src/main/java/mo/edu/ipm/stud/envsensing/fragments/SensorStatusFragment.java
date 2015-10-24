package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.android.Drone;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.SensorDrone;
import mo.edu.ipm.stud.envsensing.tasks.SensorConnectAsyncTask;


/**
 * A {@link Fragment} used to display status of sensor.
 */
public class SensorStatusFragment extends Fragment
        implements DroneEventHandler, SwipeRefreshLayout.OnRefreshListener {
    static private final String TAG = "SensorStatusFragment";
    static private final int REQUEST_ENABLE_BT = 0;

    private Drone drone;
    private SharedPreferences preferences;
    private int leftStatusEventCount;

    private SwipeRefreshLayout swipeLayout;
    private View layoutConnected;
    private View layoutDisconnected;
    private Button buttonConnect;
    private TextView textMacAddress;
    private TextView textBatteryStatus;
    private TextView textVersion;
    private TextView textEnabledSensors;


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
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_section_sensor_status);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_status, container, false);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        layoutConnected = view.findViewById(R.id.layout_connected);
        layoutDisconnected = view.findViewById(R.id.layout_disconnected);
        textMacAddress = (TextView) view.findViewById(R.id.text_mac_address);
        textBatteryStatus = (TextView) view.findViewById(R.id.text_battery_status);
        textVersion = (TextView) view.findViewById(R.id.text_version);
        textEnabledSensors = (TextView) view.findViewById(R.id.text_enabled_sensor);
        buttonConnect = (Button) view.findViewById(R.id.button_connect);
        Button buttonDisconnect = (Button) view.findViewById(R.id.button_disconnect);

        swipeLayout.setOnRefreshListener(this);
        // Send a pseudo event to trigger view initialization.
        parseEvent(new DroneEventObject(drone.isConnected ?
                DroneEventObject.droneEventType.CONNECTED :
                DroneEventObject.droneEventType.DISCONNECTED));

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferences.getString(getString(R.string.pref_bluetooth_mac), null) == null) {
                    Toast.makeText(getActivity(),
                            R.string.need_to_select_sensor, Toast.LENGTH_SHORT).show();
                    // TODO: Show sensor selection fragment directly.
                    return;
                }

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null) {
                    Toast.makeText(getActivity(),
                            R.string.bluetooth_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (bluetoothAdapter.isEnabled()) {
                    connectSensor();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    // Connect on onActivityResult() if Bluetooth is enabled instead of now.
                }
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
        // Reference:
        // https://stackoverflow.com/questions/26369905/activitys-ondestroy-fragments-
        // ondestroyview-set-null-practices
        super.onDestroyView();
        swipeLayout = null;
        layoutConnected = null;
        layoutDisconnected = null;
        buttonConnect = null;
        textMacAddress = null;
        textBatteryStatus = null;
        textVersion = null;
        textEnabledSensors = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK)
                    // This request only be sent when user request to connect sensor.
                    // So we connect it immediately after this request be accepted.
                    connectSensor();
        }
    }

    /**
     * Connect sensor without check preconditions.
     */
    private void connectSensor() {
        String mac = preferences.getString(getString(R.string.pref_bluetooth_mac), "");
        new SensorConnectAsyncTask() {
            @Override
            protected void onPostExecute(Boolean result) {
                if (!result)
                    Toast.makeText(getActivity(),
                            R.string.connect_fail, Toast.LENGTH_SHORT).show();
                if (buttonConnect != null)
                    buttonConnect.setEnabled(true);
            }
        }.execute(mac);
        buttonConnect.setEnabled(false);
    }

    /**
     * Refresh status of Drone.
     *
     * This will send measure requests to Drone, and update views on parseEvent().
     */
    @Override
    public void onRefresh() {
        if (!drone.isConnected) {
            swipeLayout.setRefreshing(false);
            return;
        }
        // The number of measure requests sent.
        // Make parseEvent() know when to setRefreshing(false).
        leftStatusEventCount = 0;
        if (drone.checkIfCharging())
            leftStatusEventCount++;
        if (drone.measureBatteryVoltage())
            leftStatusEventCount++;
        drone.statusOfTemperature();
        drone.statusOfHumidity();
        drone.statusOfPressure();
        drone.statusOfPrecisionGas();
        drone.statusOfOxidizingGas();
        drone.statusOfReducingGas();
    }

    /**
     * Callback of Drone.
     * Maintain updated status of Drone.
     */
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
        Log.d(TAG, "Drone event received: " + event);

        if (layoutConnected == null)
            // Views has been destroyed, ignore.
            return;

        if (event.matches(DroneEventObject.droneEventType.CONNECTED)) {
            layoutDisconnected.setVisibility(View.GONE);
            layoutConnected.setVisibility(View.VISIBLE);
            textMacAddress.setText(drone.lastMAC);
            textVersion.setText(getString(R.string.version_hardware_firmware,
                    drone.hardwareVersion, drone.firmwareVersion, drone.firmwareRevision));
            swipeLayout.setEnabled(true);
            swipeLayout.setRefreshing(true);
            onRefresh();
        } else if (event.matches(DroneEventObject.droneEventType.DISCONNECTED) ||
            event.matches(DroneEventObject.droneEventType.CONNECTION_LOST)) {
            layoutDisconnected.setVisibility(View.VISIBLE);
            layoutConnected.setVisibility(View.GONE);
            swipeLayout.setEnabled(false);
            swipeLayout.setRefreshing(false);
        } else if (event.matches(DroneEventObject.droneEventType.BATTERY_VOLTAGE_MEASURED) ||
                event.matches(DroneEventObject.droneEventType.CHARGING_STATUS)) {
            leftStatusEventCount --;
            textBatteryStatus.setText(getString(R.string.battery_voltage_and_charging_status,
                    drone.batteryVoltage_Volts,
                    drone.isCharging ? getString(R.string.battery_in_charging)
                                     : getString(R.string.battery_not_in_charging)
            ));
        }

        String sensors = "";
        if (drone.temperatureStatus)
            sensors += "temperature ";
        if (drone.humidityStatus)
            sensors += "humidity ";
        if (drone.pressureStatus)
            sensors += "pressure ";
        if (drone.precisionGasStatus)
            sensors += "monoxide ";
        if (drone.oxidizingGasStatus)
            sensors += "oxidizing ";
        if (drone.reducingGasStatus)
            sensors += "reducing ";
        textEnabledSensors.setText(sensors);

        if (leftStatusEventCount <= 0)
            swipeLayout.setRefreshing(false);
    }
}
