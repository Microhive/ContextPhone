package dk.itu.ubicomp.android.contextservice;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 */
public class NearbyBeacons extends Fragment {

    public NearbyBeacons() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nearby_beacons, container, false);
        return v;
    }
}
