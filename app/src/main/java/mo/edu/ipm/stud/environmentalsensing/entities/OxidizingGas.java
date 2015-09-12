package mo.edu.ipm.stud.environmentalsensing.entities;

import com.orm.SugarRecord;

/**
 * Storing oxidizing gas.
 */
public class OxidizingGas extends SugarRecord<OxidizingGas> implements MeasureValue {
    private long measureId;
    private float value;

    public OxidizingGas() {
    }

    public OxidizingGas(Measurement measurement, float ohm) {
        measureId = measurement.getId();
        value = ohm;
    }

    public long getMeasureId() {
        return measureId;
    }

    @Override
    public float getValue() {
        return value;
    }

}
