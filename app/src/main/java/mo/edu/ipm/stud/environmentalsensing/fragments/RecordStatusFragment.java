package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import mo.edu.ipm.stud.environmentalsensing.R;
import mo.edu.ipm.stud.environmentalsensing.RecordService;

/**
 * A {@link Fragment} used to display status and stop the running recording task.
 */
public class RecordStatusFragment extends Fragment {
    static private final String TAG = "RecordStatusFragment";

    private OnRecordingStoppedListener callback;
    private RecordService service;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            service = ((RecordService.LocalBinder) binder).getService();
            Log.d(TAG, "Service connected.");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            service = null;
            Log.d(TAG, "Service disconnected.");
        }
    };

    public interface OnRecordingStoppedListener {
        public void onRecordingStopped();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_record_status);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (OnRecordingStoppedListener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RecordService.isRunning()) {
            callback.onRecordingStopped();
            return;
        }

        Intent intent = new Intent(getActivity(), RecordService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_WAIVE_PRIORITY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_status, container, false);
        Button buttonStop = (Button) view.findViewById(R.id.button_stop);

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
            }
        });

        return view;
    }

    private void stopService() {
        if (RecordService.isRunning()) {
            Intent intent = new Intent(getActivity(), RecordService.class);
            intent.setAction(RecordService.ACTION_STOP);
            getActivity().startService(intent);
        }
        callback.onRecordingStopped();
    }
}
