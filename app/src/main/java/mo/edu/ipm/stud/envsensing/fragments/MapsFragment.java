package mo.edu.ipm.stud.envsensing.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.Date;
import java.util.List;

import mo.edu.ipm.stud.envsensing.R;
import mo.edu.ipm.stud.envsensing.entities.Measurement;

/**
 * Display Google Maps.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private final static int REQUEST_SELECT_DATE = 0;

    private MapFragment mapFragment;
    private GoogleMap map;

    private FloatingActionButton buttonSelectDate;

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
        mapFragment = MapFragment.newInstance(options);
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

        buttonSelectDate.setVisibility(View.VISIBLE);
    }

    private void selectDate() {
        List<Measurement> minMeasure
                = Measurement.find(Measurement.class, null, null, null, "TIMESTAMP", "1");
        if (minMeasure.isEmpty())
            return;  // TODO: tell user.

        long minDate = minMeasure.get(0).getTimestamp();
        long maxDate = new Date().getTime();
        DatePickerFragment fragment = DatePickerFragment.newInstance(minDate, maxDate);
        fragment.setTargetFragment(this, REQUEST_SELECT_DATE);
        fragment.show(getFragmentManager(), "dialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_DATE
                && resultCode == DatePickerFragment.RESULT_SELECTED) {
            Long dateFrom = data.getLongExtra(DatePickerFragment.RESULT_DATE_FROM, 0);
            Long dateTo = data.getLongExtra(DatePickerFragment.RESULT_DATE_TO, 0);
            refreshMarkers(dateFrom, dateTo);
        }
    }

    private void refreshMarkers(final long dateFrom, final long dateTo) {
        new AsyncTask<Void, Void, List<Measurement>>() {
            @Override
            protected List<Measurement> doInBackground(Void... voids) {
                return Measurement.find(Measurement.class,
                        "TIMESTAMP BETWEEN ? AND ?", "" + dateFrom, "" + dateTo);
            }

            @Override
            protected void onPostExecute(List<Measurement> measurements) {
                System.out.println(measurements.size());

                // TODO: add markers.
            }
        }.execute();
    }
}
