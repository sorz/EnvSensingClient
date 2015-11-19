package mo.edu.ipm.stud.envsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing reducing gas.
 */
public class ReducingGas extends SugarRecord implements MeasureValue {
    private long measureId;
    private float value;

    public ReducingGas() {
    }

    public ReducingGas(Measurement measurement, float value) {
        measureId = measurement.getId();
        this.value = value;
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
        return !(Float.isInfinite(getValue()) || Float.isNaN(getValue()));
    }

}
