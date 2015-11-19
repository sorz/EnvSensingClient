package mo.edu.ipm.stud.envsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing relative humidity percentage.
 */
public class Humidity extends SugarRecord implements MeasureValue {
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

    @Override
    public float getValue() {
        return value;
    }

    @Override
    public boolean isValid() {
        return getValue() >= 0 && getValue() <= 100;
    }

}
