package mo.edu.ipm.stud.environmentalsensing;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A {@link Fragment} used to display status and stop the running recording task.
 */
public class RecordStatusFragment extends Fragment {
    private OnRecordingStoppedListener callback;

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
        if (RecordService.isRunning())
            getActivity().stopService(new Intent(getActivity(), RecordService.class));
        callback.onRecordingStopped();
    }
}
