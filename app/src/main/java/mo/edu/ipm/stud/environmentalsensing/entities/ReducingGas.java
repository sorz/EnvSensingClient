package mo.edu.ipm.stud.environmentalsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing reducing gas.
 */
public class ReducingGas extends SugarRecord<ReducingGas> {
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

    public float getValue() {
        return value;
    }

}