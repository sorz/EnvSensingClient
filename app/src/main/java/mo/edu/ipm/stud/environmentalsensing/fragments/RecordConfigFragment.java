package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import mo.edu.ipm.stud.environmentalsensing.R;
import mo.edu.ipm.stud.environmentalsensing.RecordService;

/**
 * A {@link Fragment} used to configure and start a new recording task.
 */
public class RecordConfigFragment extends Fragment {
    private OnRecordingStartedListener callback;
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
        Button buttonStart = (Button) view.findViewById(R.id.button_start);
        pickerHours = (NumberPicker) view.findViewById(R.id.pickerHours);
        pickerMinutes = (NumberPicker) view.findViewById(R.id.pickerMinutes);
        pickerHours.setMaxValue(72);
        pickerHours.setMinValue(0);
        pickerMinutes.setMaxValue(59);
        pickerMinutes.setMinValue(0);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService();
            }
        });

        return view;
    }

    private void startService() {
        if (!RecordService.isRunning()) {
            long durationSeconds = pickerHours.getValue() + pickerMinutes.getValue() * 60;
            if (durationSeconds <= 0) {
                Toast.makeText(getActivity(), R.string.illegal_duration, Toast.LENGTH_SHORT).show();
                return;
            }
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
