package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mo.edu.ipm.stud.environmentalsensing.R;

/**
 * A {@link Fragment} to export raw data.
 */
public class ExportDataFragment extends Fragment {
    private OnDataExportedListener callback;

    public ExportDataFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_export_data);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_export_data, container, false);

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnDataExportedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDataExportedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    public interface OnDataExportedListener {
        public void onDataExported();
    }
}
