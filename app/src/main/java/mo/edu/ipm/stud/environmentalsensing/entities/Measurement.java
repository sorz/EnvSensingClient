package mo.edu.ipm.stud.environmentalsensing.entities;

import com.orm.SugarRecord;

import java.util.Date;

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
}
