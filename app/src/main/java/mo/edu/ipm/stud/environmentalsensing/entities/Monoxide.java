package mo.edu.ipm.stud.environmentalsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing ppm of carbon monoxide by precision gas sensor.
 */
public class Monoxide extends SugarRecord<Monoxide> implements MeasureValue {
    private long measureId;
    private float value;

    public Monoxide() {
    }

    public Monoxide(Measurement measurement, float ppm) {
        measureId = measurement.getId();
        value = ppm;
    }

    public long getMeasureId() {
        return measureId;
    }

    @Override
    public float getValue() {
        return value;
    }
}
