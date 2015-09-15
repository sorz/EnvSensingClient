package mo.edu.ipm.stud.envsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing pressure.
 */
public class Pressure extends SugarRecord<Pressure> implements MeasureValue {
    private long measureId;
    private float value;

    public Pressure() {
    }

    public Pressure(Measurement measurement, float pascal) throws InvalidValueException {
        measureId = measurement.getId();
        if (pascal <= 0)
            throw new InvalidValueException("Value " + pascal + " is less than or equal to 0.");
        value = pascal;
    }

    public long getMeasureId() {
        return measureId;
    }

    @Override
    public float getValue() {
        return value;
    }

    public float getAtmospheres() {
        return (float) (value * 9.86923267e-6);
    }

    public float getTorr() {
        return  (float) (value * 0.00750061683);
    }

    public float getAltitudeMeters() {
        return (float) ((1 - Math.pow(value / 101326.0, 0.1902632)) * 44330.77);
    }

    public float getAltitudeFeet() {
        return getAltitudeMeters() * 3.2084f;
    }

}
