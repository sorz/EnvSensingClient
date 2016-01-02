package mo.edu.ipm.stud.envsensing.fragments;


import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.entities.Measurement;

/**
 * Display Google Maps.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, ClusterManager.OnClusterClickListener<Measurement>, ClusterManager.OnClusterItemClickListener<Measurement> {
    private final static String TAG = "MapsFragment";
    private final static int REQUEST_SELECT_DATE = 0;

    private GoogleMap map;
    private ClusterManager<Measurement> clusterManager;

    private FloatingActionButton buttonSelectDate;
    private ProgressBar progressBar;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.title_section_maps);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        buttonSelectDate = (FloatingActionButton) view.findViewById(R.id.button_select_date);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        buttonSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDate();
            }
        });

        CameraPosition position = CameraPosition.fromLatLngZoom(new LatLng(22.18, 113.55), 12);
        // Set initial position to Macao. TODO: use last position?
        GoogleMapOptions options = new GoogleMapOptions()
                .camera(position);
        MapFragment mapFragment = MapFragment.newInstance(options);
        getFragmentManager().beginTransaction()
                .replace(R.id.map, mapFragment)
                .commit();

        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        buttonSelectDate = null;
        progressBar = null;
        clusterManager = null;
        map = null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMyLocationEnabled(true);

        clusterManager = new ClusterManager<>(getActivity(), map);
        map.setOnCameraChangeListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);

        buttonSelectDate.setVisibility(View.VISIBLE);
        selectDate();
    }

    private void selectDate() {
        List<Measurement> minMeasure
                = Measurement.find(Measurement.class, null, null, null, "TIMESTAMP", "1");
        if (minMeasure.isEmpty())
            return;  // TODO: tell user.

        long minDate = minMeasure.get(0).getTimestamp();
        long maxDate = new Date().getTime() + 24 * 3600 * 1000;  // Add one day
        DatePickerDialogFragment fragment = DatePickerDialogFragment.newInstance(minDate, maxDate);
        fragment.setTargetFragment(this, REQUEST_SELECT_DATE);
        fragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_DATE
                && resultCode == DatePickerDialogFragment.RESULT_SELECTED) {
            Long dateFrom = data.getLongExtra(DatePickerDialogFragment.RESULT_DATE_FROM, 0);
            Long dateTo = data.getLongExtra(DatePickerDialogFragment.RESULT_DATE_TO, 0);
            refreshMarkers(dateFrom, dateTo);
        }
    }

    private void refreshMarkers(final long dateFrom, final long dateTo) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                buttonSelectDate.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                List<Measurement> items = Measurement.find(Measurement.class,
                        "TIMESTAMP BETWEEN ? AND ?", "" + dateFrom, "" + dateTo);
                for (Iterator<Measurement> iterator = items.iterator(); iterator.hasNext();) {
                    Measurement measurement = iterator.next();
                    if (measurement.getPosition() == null) {
                        iterator.remove();
                    }
                }
                Log.d(TAG, "Measurements loaded: " + items.size());
                if (clusterManager != null) {
                    clusterManager.clearItems();
                    clusterManager.addItems(items);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                clusterManager.cluster();
                if (buttonSelectDate != null && progressBar != null) {
                    buttonSelectDate.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }.execute();
    }

    @Override
    public boolean onClusterClick(Cluster<Measurement> cluster) {
        ArrayList<Measurement> measurements = new ArrayList<>(cluster.getItems());
        DialogFragment fragment = DataListDialogFragment.newInstance(measurements);
        fragment.show(getFragmentManager(), "data-list");
        return true;
    }

    @Override
    public boolean onClusterItemClick(Measurement measurement) {
        DialogFragment fragment = DataDialogFragment.newInstance(measurement);
        fragment.show(getFragmentManager(), "data-dialog");
        return true;
    }
}
