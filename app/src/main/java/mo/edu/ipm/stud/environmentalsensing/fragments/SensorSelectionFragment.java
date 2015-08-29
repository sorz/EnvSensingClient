package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;

import mo.edu.ipm.stud.environmentalsensing.R;


/**
 * A {@link Fragment} used to pair and select the sensor device.
 */
public class SensorSelectionFragment extends Fragment {
    static private final int REQUEST_ENABLE_BT = 0;

    private OnSensorSelectedListener callback;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDeviceAdapter;
    private ArrayAdapter<String> unpairedDeviceAdapter;
    private BroadcastReceiver scanReceiver;

    public interface OnSensorSelectedListener {
        public void onSensorSelected(String mac);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), R.string.bluetooth_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            enableBluetooth();
        }

        pairedDeviceAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        unpairedDeviceAdapter =
                new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sensor_selection, container, false);

        Button buttonEnableBt = (Button) view.findViewById(R.id.button_enable_bluetooth);
        buttonEnableBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableBluetooth();
            }
        });
        Button buttonScan = (Button) view.findViewById(R.id.button_scan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanDevices();
            }
        });

        ListView listPairedDevices = (ListView) view.findViewById(R.id.list_paired_devices);
        ListView listUnpairedDevices = (ListView) view.findViewById(R.id.list_unpaired_devices);
        listPairedDevices.setAdapter(pairedDeviceAdapter);
        listUnpairedDevices.setAdapter(unpairedDeviceAdapter);
        loadPairedDevices();

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                String[] nameMac = item.split("\n");
                selectDeviceWithConfirm(nameMac[0], nameMac[1]);
            }
        };
        listPairedDevices.setOnItemClickListener(itemClickListener);
        listUnpairedDevices.setOnItemClickListener(itemClickListener);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                boolean enabled = resultCode == Activity.RESULT_OK;
                if (enabled)
                    loadPairedDevices();
                View view = getView();
                if (view != null) {
                    view.findViewById(R.id.bluetooth_disabled)
                            .setVisibility(enabled ? View.GONE : View.VISIBLE);
                    view.findViewById(R.id.bluetooth_enabled)
                            .setVisibility(enabled ? View.VISIBLE : View.GONE);
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
        if (scanReceiver != null)
            getActivity().unregisterReceiver(scanReceiver);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (OnSensorSelectedListener) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reference:
        // https://stackoverflow.com/questions/28389841/change-actionbar-title-using-fragments
        getActivity().setTitle(R.string.title_select_sensor);
    }

    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void scanDevices() {
        // Reference:
        // http://developer.android.com/intl/zh-cn/guide/topics/connectivity/
        // bluetooth.html#DiscoveringDevices
        if (scanReceiver == null) {
            scanReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device =
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        unpairedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        View view = getView();
                        if (view != null)
                            view.findViewById(R.id.button_scan).setEnabled(false);
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        View view = getView();
                        if (view != null)
                            view.findViewById(R.id.button_scan).setEnabled(true);
                    }
                }
            };

            getActivity().registerReceiver(scanReceiver,
                    new IntentFilter(BluetoothDevice.ACTION_FOUND));
            getActivity().registerReceiver(scanReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
            getActivity().registerReceiver(scanReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        }
        unpairedDeviceAdapter.clear();
        bluetoothAdapter.startDiscovery();
    }

    private void loadPairedDevices() {
        // Reference:
        // http://developer.android.com/intl/zh-cn/guide/topics/connectivity/
        // bluetooth.html#QueryingPairedDevices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        pairedDeviceAdapter.clear();
        for (BluetoothDevice device : pairedDevices) {
            pairedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
        }
    }

    private void selectDeviceWithConfirm(final String name, final String mac) {
        if (!name.startsWith("Sensordrone"))
            new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.dialog_select_device_confirm_message, name, mac))
                    .setTitle(R.string.dialog_select_device_confirm_title)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            callback.onSensorSelected(mac);
                        }
                    })
                    .show();
        else
            callback.onSensorSelected(mac);
    }


}
