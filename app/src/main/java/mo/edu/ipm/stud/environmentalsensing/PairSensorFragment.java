package mo.edu.ipm.stud.environmentalsensing;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private ListView listPairedDevices;

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

        pairedDeviceAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
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

        listPairedDevices = (ListView) view.findViewById(R.id.list_paired_devices);
        listPairedDevices.setAdapter(pairedDeviceAdapter);
        loadPairedDevices();

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

    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void loadPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        pairedDeviceAdapter.clear();
        for (BluetoothDevice device : pairedDevices) {
            pairedDeviceAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        pairedDeviceAdapter.notifyDataSetChanged();
    }

}
