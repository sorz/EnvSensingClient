package mo.edu.ipm.stud.environmentalsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing air temperature.
 */
public class Temperature extends SugarRecord<Temperature> implements MeasureValue {
    private long measureId;
    private float value;


    public Temperature() {
    }

    public Temperature(Measurement measurement, float kelvin) {
        measureId = measurement.getId();
        value = kelvin;
    }

    public long getMeasureId() {
        return measureId;
    }

    @Override
    public float getValue() {
        return value;
    }

    public float getCelsius() {
        return value - 273.15f;
    }

    public float getFahrenheit() {
        return value * 9f / 5f - 459.67f;
    }

}
