package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import mo.edu.ipm.stud.envsensing.MainActivity;
import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.services.SensorService;

/**
 * A {@link Fragment} used to display status and stop the running recording task.
 */
public class SensorInTaskFragment extends Fragment {
    static private final String TAG = "SensorInTaskFragment";

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_record_status);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_in_task, container, false);
        TextView textStopTime = (TextView) view.findViewById(R.id.text_stop_time);
        TextView textMeasureInterval = (TextView) view.findViewById(R.id.text_measure_interval);
        TextView textSuccessCount = (TextView) view.findViewById(R.id.text_measure_success_count);
        TextView textFailCount = (TextView) view.findViewById(R.id.text_measure_fail_count);
        TextView textSensorState = (TextView) view.findViewById(R.id.text_sensor_state);
        Button buttonStop = (Button) view.findViewById(R.id.button_stop);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
            }
        });

        SensorService service = ((MainActivity) getActivity()).getSensorService();

        textSuccessCount.setText(String.format("%d", service.getCurrentTaskMeasuringSuccessCount()));
        textFailCount.setText(String.format("%d", service.getCurrentTaskMeasuringFailCount()));
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
        textStopTime.setText(dateFormat.format(service.getCurrentTaskAutoStopTime()));
        textMeasureInterval.setText(getString(R.string.certain_seconds,
                (int) service.getCurrentTaskDoMeasuringInterval() / 1000));

        switch (service.getState()) {
            case TASK_REST:
                textSensorState.setText(R.string.sensor_state_task_rest);
                break;
            case TASK_MEASURING:
                textSensorState.setText(R.string.sensor_state_task_measuring);
                buttonStop.setEnabled(false);
                break;
            default:
                Log.wtf(TAG, "This fragment cannot handle the state.");
                break;
        }

        return view;
    }

    private void stopService() {
        Intent intent = new Intent(getActivity(), SensorService.class);
        intent.setAction(SensorService.ACTION_STOP_TASK);
        getActivity().startService(intent);
    }
}
