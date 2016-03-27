package dk.itu.ubicomp.android.contextservice;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estimote.sdk.Beacon;
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
//        List<Marker> markers = ShowCachedBeacons();
        List<Marker> markers = MarkClosestBeaconToMe();

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
        for(Map.Entry<String, BeaconDb.BeaconData> entry : BeaconDb.getInstance().getmMapOfBeaconData().entrySet()) {
            String key = entry.getKey();
            BeaconDb.BeaconData value = entry.getValue();
            if (value.mBeacon.getMajor() == floor)
            {
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(value.mLocation.getLatitude(), value.mLocation.getLongitude())).title("Floor " + value.mBeacon.getMajor() + ", " + value.mBeacon.getMinor())));
            }
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
        int floor = -(indoorBuilding.getActiveLevelIndex() - 5);
        Log.d("FLOOR", Integer.toString(floor, -404));
        mMap.clear();
        ShowFloorMarkers(floor);
    }

    private List<Marker> MarkClosestBeaconToMe()
    {
        ArrayList<Marker> markers = new ArrayList<Marker>();

        if (MainActivity.lastSpottedBeacon != null)
        {
            String key = MainActivity.lastSpottedBeacon.getProximityUUID() + "," + MainActivity.lastSpottedBeacon.getMajor()+ "," + MainActivity.lastSpottedBeacon.getMinor();
            BeaconDb.BeaconData entry = BeaconDb.getInstance().getmMapOfBeaconData().get(key);
            if (entry != null)
            {
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(entry.mLocation.getLatitude(), entry.mLocation.getLongitude())).title("Closest to: Floor " + entry.mBeacon.getMajor() + ", " + entry.mBeacon.getMinor())));
            }
        }
        return markers;
    }
}
