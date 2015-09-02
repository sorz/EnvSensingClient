package mo.edu.ipm.stud.environmentalsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing ppm of carbon monoxide by precision gas sensor.
 */
public class Monoxide extends SugarRecord<Monoxide> {
    private long measureId;
    private float value;

    public Monoxide() {
    }

    public Monoxide(Measurement measurement, float ppm) {
        value = ppm;
    }

    public long getMeasureId() {
        return measureId;
    }

    public float getValue() {
        return value;
    }
}
