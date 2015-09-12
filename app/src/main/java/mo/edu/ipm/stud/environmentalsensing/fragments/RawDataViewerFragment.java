package mo.edu.ipm.stud.environmentalsensing.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orm.query.Select;

import java.util.List;

import mo.edu.ipm.stud.environmentalsensing.R;
import mo.edu.ipm.stud.environmentalsensing.adapters.RawDataAdapter;
import mo.edu.ipm.stud.environmentalsensing.entities.Measurement;


public class RawDataViewerFragment extends Fragment {
    private static final int MAX_LISTED_ITEMS = 1000;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RawDataAdapter adapter;


    public RawDataViewerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_raw_data_viewer, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        List<Measurement> measurements = Select.from(Measurement.class)
                .orderBy("-timestamp").limit("" + MAX_LISTED_ITEMS).list();
        adapter = new RawDataAdapter(measurements);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        layoutManager = null;
        adapter = null;
    }
}
