package mo.edu.ipm.stud.envsensing.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.adapters.RawDataAdapter;
import mo.edu.ipm.stud.envsensing.entities.Measurement;

/**
 * A dialog which list a set of measurements.
 */
public class DataListDialogFragment extends DialogFragment {
    public final static String ARGS_ITEMS = "args-items";

    private RecyclerView recyclerView;
    private RawDataAdapter adapter;


    static DataListDialogFragment newInstance(ArrayList<Measurement> items) {
        DataListDialogFragment fragment = new DataListDialogFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(ARGS_ITEMS, items);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        List<Measurement> measurements = getArguments().getParcelableArrayList(ARGS_ITEMS);
        if (measurements == null)
            measurements = new ArrayList<>();

        getDialog().setTitle(getString(R.string.data_list_dialog_title, measurements.size()));

        adapter = new RawDataAdapter(getActivity(), measurements);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        adapter = null;
    }


}
