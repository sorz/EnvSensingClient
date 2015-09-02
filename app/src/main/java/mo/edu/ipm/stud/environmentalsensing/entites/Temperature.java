package mo.edu.ipm.stud.environmentalsensing.entites;

/**
 * Storing the
 */
public class Temperature extends MeasureValue<Temperature> {

    Temperature() {
    }

    public Temperature(Measurement measurement) {
        super(measurement, 1);
    }
}
