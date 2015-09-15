package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.app.Fragment;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import mo.edu.ipm.stud.environmentalsensing.R;
import mo.edu.ipm.stud.environmentalsensing.services.RecordService;

/**
 * A {@link Fragment} used to configure and start a new recording task.
 */
public class RecordConfigFragment extends Fragment {
    static private final int REQUEST_ENABLE_BT = 0;

    private OnRecordingStartedListener callback;
    private SharedPreferences preferences;

    private NumberPicker pickerHours;
    private NumberPicker pickerMinutes;

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
        View view = inflater.inflate(R.layout.fragment_record_config, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Button buttonStart = (Button) view.findViewById(R.id.button_start);
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

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkThenStartService();
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(getString(R.string.pref_recording_duration_hours),
                pickerHours.getValue());
        editor.putInt(getString(R.string.pref_recording_duration_minutes),
                pickerMinutes.getValue());
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
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(),
                    R.string.bluetooth_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            startService();
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
                    startService();
        }
    }

    private void startService() {
        if (!RecordService.isRunning()) {
            long durationSeconds = pickerHours.getValue() * 3600 + pickerMinutes.getValue() * 60;
            if (durationSeconds <= 0)
                return;
            Intent intent = new Intent(getActivity(), RecordService.class);
            intent.setAction(RecordService.ACTION_NEW);
            intent.putExtra(RecordService.EXTRA_RECORDING_START, SystemClock.elapsedRealtime());
            intent.putExtra(RecordService.EXTRA_RECORDING_END,
                    SystemClock.elapsedRealtime() + durationSeconds * 1000);
            getActivity().startService(intent);
        }
        callback.onRecordingStarted();
    }
}
