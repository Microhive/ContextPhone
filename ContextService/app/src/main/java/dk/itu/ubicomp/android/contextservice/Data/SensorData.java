package dk.itu.ubicomp.android.contextservice.Data;

import java.io.Serializable;

/**
 * Created by Eiler on 27/03/2016.
 */
public class SensorData implements Serializable {
    public String id;
    public String sensortype;
    public String value;
    public String timestamp;

    public SensorData(String id, String sensortype, String value, String timestamp) {
        this.id = id;
        this.sensortype = sensortype;
        this.value = value;
        this.timestamp = timestamp;
    }
}
