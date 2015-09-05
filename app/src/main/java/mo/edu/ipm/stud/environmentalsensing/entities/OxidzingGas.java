package mo.edu.ipm.stud.environmentalsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing oxidizing gas.
 */
public class OxidzingGas extends SugarRecord<OxidzingGas> {
    private long measureId;
    private float value;

    public OxidzingGas() {
    }

    public OxidzingGas(Measurement measurement, float value) {
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
