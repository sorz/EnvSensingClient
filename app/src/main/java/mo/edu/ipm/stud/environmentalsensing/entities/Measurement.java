package mo.edu.ipm.stud.environmentalsensing.entities;

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
    public <T extends SugarRecord<?>> T getData(Class<T> type) {
        List<T> result = Temperature.find(type, "MEASURE_ID = ?", "" + getId());
        if (result.size() > 0)
            return result.get(0);
        else
            return null;
    }
}
