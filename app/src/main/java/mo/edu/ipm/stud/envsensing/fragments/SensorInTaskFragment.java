package mo.edu.ipm.stud.envsensing.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.services.RecordService;

/**
 * A {@link Fragment} used to display status and stop the running recording task.
 */
public class SensorInTaskFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    static private final String TAG = "SensorInTaskFragment";

    private SwipeRefreshLayout swipeLayout;
    private TextView textStartTime;
    private TextView textStopTime;
    private TextView textMeasureInterval;
    private TextView textSuccessCount;
    private TextView textFailCount;

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_record_status);
    }

    @Override
    public void onPause() {
        super.onPause();
        swipeLayout.setRefreshing(false);
        swipeLayout.destroyDrawingCache();
        swipeLayout.clearAnimation();
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
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        textStartTime = (TextView) view.findViewById(R.id.text_start_time);
        textStopTime = (TextView) view.findViewById(R.id.text_stop_time);
        textMeasureInterval = (TextView) view.findViewById(R.id.text_measure_interval);
        textSuccessCount = (TextView) view.findViewById(R.id.text_measure_success_count);
        textFailCount = (TextView) view.findViewById(R.id.text_measure_fail_count);
        Button buttonStop = (Button) view.findViewById(R.id.button_stop);

        swipeLayout.setOnRefreshListener(this);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
            }
        });

        return view;
    }

    private void refreshTaskInfo() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.US);
        //textStartTime.setText(dateFormat.format(service.getStartTime()));
        //textStopTime.setText(dateFormat.format(service.getStopTime()));
//        textMeasureInterval.setText(getString(R.string.certain_seconds,
//                (int) service.getInterval() / 1000));
    }

    private void refreshTaskStatus() {
        if (textStopTime == null)
            return;
        //textSuccessCount.setText("" + service.getMeasureSuccessCount());
        //textFailCount.setText("" + service.getMeasureFailCount());
    }

    @Override
    public void onRefresh() {
        if (!RecordService.isRunning()) {
            //callback.onRecordingStopped();
            return;
        }
        refreshTaskStatus();
        swipeLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        textStartTime = null;
        textStopTime = null;
        textMeasureInterval = null;
    }

    private void stopService() {
        if (RecordService.isRunning()) {
            Intent intent = new Intent(getActivity(), RecordService.class);
            intent.setAction(RecordService.ACTION_STOP);
            getActivity().startService(intent);
        }
        //callback.onRecordingStopped();
    }
}
