package mo.edu.ipm.stud.environmentalsensing;

import android.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A {@link Fragment} used to configure and start a new recording task.
 */
public class RecordConfigFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_record_config);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record_config, container, false);
        Button buttonStart = (Button) view.findViewById(R.id.button_start);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService();
            }
        });

        return view;
    }

    private void startService() {
        if (RecordService.isRunning())
            return;
        Intent intent = new Intent(getActivity(), RecordService.class);
        getActivity().startService(intent);
        // TODO: Switch to RecordStatusFragment.
    }
}
