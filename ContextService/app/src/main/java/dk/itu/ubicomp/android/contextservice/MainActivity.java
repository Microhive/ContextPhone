package dk.itu.ubicomp.android.contextservice;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static BeaconManager beaconManager;
    public static Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.floating_action_button, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = new Intent(this, ContextService.class);
        startService(intent);

        int startView = getIntent().getIntExtra("displayfragmentview", R.id.nav_my_beacons);
        DisplayFragmentView(startView);
        Log.d("ACTIVITY", "RESUMED!");

        beaconManager = new BeaconManager(this);
        region = new Region("rid", null, null, null);
        // add this below:
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                for (Beacon beacon : list) {
                    if (!BeaconDb.getInstance().getmMapOfBeacons().containsKey(beacon.getProximityUUID() + "," + beacon.getMajor() + "," + beacon.getMinor())) {
                        Log.d("BEACON FOUND!", beacon.getMajor() + ", " + beacon.getMinor());
                        BeaconDb.getInstance().AddItem(beacon.getProximityUUID() + "," + beacon.getMajor() + "," + beacon.getMinor(), beacon);
                    }
                }

                /*NearbyBeacons fragment = (NearbyBeacons) getSupportFragmentManager().findFragmentById(R.id.fragment1);
                fragment..<specific_function_name>();*/

                Log.d("Beacons that exist!", Integer.toString(BeaconDb.getInstance().getmMapOfBeacons().size(), 0));
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("displayfragmentview"))
        {
            int startView = getIntent().getIntExtra("displayfragmentview", R.id.nav_my_beacons);
            DisplayFragmentView(startView);
            Log.d("ACTIVITY", "RESUMED!");
        }

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DisplayFragmentView(item.getItemId());
        return true;
    }

    public void DisplayFragmentView(int viewId) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (viewId) {

            case R.id.nav_my_location:
                fragment = new MyLocation();
                title = getString(R.string.nav_title_my_location);
                break;

            case R.id.nav_privacy:
                fragment = new Privacy();
                title = getString(R.string.nav_title_privacy);
                break;

            /*case R.id.nav_my_beacons:
                fragment = new MyBeacons();
                title = getString(R.string.nav_title_my_beacons);
                break;*/

            case R.id.nav_nearby_beacons:
                fragment = new NearbyBeacons();
                title = getString(R.string.nav_title_nearby_beacons);
                break;

        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        // set the toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
