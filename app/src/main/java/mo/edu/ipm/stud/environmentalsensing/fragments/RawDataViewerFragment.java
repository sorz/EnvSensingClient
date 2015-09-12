package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.orm.query.Select;

import java.util.List;

import mo.edu.ipm.stud.environmentalsensing.R;
import mo.edu.ipm.stud.environmentalsensing.adapters.RawDataAdapter;
import mo.edu.ipm.stud.environmentalsensing.entities.Measurement;


public class RawDataViewerFragment extends Fragment {
    private static final int MAX_LISTED_ITEMS = 1000;

    private OnExportDataListener callback;

    private FloatingActionButton floatingButton;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RawDataAdapter adapter;


    public RawDataViewerFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_section_raw_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_raw_data_viewer, container, false);
        floatingButton = (FloatingActionButton) view.findViewById(R.id.floating_button);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        List<Measurement> measurements = Select.from(Measurement.class)
                .orderBy("-timestamp").limit("" + MAX_LISTED_ITEMS).list();
        if (measurements.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            floatingButton.setVisibility(View.GONE);
            view.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        } else {
            adapter = new RawDataAdapter(getActivity(), measurements);
            recyclerView.setAdapter(adapter);
            floatingButton.attachToRecyclerView(recyclerView);
            floatingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onExportData();
                }
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        layoutManager = null;
        adapter = null;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnExportDataListener) activity;
        }catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnExportDataListener");
        }
    }

    public interface OnExportDataListener {
        public void onExportData();
    }
}
