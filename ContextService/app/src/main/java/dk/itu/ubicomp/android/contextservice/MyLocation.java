package dk.itu.ubicomp.android.contextservice;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.IndoorLevel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MyLocation extends Fragment implements OnMapReadyCallback, GoogleMap.OnIndoorStateChangeListener {

    public MyLocation() {
        // Required empty public constructor
    }

    private GoogleMap mMap;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_location, container, false);
        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        List<Marker> markers = PrepareMarkers();

        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int padding = 200;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        googleMap.moveCamera(cu);

        mMap.setOnIndoorStateChangeListener(this);
    }

    public List<Marker> PrepareMarkers()
    {
        List<Marker> markers = new ArrayList<Marker>();
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659629, 12.590958)).title("Room1")));
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.660024, 12.591505)).title("Room2")));
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.660045, 12.590923)).title("Room3")));
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659316, 12.590700)).title("Room4")));
        markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659139, 12.591215)).title("Room5")));

        return markers;
    }

    @Override
    public void onIndoorBuildingFocused() {

    }

    @Override
    public void onIndoorLevelActivated(IndoorBuilding indoorBuilding) {
        int floor = indoorBuilding.getActiveLevelIndex();
        Log.d("FLOOR", Integer.toString(-(floor - 5), -404));
    }
}
