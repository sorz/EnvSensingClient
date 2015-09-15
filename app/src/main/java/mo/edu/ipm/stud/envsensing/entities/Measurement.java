package mo.edu.ipm.stud.envsensing.entities;

import android.support.annotation.Nullable;

import com.orm.SugarRecord;

import java.util.Date;
import java.util.List;

/**
 * Logging the date time of a specific measurement.
 */
public class Measurement extends SugarRecord<Measurement> {
    private long timestamp;

    public Measurement() {
        timestamp = System.currentTimeMillis();
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getDate() {
        return new Date(getTimestamp());
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
}
