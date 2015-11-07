package mo.edu.ipm.stud.envsensing.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import mo.edu.ipm.stud.envsensing.R;

/**
 * Display Google Maps.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private MapFragment mapFragment;
    private GoogleMap map;

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

    }
}
