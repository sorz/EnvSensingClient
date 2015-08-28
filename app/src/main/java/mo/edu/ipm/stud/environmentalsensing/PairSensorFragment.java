package mo.edu.ipm.stud.environmentalsensing;

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

import java.util.Set;


/**
 * A {@link Fragment} used to pair and select the sensor device.
 */
public class PairSensorFragment extends Fragment {
    static private final int REQUEST_ENABLE_BT = 0;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> pairedDeviceAdapter;
    private ArrayAdapter<String> unpairedDeviceAdapter;
    private BroadcastReceiver scanReceiver;

    private ListView listPairedDevices;
    private ListView listUnpairedDevices;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     * @return A new instance of fragment ConnectSensorFragment.
     */
    public static PairSensorFragment newInstance() {
        return new PairSensorFragment();
    }

    public PairSensorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // TODO: exit with a message.
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
        getActivity().setTitle(R.string.title_pair_sensor);
        View view = inflater.inflate(R.layout.fragment_connect_sensor, container, false);

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

        listPairedDevices = (ListView) view.findViewById(R.id.list_paired_devices);
        listUnpairedDevices = (ListView) view.findViewById(R.id.list_unpaired_devices);
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

    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void scanDevices() {
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
                            selectDevice(name, mac);
                        }
                    })
                    .show();
        else
            selectDevice(name, mac);
    }

    private void selectDevice(String name, String mac) {
        // TODO: Save MAC address and return.
        System.out.println(name);
    }

}
