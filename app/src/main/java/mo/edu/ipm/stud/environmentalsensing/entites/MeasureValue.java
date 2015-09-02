package mo.edu.ipm.stud.environmentalsensing.entites;

import com.orm.SugarRecord;

/**
 * Storing a numerical result measured by specific sensor.
 */
class MeasureValue<Sensor> extends SugarRecord<Sensor> {
    private long measureId;
    private int sensor;
    private double value;

    MeasureValue() {
    }

    MeasureValue(Measurement measurement, int sensor) {
        this.measureId = measurement.getId();
        this.sensor = sensor;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public int getSensor() {
        return sensor;
    }

    public long getMeasureId() {
        return measureId;
    }

}
