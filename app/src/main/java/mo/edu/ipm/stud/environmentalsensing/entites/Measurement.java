package mo.edu.ipm.stud.environmentalsensing.entites;

import com.orm.SugarRecord;

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
}
