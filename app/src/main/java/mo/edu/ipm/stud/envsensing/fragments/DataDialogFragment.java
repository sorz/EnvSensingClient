package mo.edu.ipm.stud.envsensing.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.entities.Measurement;
import mo.edu.ipm.stud.envsensing.tasks.BindDataViewAsyncTask;

/**
 * A dialog which display data about a single measurement.
 */
public class DataDialogFragment extends DialogFragment {
    public final static String ARGS_ITEM = "args-item";

    static DataDialogFragment newInstance(Measurement item) {
        DataDialogFragment fragment = new DataDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARGS_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        int width = getResources().getDimensionPixelSize(R.dimen.popup_width);
        int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.title_data_dialog);

        View view = inflater.inflate(R.layout.fragment_data_dialog, container, false);

        Measurement item = getArguments().getParcelable(ARGS_ITEM);
        BindDataViewAsyncTask.ViewHolder viewHolder = new BindDataViewAsyncTask.ViewHolder(view);
        new BindDataViewAsyncTask(getActivity(), viewHolder, item).execute();
        return view;
    }

}
