package dk.itu.ubicomp.android.contextservice;

/**
 * Created by Eiler on 17/03/2016.
 */

import com.estimote.sdk.Beacon;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * Date: 13/05/13
 * Time: 10:36
 */
public class BeaconDb {

    private static BeaconDb mInstance = null;
    private String mString;

    public void AddItem(String s, Beacon b)
    {
        if (!mMapOfBeacons.containsValue(s))
        {
            mMapOfBeacons.put(s, b);
        }
    }

    public Map<String, Beacon> getmMapOfBeacons() {
        return mMapOfBeacons;
    }

    private Map<String, Beacon> mMapOfBeacons;

    private BeaconDb(){
        mString = "Hello";
        mMapOfBeacons = new HashMap<String, Beacon>();
    }

    public static BeaconDb getInstance(){
        if(mInstance == null)
        {
            mInstance = new BeaconDb();
        }
        return mInstance;
    }

    public String getString(){
        return this.mString;
    }

    public void setString(String value){
        mString = value;
    }
}