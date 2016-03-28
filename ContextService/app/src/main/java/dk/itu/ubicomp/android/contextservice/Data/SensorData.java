package dk.itu.ubicomp.android.contextservice.Data;

import java.io.Serializable;

/**
 * Created by Eiler on 27/03/2016.
 */
public class SensorData implements Serializable {
    public String id;
    public String entype;
    public String sensortype;
    public String value;
    public String timestamp;
    public String androidID;

    public SensorData(String id, String entype, String sensortype, String value, String timestamp, String androidID) {
        this.id = id;
        this.entype = entype;
        this.sensortype = sensortype;
        this.value = value;
        this.timestamp = timestamp;
        this.androidID = androidID;
    }
}
