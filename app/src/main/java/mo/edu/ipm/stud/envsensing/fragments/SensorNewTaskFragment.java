package mo.edu.ipm.stud.envsensing.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.services.RecordService;
import mo.edu.ipm.stud.envsensing.services.SensorService;

/**
 * A {@link Fragment} used to configure and start a new recording task.
 */
public class SensorNewTaskFragment extends Fragment {
    static private final int REQUEST_ENABLE_BT = 0;
    static private final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 0;

    private OnRecordingStartedListener callback;
    private SharedPreferences preferences;

    private NumberPicker pickerHours;
    private NumberPicker pickerMinutes;
    private TextView textTag;

    public interface OnRecordingStartedListener {
        public void onRecordingStarted();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_record_config);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (OnRecordingStartedListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_new_task, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Button buttonStart = (Button) view.findViewById(R.id.button_start);
        View experiment = view.findViewById(R.id.experiment);
        Button buttonMeasure = (Button) view.findViewById(R.id.button_measure);
        textTag = (TextView) view.findViewById(R.id.text_tag);

        pickerHours = (NumberPicker) view.findViewById(R.id.pickerHours);
        pickerMinutes = (NumberPicker) view.findViewById(R.id.pickerMinutes);
        pickerHours.setMaxValue(72);
        pickerHours.setMinValue(0);
        pickerMinutes.setMaxValue(59);
        pickerMinutes.setMinValue(0);
        pickerHours.setValue(preferences.getInt(
                getString(R.string.pref_recording_duration_hours), 0));
        pickerMinutes.setValue(preferences.getInt(
                getString(R.string.pref_recording_duration_minutes), 30));
        textTag.setText(preferences.getString(getString(R.string.pref_recording_tag), ""));

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkThenStartService();
            }
        });

        if (preferences.getBoolean(getString(R.string.pref_recording_experiment), false)) {
            experiment.setVisibility(View.VISIBLE);
            buttonMeasure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //makeSingleMeasure();
                }
            });
        } else {
            experiment.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pickerHours = null;
        pickerMinutes = null;
        textTag = null;
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.pref_recording_duration_hours),
                pickerHours.getValue());
        editor.putInt(getString(R.string.pref_recording_duration_minutes),
                pickerMinutes.getValue());
        editor.putString(getString(R.string.pref_recording_tag),
                textTag.getText().toString());
        editor.apply();
    }

    private void checkThenStartService() {
        if (preferences.getString(getString(R.string.pref_bluetooth_mac), null) == null) {
            Toast.makeText(getActivity(),
                    R.string.need_to_select_sensor, Toast.LENGTH_SHORT).show();
            return;
        }
        if (pickerHours.getValue() + pickerMinutes.getValue() == 0) {
            Toast.makeText(getActivity(), R.string.illegal_duration, Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            FragmentCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(),
                    R.string.bluetooth_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            //startService();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            // startService() will be called in onActivityResult().
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK)
                    // This request only be sent when user request to start service.
                    // So we start it immediately after this request be accepted.
                    //startService();
                ;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Still need to check Bluetooth permission, so we call it to check.
                    checkThenStartService();
                } else {
                    Toast.makeText(getActivity(), R.string.lack_location_permission,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startConnectSensorService() {
        Intent intent = new Intent(getActivity(), SensorService.class);
        intent.setAction(SensorService.ACTION_CONNECT);
        getActivity().startService(intent);
    }

    private void startNewTaskService() {
        long durationSeconds = pickerHours.getValue() * 3600 + pickerMinutes.getValue() * 60;
        if (durationSeconds <= 0)
            return;
        String tag = textTag.getText().toString();

        Intent intent = new Intent(getActivity(), SensorService.class);
        intent.setAction(SensorService.ACTION_NEW_TASK);
        intent.putExtra(SensorService.EXTRA_TASK_END,
                SystemClock.elapsedRealtime() + durationSeconds * 1000);
        intent.putExtra(RecordService.EXTRA_MEASURE_TAG, tag);
        getActivity().startService(intent);
    }
}
