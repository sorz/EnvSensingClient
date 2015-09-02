package mo.edu.ipm.stud.environmentalsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing relative humidity percentage.
 */
public class Humidity extends SugarRecord<Humidity> {
    private long measureId;
    private float value;

    public Humidity() {
    }

    public Humidity(Measurement measurement, float percentage) {
        measureId = measurement.getId();
        value = percentage;
    }

    public long getMeasureId() {
        return measureId;
    }

    public float getValue() {
        return value;
    }

}
