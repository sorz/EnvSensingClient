package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import mo.edu.ipm.stud.envsensing.MainActivity;
import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.services.SensorService;

/**
 * Created by xierch on 2016/2/13.
 */
public class SensorConnectFragment extends Fragment {
    static private final String TAG = "SensorConnectFragment";
    static private final int REQUEST_ENABLE_BT = 0;

    private SharedPreferences preferences;
    private TextView textConnectState;
    private Button buttonConnect;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_sensor_control);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_connect, container, false);
        buttonConnect = (Button) view.findViewById(R.id.button_connect);
        textConnectState = (TextView) view.findViewById(R.id.text_connect_status);

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (preferences.getString(getString(R.string.pref_bluetooth_mac), null) == null) {
                    Toast.makeText(getActivity(),
                            R.string.need_to_select_sensor, Toast.LENGTH_SHORT).show();
                    // TODO: Show sensor selection fragment directly.
                    return;
                }
                checkPermissionThenConnectSensor();
            }
        });

        checkState();

        return view;
    }

    private void checkState() {
        SensorService service = ((MainActivity) getActivity()).getSensorService();
        if (service == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkState();
                }
            }, 500);
            return;
        }
        switch (service.getState()) {
            case DISCONNECTED:
                textConnectState.setText(R.string.sensor_not_connect);
                buttonConnect.setEnabled(true);
                break;
            case CONNECTING:
                textConnectState.setText(R.string.sensor_is_connecting);
                buttonConnect.setEnabled(false);
                break;
            default:
                Log.wtf(TAG, "This fragment cannot handle the state.");
                break;
        }
    }

    @Override
    public void onDestroyView() {
        // Reference:
        // https://stackoverflow.com/questions/26369905/activitys-ondestroy-fragments-
        // ondestroyview-set-null-practices
        super.onDestroyView();
        textConnectState = null;
        buttonConnect = null;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK)
                    // This request only be sent when user request to connect sensor.
                    // So we connect it immediately after this request be accepted.
                    connectSensorWithoutCheckPermission();
        }
    }


    private void checkPermissionThenConnectSensor() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(),
                    R.string.bluetooth_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            checkPermissionThenConnectSensor();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            // Connect on onActivityResult() if Bluetooth is enabled instead of now.
        }
    }

    private void connectSensorWithoutCheckPermission() {
        Intent intent = new Intent(getActivity(), SensorService.class);
        intent.setAction(SensorService.ACTION_CONNECT);
        getActivity().startService(intent);
        buttonConnect.setEnabled(false);
    }

}
