package dk.itu.ubicomp.android.contextservice;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyLocation extends Fragment implements OnMapReadyCallback, GoogleMap.OnIndoorStateChangeListener {

    public MyLocation() {
        // Required empty public constructor
    }

    private GoogleMap mMap;
    Map<Integer, List<Marker>> mFloorMap = new HashMap<Integer, List<Marker>>();

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

//        List<Marker> markers = ShowFloorMarkers(0);
        List<Marker> markers = ShowCachedBeacons();

        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }

        if (markers.size() > 0)
        {
            LatLngBounds bounds = builder.build();

            int padding = 200;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            googleMap.moveCamera(cu);
        }

        mMap.setOnIndoorStateChangeListener(this);
    }

    private List<Marker> ShowFloorMarkers(int floor)
    {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        switch (floor) {
            case 0:
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659821, 12.590897)).title("Cafe Analog")));
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659405, 12.590696)).title("ScrollBar")));
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659213, 12.591240)).title("Eatit")));
                break;
            case 1:
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.660007, 12.590953)).title("Læsesal")));
                break;
            case 2:
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.660017, 12.591497)).title("IT Afdeling")));
                break;
            case 3:
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659291, 12.591222)).title("Studievejledning")));
                break;
            case 4:
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659751, 12.591445)).title("Auditorium 4")));
                break;
            case 5:
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(55.659582, 12.591392)).title("Pit Lab")));
                break;
        }
        return markers;
    }

    private List<Marker> ShowCachedBeacons()
    {
        ArrayList<Marker> markers = new ArrayList<Marker>();
        for(Map.Entry<String, BeaconDb.BeaconData> entry : BeaconDb.getInstance().getmMapOfBeaconData().entrySet()) {
            String key = entry.getKey();
            BeaconDb.BeaconData value = entry.getValue();
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(value.mLocation.getLatitude(), value.mLocation.getLongitude())).title("Floor " + value.mBeacon.getMajor() + ", " + value.mBeacon.getMinor())));
        }
        return markers;
    }

    @Override
    public void onIndoorBuildingFocused() {

    }

    @Override
    public void onIndoorLevelActivated(IndoorBuilding indoorBuilding) {
//        int floor = -(indoorBuilding.getActiveLevelIndex() - 5);
//        Log.d("FLOOR", Integer.toString(floor, -404));
//        mMap.clear();
//        ShowFloorMarkers(floor);
    }
}
