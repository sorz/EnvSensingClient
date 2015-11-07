package mo.edu.ipm.stud.envsensing.entities;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.orm.SugarRecord;

import java.util.Date;
import java.util.List;

/**
 * Logging the date time of a specific measurement.
 */
public class Measurement extends SugarRecord<Measurement> implements ClusterItem {
    private long timestamp;
    private boolean uploaded;
    private String tag;

    public Measurement() {
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        return new Date(getTimestamp());
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Nullable
    public <T extends SugarRecord<?>> T getValue(Class<T> type) {
        MeasurementValueCache cache = MeasurementValueCache.getInstance();
        T value = cache.getValue(getId(), type);
        if (value != null)
            return value;

        List<T> result = Temperature.find(type, "MEASURE_ID = ?", "" + getId());
        if (result.size() == 0)
            return null;

        value = result.get(0);
        cache.putValue(getId(), value);
        return value;
    }

    @Nullable
    public <T extends SugarRecord<?>> T getValueWithoutCache(Class<T> type) {
        // TODO: Remove redundancy.
        List<T> result = Temperature.find(type, "MEASURE_ID = ?", "" + getId());
        if (result.size() == 0)
            return null;
        else
            return result.get(0);
    }

    public String getTag() {
        return tag;
    }

    @Override
    public LatLng getPosition() {
        LocationInfo location = getValue(LocationInfo.class);
        if (location == null)
            return null;
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
