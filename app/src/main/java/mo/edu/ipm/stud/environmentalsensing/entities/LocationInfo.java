package mo.edu.ipm.stud.environmentalsensing.entities;

import android.location.Location;

import com.orm.SugarRecord;

/**
 * Storing location of one measurement.
 */
public class LocationInfo extends SugarRecord<LocationInfo> {
    private long measureId;
    private long time;
    private String provider;
    private double latitude;
    private double longitude;
    private float accuracy;

    public LocationInfo() {
    }

    public LocationInfo(Measurement measurement, Location location) {
        measureId = measurement.getId();
        time = location.getTime();
        provider = location.getProvider();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
    }

    public float getAccuracy() {
        return accuracy;
    }

    public long getMeasureId() {
        return measureId;
    }

    public long getTime() {
        return time;
    }

    public String getProvider() {
        return provider;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
